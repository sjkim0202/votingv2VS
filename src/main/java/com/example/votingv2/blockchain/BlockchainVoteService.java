package com.example.votingv2.blockchain;

import com.example.votingv2.entity.User;
import com.example.votingv2.entity.UserBlockchainKey;
import com.example.votingv2.repository.UserBlockchainKeyRepository;
import com.example.votingv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockchainVoteService {

    private final Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:8545"));
    private final String contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

    private final UserRepository userRepository;
    private final UserBlockchainKeyRepository userBlockchainKeyRepository;

    /**
     * ✅ 유저에 따라 Credentials를 불러와서 컨트랙트 로드
     */
    private Vote loadContract(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        UserBlockchainKey key = userBlockchainKeyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("블록체인 키 없음"));

        Credentials credentials = Credentials.create(key.getPrivateKey());

        return Vote.load(contractAddress, web3, credentials, new DefaultGasProvider());
    }

    // ✅ 블록체인에 투표 생성
    public BigInteger createVote(String username, String title, List<String> items) throws Exception {
        try {
            TransactionReceipt receipt = loadContract(username).createVote(title, items).send();

            Event voteCreatedEvent = new Event("VoteCreated",
                    Arrays.asList(
                            new TypeReference<Uint256>(true) {},
                            new TypeReference<Utf8String>() {}
                    )
            );

            List<EventValuesWithLog> logs = extractEvent(receipt, voteCreatedEvent);

            if (logs.isEmpty()) {
                throw new IllegalStateException("VoteCreated 이벤트를 찾을 수 없습니다.");
            }

            return (BigInteger) logs.get(0).getIndexedValues().get(0).getValue();
        } catch (Exception e) {
            throw new RuntimeException("createVote 실패", e);
        }
    }

    // ✅ 특정 항목에 투표
    public TransactionReceipt submitVote(String username, BigInteger voteId, BigInteger itemIndex) throws Exception {
        return loadContract(username).submitVote(voteId, itemIndex).send();
    }

    // ✅ 투표 결과 조회
    public Map<String, Object> getVoteResult(BigInteger voteId) throws Exception {
        List<Type> result = loadContract("admin1").getVoteResult(voteId).send();
        // 🔥 getVoteResult는 그냥 admin 계정으로 호출 (데이터 조회만 할 때는 signer 신경 덜 써도 됨)

        String title = ((Utf8String) result.get(0)).getValue();
        List<Utf8String> items = (List<Utf8String>) result.get(1).getValue();
        List<Uint256> counts = (List<Uint256>) result.get(2).getValue();

        Map<String, Object> response = new HashMap<>();
        response.put("title", title);
        response.put("items", items.stream().map(Utf8String::getValue).collect(Collectors.toList()));
        response.put("counts", counts.stream().map(Uint256::getValue).collect(Collectors.toList()));
        return response;
    }

    // ✅ 이벤트 추출 유틸
    private List<EventValuesWithLog> extractEvent(TransactionReceipt receipt, Event event) {
        String encodedEventSignature = EventEncoder.encode(event);
        List<EventValuesWithLog> results = new ArrayList<>();

        for (Log log : receipt.getLogs()) {
            if (log.getTopics().isEmpty() || !log.getTopics().get(0).equals(encodedEventSignature)) continue;

            List<Type> indexedValues = new ArrayList<>();
            for (int i = 0; i < event.getIndexedParameters().size(); i++) {
                Type value = FunctionReturnDecoder.decodeIndexedValue(
                        log.getTopics().get(i + 1), event.getIndexedParameters().get(i));
                indexedValues.add(value);
            }

            @SuppressWarnings("unchecked")
            List<TypeReference<Type>> castedReferences = (List<TypeReference<Type>>) (List<?>) event.getNonIndexedParameters();
            List<Type> nonIndexedValues = FunctionReturnDecoder.decode(log.getData(), castedReferences);

            results.add(new EventValuesWithLog(indexedValues, nonIndexedValues, log));
        }

        return results;
    }

    public static class EventValuesWithLog {
        private final List<Type> indexedValues;
        private final List<Type> nonIndexedValues;
        private final Log log;

        public EventValuesWithLog(List<Type> indexedValues, List<Type> nonIndexedValues, Log log) {
            this.indexedValues = indexedValues;
            this.nonIndexedValues = nonIndexedValues;
            this.log = log;
        }

        public List<Type> getIndexedValues() {
            return indexedValues;
        }

        public List<Type> getNonIndexedValues() {
            return nonIndexedValues;
        }

        public Log getLog() {
            return log;
        }
    }
}
