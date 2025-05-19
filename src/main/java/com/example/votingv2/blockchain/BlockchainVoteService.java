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
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;


import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockchainVoteService {

    private final Web3j web3 = Web3j.build(new HttpService("https://sepolia.infura.io/v3/83da2e9359224d08ae3fa24549c036b8"));
    private final String contractAddress = "0xd9145CCE52D386f254917e481eB44e9943F39138";

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

        System.out.println("✅ 불러온 프라이빗 키: " + key.getPrivateKey());
        Credentials credentials = Credentials.create(key.getPrivateKey());
        System.out.println("✅ 지갑 주소: " + credentials.getAddress());

        ContractGasProvider gasProvider = new StaticGasProvider(
                Convert.toWei("20", Convert.Unit.GWEI).toBigInteger(), // gasPrice
                BigInteger.valueOf(500_000)                            // gasLimit
        );

        return Vote.load(contractAddress, web3, credentials, gasProvider);
    }

    // ✅ 블록체인에 투표 생성
    public BigInteger createVote(String username, String title, List<String> items) throws Exception {
        try {
            Vote vote = loadContract(username);
            TransactionReceipt receipt = vote.createVote(title, items).send();
            System.out.println("📜 receipt.getLogs().size() = " + receipt.getLogs().size());
            receipt.getLogs().forEach(log -> System.out.println("🔹 log: " + log));

            List<Vote.VoteCreatedEventResponse> events = Vote.getVoteCreatedEvents(receipt);
            if (events.isEmpty()) {
                throw new IllegalStateException("VoteCreated 이벤트를 찾을 수 없습니다.");
            }
            return events.get(0).voteId;

        } catch (TransactionException e) {
            System.err.println("⚠️ 블록체인 트랜잭션 실패: " + e.getMessage());
            throw new RuntimeException("트랜잭션 실패 또는 처리 지연", e);
        }
    }

    // ✅ 특정 항목에 투표
    public TransactionReceipt submitVote(String username, BigInteger voteId, BigInteger itemIndex) throws Exception {
        return loadContract(username).submitVote(voteId, itemIndex).send();
    }

    // ✅ 투표 결과 조회
    public Map<String, Object> getVoteResult(String username, BigInteger voteId) throws Exception {
        Tuple3<String, List<String>, List<BigInteger>> result = loadContract(username).getVoteResult(voteId).send();

        String title = result.component1();
        List<String> items = result.component2();
        List<BigInteger> counts = result.component3();


        Map<String, Object> response = new HashMap<>();
        response.put("title", title);
        response.put("items", items);
        response.put("counts", counts);
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
