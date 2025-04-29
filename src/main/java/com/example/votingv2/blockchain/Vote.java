package com.example.votingv2.blockchain;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.crypto.Credentials;
import java.util.stream.Collectors;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
/**
 * Vote.java
 *
 * 이 클래스는 Solidity로 작성된 스마트컨트랙트(Vote.sol)를
 * Java에서 직접 사용할 수 있도록 web3j가 래핑(wrapping)한 클래스입니다.
 * Vote.sol의 각 public 함수에 대응하는 Java 메서드들이 정의되어 있으며,
 * 실제 블록체인에 트랜잭션을 전송하거나, 데이터를 읽을 수 있습니다.
 */
public class Vote extends Contract {
    public static final String BINARY = "0x";

    // Vote 클래스 로드: Credentials 방식 (서명 필요)
    protected Vote(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }
    // Vote 클래스 로드: TransactionManager 방식 (프록시 등 사용 시)
    protected Vote(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }
    // 스마트컨트랙트를 불러오는 static 함수 (Credentials 사용)
    public static Vote load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new Vote(contractAddress, web3j, credentials, gasProvider);
    }
    // 스마트컨트랙트를 불러오는 static 함수 (TransactionManager 사용)
    public static Vote load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return new Vote(contractAddress, web3j, transactionManager, gasProvider);
    }

    public org.web3j.protocol.core.RemoteFunctionCall<TransactionReceipt> createVote(String title, List<String> itemNames) {
        List<Utf8String> converted = itemNames.stream().map(Utf8String::new).collect(Collectors.toList());
        final Function function = new Function(
                "createVote",
                Arrays.asList(new Utf8String(title), new DynamicArray<>(Utf8String.class, converted)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public org.web3j.protocol.core.RemoteFunctionCall<List<Type>> getVoteResult(BigInteger voteId) {
        final Function function = new Function(
                "getVoteResult",
                Arrays.asList(new Uint256(voteId)),
                Arrays.asList(
                        new TypeReference<Utf8String>() {},
                        new TypeReference<org.web3j.abi.datatypes.DynamicArray<Utf8String>>() {},
                        new TypeReference<org.web3j.abi.datatypes.DynamicArray<Uint256>>() {}
                ));
        return executeRemoteCallMultipleValueReturn(function);
    }

    public org.web3j.protocol.core.RemoteFunctionCall<TransactionReceipt> submitVote(BigInteger voteId, BigInteger itemIndex) {
        final Function function = new Function(
                "submitVote",
                Arrays.asList(new Uint256(voteId), new Uint256(itemIndex)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
}