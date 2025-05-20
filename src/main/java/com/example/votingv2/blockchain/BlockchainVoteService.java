package com.example.votingv2.blockchain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.web3j.tuples.generated.Tuple3;

@Service
@RequiredArgsConstructor
public class BlockchainVoteService {

    private final Web3j web3 = Web3j.build(new HttpService("https://sepolia.infura.io/v3/83da2e9359224d08ae3fa24549c036b8"));
    private final String contractAddress = "0x722D907B2f8B3f6014E8a6734f87F379f76Fb0a4";

    /**
     * 서버 지갑을 통해 투표 생성
     */
    public BigInteger createVoteAsServer(String title, List<String> items) throws Exception {
        Vote vote = Vote.load(contractAddress, web3, VotingServerWallet.getCredentials(), getGasProvider());
        TransactionReceipt receipt = vote.createVote(title, items).send();

        List<Vote.VoteCreatedEventResponse> events = Vote.getVoteCreatedEvents(receipt);
        if (events.isEmpty()) {
            throw new IllegalStateException("VoteCreated 이벤트를 찾을 수 없습니다.");
        }

        return events.get(0).voteId;
    }

    /**
     * 서버 지갑을 통해 사용자 대신 투표
     */
    public TransactionReceipt submitVoteAsServer(BigInteger voteId, BigInteger itemIndex) throws Exception {
        Vote vote = Vote.load(contractAddress, web3, VotingServerWallet.getCredentials(), getGasProvider());
        return vote.submitVote(voteId, itemIndex).send();
    }

    /**
     * 투표 결과 조회 (서버 지갑으로도 가능)
     */
    public Map<String, Object> getVoteResultServer(BigInteger voteId) throws Exception {
        Vote vote = Vote.load(contractAddress, web3, VotingServerWallet.getCredentials(), getGasProvider());
        Tuple3<String, List<String>, List<BigInteger>> result = vote.getAllVoteResults(voteId).send();

        Map<String, Object> response = new HashMap<>();
        response.put("title", result.component1());
        response.put("items", result.component2());
        response.put("counts", result.component3());
        return response;
    }

    private ContractGasProvider getGasProvider() {
        return new StaticGasProvider(
                Convert.toWei("20", Convert.Unit.GWEI).toBigInteger(),
                BigInteger.valueOf(500_000)
        );
    }
}
