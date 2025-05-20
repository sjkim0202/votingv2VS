package com.example.votingv2.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class Vote extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b506112018061001c5f395ff3fe608060405234801561000f575f5ffd5b5060043610610060575f3560e01c80634d58648414610064578063599f36d1146100945780636f93bfb7146100b257806390a3a11f146100ce578063cc1d4003146100ea578063e4fdeea31461011c575b5f5ffd5b61007e6004803603810190610079919061078f565b61014e565b60405161008b91906107dc565b60405180910390f35b61009c61017d565b6040516100a991906107dc565b60405180910390f35b6100cc60048036038101906100c7919061078f565b610183565b005b6100e860048036038101906100e39190610a13565b61028a565b005b61010460048036038101906100ff9190610a89565b6103d9565b60405161011393929190610cce565b60405180910390f35b61013660048036038101906101319190610a89565b610694565b60405161014593929190610d32565b60405180910390f35b5f5f5f8481526020019081526020015f206001015f8381526020019081526020015f2060010154905092915050565b60015481565b5f5f5f8481526020019081526020015f209050806003015f9054906101000a900460ff166101e6576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101dd90610db8565b60405180910390fd5b8060020154821061022c576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161022390610e20565b60405180910390fd5b806001015f8381526020019081526020015f206001015f81548092919061025290610e6b565b919050555081837f1d4cdcaaff2fc01a95a07d98488ca526d20a79f9d50de8404981671338e66b1560405160405180910390a3505050565b815f5f60015481526020019081526020015f205f0190816102ab91906110af565b5080515f5f60015481526020019081526020015f206002018190555060015f5f60015481526020019081526020015f206003015f6101000a81548160ff0219169083151502179055505f5f90505b81518110156103835760405180604001604052808383815181106103205761031f61117e565b5b602002602001015181526020015f8152505f5f60015481526020019081526020015f206001015f8381526020019081526020015f205f820151815f01908161036891906110af565b506020820151816001015590505080806001019150506102f9565b506001547f8225b3ad6e66d4c2342e977fbab464beaf3ae9d33b222938cff268dfd7b55b83836040516103b691906111ab565b60405180910390a260015f8154809291906103d090610e6b565b91905055505050565b60608060605f5f5f8681526020019081526020015f209050806003015f9054906101000a900460ff16610441576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161043890610db8565b60405180910390fd5b5f816002015467ffffffffffffffff8111156104605761045f61080d565b5b60405190808252806020026020018201604052801561049357816020015b606081526020019060019003908161047e5790505b5090505f826002015467ffffffffffffffff8111156104b5576104b461080d565b5b6040519080825280602002602001820160405280156104e35781602001602082028036833780820191505090505b5090505f5f90505b83600201548110156105f457836001015f8281526020019081526020015f205f01805461051790610edf565b80601f016020809104026020016040519081016040528092919081815260200182805461054390610edf565b801561058e5780601f106105655761010080835404028352916020019161058e565b820191905f5260205f20905b81548152906001019060200180831161057157829003601f168201915b50505050508382815181106105a6576105a561117e565b5b6020026020010181905250836001015f8281526020019081526020015f20600101548282815181106105db576105da61117e565b5b60200260200101818152505080806001019150506104eb565b50825f01828282805461060690610edf565b80601f016020809104026020016040519081016040528092919081815260200182805461063290610edf565b801561067d5780601f106106545761010080835404028352916020019161067d565b820191905f5260205f20905b81548152906001019060200180831161066057829003601f168201915b505050505092509550955095505050509193909250565b5f602052805f5260405f205f91509050805f0180546106b290610edf565b80601f01602080910402602001604051908101604052809291908181526020018280546106de90610edf565b80156107295780601f1061070057610100808354040283529160200191610729565b820191905f5260205f20905b81548152906001019060200180831161070c57829003601f168201915b505050505090806002015490806003015f9054906101000a900460ff16905083565b5f604051905090565b5f5ffd5b5f5ffd5b5f819050919050565b61076e8161075c565b8114610778575f5ffd5b50565b5f8135905061078981610765565b92915050565b5f5f604083850312156107a5576107a4610754565b5b5f6107b28582860161077b565b92505060206107c38582860161077b565b9150509250929050565b6107d68161075c565b82525050565b5f6020820190506107ef5f8301846107cd565b92915050565b5f5ffd5b5f5ffd5b5f601f19601f8301169050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b610843826107fd565b810181811067ffffffffffffffff821117156108625761086161080d565b5b80604052505050565b5f61087461074b565b9050610880828261083a565b919050565b5f67ffffffffffffffff82111561089f5761089e61080d565b5b6108a8826107fd565b9050602081019050919050565b828183375f83830152505050565b5f6108d56108d084610885565b61086b565b9050828152602081018484840111156108f1576108f06107f9565b5b6108fc8482856108b5565b509392505050565b5f82601f830112610918576109176107f5565b5b81356109288482602086016108c3565b91505092915050565b5f67ffffffffffffffff82111561094b5761094a61080d565b5b602082029050602081019050919050565b5f5ffd5b5f61097261096d84610931565b61086b565b905080838252602082019050602084028301858111156109955761099461095c565b5b835b818110156109dc57803567ffffffffffffffff8111156109ba576109b96107f5565b5b8086016109c78982610904565b85526020850194505050602081019050610997565b5050509392505050565b5f82601f8301126109fa576109f96107f5565b5b8135610a0a848260208601610960565b91505092915050565b5f5f60408385031215610a2957610a28610754565b5b5f83013567ffffffffffffffff811115610a4657610a45610758565b5b610a5285828601610904565b925050602083013567ffffffffffffffff811115610a7357610a72610758565b5b610a7f858286016109e6565b9150509250929050565b5f60208284031215610a9e57610a9d610754565b5b5f610aab8482850161077b565b91505092915050565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f610ae682610ab4565b610af08185610abe565b9350610b00818560208601610ace565b610b09816107fd565b840191505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f819050602082019050919050565b5f82825260208201905092915050565b5f610b5782610ab4565b610b618185610b3d565b9350610b71818560208601610ace565b610b7a816107fd565b840191505092915050565b5f610b908383610b4d565b905092915050565b5f602082019050919050565b5f610bae82610b14565b610bb88185610b1e565b935083602082028501610bca85610b2e565b805f5b85811015610c055784840389528151610be68582610b85565b9450610bf183610b98565b925060208a01995050600181019050610bcd565b50829750879550505050505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f819050602082019050919050565b610c498161075c565b82525050565b5f610c5a8383610c40565b60208301905092915050565b5f602082019050919050565b5f610c7c82610c17565b610c868185610c21565b9350610c9183610c31565b805f5b83811015610cc1578151610ca88882610c4f565b9750610cb383610c66565b925050600181019050610c94565b5085935050505092915050565b5f6060820190508181035f830152610ce68186610adc565b90508181036020830152610cfa8185610ba4565b90508181036040830152610d0e8184610c72565b9050949350505050565b5f8115159050919050565b610d2c81610d18565b82525050565b5f6060820190508181035f830152610d4a8186610adc565b9050610d5960208301856107cd565b610d666040830184610d23565b949350505050565b7f766f746520646f6573206e6f74206578697374000000000000000000000000005f82015250565b5f610da2601383610abe565b9150610dad82610d6e565b602082019050919050565b5f6020820190508181035f830152610dcf81610d96565b9050919050565b7f696e76616c6964206974656d20696e64657800000000000000000000000000005f82015250565b5f610e0a601283610abe565b9150610e1582610dd6565b602082019050919050565b5f6020820190508181035f830152610e3781610dfe565b9050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f610e758261075c565b91507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8203610ea757610ea6610e3e565b5b600182019050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f6002820490506001821680610ef657607f821691505b602082108103610f0957610f08610eb2565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f60088302610f6b7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82610f30565b610f758683610f30565b95508019841693508086168417925050509392505050565b5f819050919050565b5f610fb0610fab610fa68461075c565b610f8d565b61075c565b9050919050565b5f819050919050565b610fc983610f96565b610fdd610fd582610fb7565b848454610f3c565b825550505050565b5f5f905090565b610ff4610fe5565b610fff818484610fc0565b505050565b5b81811015611022576110175f82610fec565b600181019050611005565b5050565b601f8211156110675761103881610f0f565b61104184610f21565b81016020851015611050578190505b61106461105c85610f21565b830182611004565b50505b505050565b5f82821c905092915050565b5f6110875f198460080261106c565b1980831691505092915050565b5f61109f8383611078565b9150826002028217905092915050565b6110b882610ab4565b67ffffffffffffffff8111156110d1576110d061080d565b5b6110db8254610edf565b6110e6828285611026565b5f60209050601f831160018114611117575f8415611105578287015190505b61110f8582611094565b865550611176565b601f19841661112586610f0f565b5f5b8281101561114c57848901518255600182019150602085019450602081019050611127565b868310156111695784890151611165601f891682611078565b8355505b6001600288020188555050505b505050505050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b5f6020820190508181035f8301526111c38184610adc565b90509291505056fea264697066735822122077a8cd2577cb4a3e780ab5b185c118f318f9cc4501a87f52933941e98990300d64736f6c634300081e0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_CREATEVOTE = "createVote";

    public static final String FUNC_GETALLVOTERESULTS = "getAllVoteResults";

    public static final String FUNC_GETVOTERESULT = "getVoteResult";

    public static final String FUNC_SUBMITVOTE = "submitVote";

    public static final String FUNC_VOTECOUNTER = "voteCounter";

    public static final String FUNC_VOTEMAP = "voteMap";

    public static final Event VOTECREATED_EVENT = new Event("VoteCreated",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event VOTESUBMITTED_EVENT = new Event("VoteSubmitted",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    @Deprecated
    protected Vote(String contractAddress, Web3j web3j, Credentials credentials,
                   BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Vote(String contractAddress, Web3j web3j, Credentials credentials,
                   ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Vote(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                   BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Vote(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                   ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<VoteCreatedEventResponse> getVoteCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Log> logs = transactionReceipt.getLogs(); // ✅ 트랜잭션에서 로그 추출
        List<VoteCreatedEventResponse> responses = new ArrayList<>();

        for (Log log : logs) {
            EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTECREATED_EVENT, log);
            if (eventValues != null) {
                VoteCreatedEventResponse typedResponse = new VoteCreatedEventResponse();
                typedResponse.log = log;
                typedResponse.voteId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.title = (String) eventValues.getNonIndexedValues().get(0).getValue();
                responses.add(typedResponse);
            }
        }

        return responses;
    }

    public static VoteCreatedEventResponse getVoteCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTECREATED_EVENT, log);
        VoteCreatedEventResponse typedResponse = new VoteCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.voteId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.title = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<VoteCreatedEventResponse> voteCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getVoteCreatedEventFromLog(log));
    }

    public Flowable<VoteCreatedEventResponse> voteCreatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(VOTECREATED_EVENT));
        return voteCreatedEventFlowable(filter);
    }

    public static List<VoteSubmittedEventResponse> getVoteSubmittedEvents(TransactionReceipt transactionReceipt) {
        List<Log> logs = transactionReceipt.getLogs(); // ✅ 로그 리스트 가져오기
        List<VoteSubmittedEventResponse> responses = new ArrayList<>();

        for (Log log : logs) {
            EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTESUBMITTED_EVENT, log);
            if (eventValues != null) {
                VoteSubmittedEventResponse typedResponse = new VoteSubmittedEventResponse();
                typedResponse.log = log;
                typedResponse.voteId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.itemIndex = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                responses.add(typedResponse);
            }
        }

        return responses;
    }

    public static VoteSubmittedEventResponse getVoteSubmittedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTESUBMITTED_EVENT, log);
        VoteSubmittedEventResponse typedResponse = new VoteSubmittedEventResponse();
        typedResponse.log = log;
        typedResponse.voteId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.itemIndex = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<VoteSubmittedEventResponse> voteSubmittedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getVoteSubmittedEventFromLog(log));
    }

    public Flowable<VoteSubmittedEventResponse> voteSubmittedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(VOTESUBMITTED_EVENT));
        return voteSubmittedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> createVote(String _title,
                                                             List<String> _itemNames) {
        final Function function = new Function(
                FUNC_CREATEVOTE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_title),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Utf8String>(
                                org.web3j.abi.datatypes.Utf8String.class,
                                org.web3j.abi.Utils.typeMap(_itemNames, org.web3j.abi.datatypes.Utf8String.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple3<String, List<String>, List<BigInteger>>> getAllVoteResults(
            BigInteger _voteId) {
        final Function function = new Function(FUNC_GETALLVOTERESULTS,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_voteId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<DynamicArray<Utf8String>>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<Tuple3<String, List<String>, List<BigInteger>>>(function,
                new Callable<Tuple3<String, List<String>, List<BigInteger>>>() {
                    @Override
                    public Tuple3<String, List<String>, List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, List<String>, List<BigInteger>>(
                                (String) results.get(0).getValue(),
                                convertToNative((List<Utf8String>) results.get(1).getValue()),
                                convertToNative((List<Uint256>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<BigInteger> getVoteResult(BigInteger _voteId, BigInteger _itemIndex) {
        final Function function = new Function(FUNC_GETVOTERESULT,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_voteId),
                        new org.web3j.abi.datatypes.generated.Uint256(_itemIndex)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> submitVote(BigInteger _voteId,
                                                             BigInteger _itemIndex) {
        final Function function = new Function(
                FUNC_SUBMITVOTE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_voteId),
                        new org.web3j.abi.datatypes.generated.Uint256(_itemIndex)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> voteCounter() {
        final Function function = new Function(FUNC_VOTECOUNTER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple3<String, BigInteger, Boolean>> voteMap(BigInteger param0) {
        final Function function = new Function(FUNC_VOTEMAP,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}));
        return new RemoteFunctionCall<Tuple3<String, BigInteger, Boolean>>(function,
                new Callable<Tuple3<String, BigInteger, Boolean>>() {
                    @Override
                    public Tuple3<String, BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, BigInteger, Boolean>(
                                (String) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                (Boolean) results.get(2).getValue());
                    }
                });
    }

    @Deprecated
    public static Vote load(String contractAddress, Web3j web3j, Credentials credentials,
                            BigInteger gasPrice, BigInteger gasLimit) {
        return new Vote(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Vote load(String contractAddress, Web3j web3j,
                            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Vote(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Vote load(String contractAddress, Web3j web3j, Credentials credentials,
                            ContractGasProvider contractGasProvider) {
        return new Vote(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Vote load(String contractAddress, Web3j web3j,
                            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Vote(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Vote> deploy(Web3j web3j, Credentials credentials,
                                          ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Vote.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<Vote> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
                                          BigInteger gasLimit) {
        return deployRemoteCall(Vote.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<Vote> deploy(Web3j web3j, TransactionManager transactionManager,
                                          ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Vote.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<Vote> deploy(Web3j web3j, TransactionManager transactionManager,
                                          BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Vote.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class VoteCreatedEventResponse extends BaseEventResponse {
        public BigInteger voteId;

        public String title;
    }

    public static class VoteSubmittedEventResponse extends BaseEventResponse {
        public BigInteger voteId;

        public BigInteger itemIndex;
    }
}
