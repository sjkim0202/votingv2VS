package com.example.votingv2.blockchain;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * BlockchainVoteService
 *
 * 이 서비스는 Vote 스마트컨트랙트와 통신하여
 * 투표 생성, 투표 제출, 결과 조회 기능을 제공합니다.
 */
@Service
public class BlockchainVoteService {

    // 하드햇 로컬 노드 주소
    private final Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:8545"));

    // 배포된 컨트랙트 주소 (반드시 수정)
    private final String contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

    // 하드햇 기본 계정 프라이빗 키 (반드시 수정)
    private final String privateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

    // 컨트랙트 인스턴스 로드
    private Vote loadContract() {
        Credentials credentials = Credentials.create(privateKey);
        return Vote.load(contractAddress, web3, credentials, new DefaultGasProvider());
    }

    /**
     * 블록체인에 투표 생성 요청
     */
    public TransactionReceipt createVote(String title, List<String> items) throws Exception {
        return loadContract().createVote(title, items).send();
    }

    /**
     * 특정 항목에 투표
     */
    public TransactionReceipt submitVote(BigInteger voteId, BigInteger itemIndex) throws Exception {
        return loadContract().submitVote(voteId, itemIndex).send();
    }

    /**
     * 투표 결과 조회 (제목, 항목 리스트, 득표 수)
     */
    public Map<String, Object> getVoteResult(BigInteger voteId) throws Exception {
        List<Type> result = loadContract().getVoteResult(voteId).send();

        String title = ((Utf8String) result.get(0)).getValue();
        List<Utf8String> items = (List<Utf8String>) result.get(1).getValue();
        List<Uint256> counts = (List<Uint256>) result.get(2).getValue();

        Map<String, Object> response = new HashMap<>();
        response.put("title", title);
        response.put("items", items.stream().map(Utf8String::getValue).collect(Collectors.toList()));
        response.put("count s", counts.stream().map(Uint256::getValue).collect(Collectors.toList()));
        return response;
    }
}