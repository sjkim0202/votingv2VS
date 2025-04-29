package com.example.votingv2;

import com.example.votingv2.blockchain.Vote;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.List;

public class TestBlockchainRunner {

    public static void main(String[] args) throws Exception {

        try {
            // 1. Web3 연결
            Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:8545"));

            // 2. 테스트 계정 프라이빗 키 입력 (Hardhat에서 출력된 것 중 하나 사용)
            String privateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";
            Credentials credentials = Credentials.create(privateKey);

            // 3. 배포된 스마트컨트랙트 주소 입력
            String contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

            // 4. 컨트랙트 인스턴스 로드
            Vote vote = Vote.load(contractAddress, web3, credentials, new DefaultGasProvider());

            // 5. 투표 생성
            vote.createVote("좋아하는 언어는?", List.of("Java", "Python", "Solidity")).send();
            System.out.println("✅ 투표 생성 완료!");

            // 6. 투표 결과 조회
            var result = vote.getVoteResult(BigInteger.valueOf(1)).send();

            System.out.println("📊 제목: " + result.get(0).getValue());
            System.out.println("📊 항목: " + result.get(1).getValue());
            System.out.println("📊 득표: " + result.get(2).getValue());
        } catch (Exception e) {
            System.out.println("ㅈ댐");
            e.printStackTrace();
        }

    }
}