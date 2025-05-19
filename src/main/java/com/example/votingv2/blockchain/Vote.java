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
import org.web3j.abi.datatypes.Address;
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
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

@SuppressWarnings("rawtypes")
public class Vote extends Contract {
    public static final String BINARY = "608060405260018055348015610013575f80fd5b506111c8806100215f395ff3fe608060405234801561000f575f80fd5b5060043610610055575f3560e01c80636f93bfb71461005957806390a3a11f14610075578063bfa0fc9314610091578063e207d04d146100c3578063e4fdeea3146100e1575b5f80fd5b610073600480360381019061006e919061070f565b610112565b005b61008f600480360381019061008a919061096b565b6102b7565b005b6100ab60048036038101906100a691906109e1565b6103bf565b6040516100ba93929190610c40565b60405180910390f35b6100cb610620565b6040516100d89190610c99565b60405180910390f35b6100fb60048036038101906100f691906109e1565b610626565b604051610109929190610cb2565b60405180910390f35b5f805f8481526020019081526020015f209050806003015f3373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f9054906101000a900460ff16156101b1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101a890610d2a565b60405180910390fd5b806002015482106101f7576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101ee90610d92565b60405180910390fd5b806001015f8381526020019081526020015f206001015f81548092919061021d90610ddd565b91905055506001816003015f3373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f6101000a81548160ff02191690831515021790555081837fda5ef9e376c941ca1c3a24da035b0dc80de942e10915eb12df78540f1d0793ec336040516102aa9190610e63565b60405180910390a3505050565b5f805f60015481526020019081526020015f20905082815f0190816102dc9190611076565b50815181600201819055505f5b82518110156103685760405180604001604052808483815181106103105761030f611145565b5b602002602001015181526020015f815250826001015f8381526020019081526020015f205f820151815f0190816103479190611076565b5060208201518160010155905050808061036090610ddd565b9150506102e9565b506001547f8225b3ad6e66d4c2342e977fbab464beaf3ae9d33b222938cff268dfd7b55b838460405161039b9190611172565b60405180910390a260015f8154809291906103b590610ddd565b9190505550505050565b60608060605f805f8681526020019081526020015f209050805f0180546103e590610ea9565b80601f016020809104026020016040519081016040528092919081815260200182805461041190610ea9565b801561045c5780601f106104335761010080835404028352916020019161045c565b820191905f5260205f20905b81548152906001019060200180831161043f57829003601f168201915b50505050509350806002015467ffffffffffffffff81111561048157610480610765565b5b6040519080825280602002602001820160405280156104b457816020015b606081526020019060019003908161049f5790505b509250806002015467ffffffffffffffff8111156104d5576104d4610765565b5b6040519080825280602002602001820160405280156105035781602001602082028036833780820191505090505b5091505f5b816002015481101561061757816001015f8281526020019081526020015f205f01805461053490610ea9565b80601f016020809104026020016040519081016040528092919081815260200182805461056090610ea9565b80156105ab5780601f10610582576101008083540402835291602001916105ab565b820191905f5260205f20905b81548152906001019060200180831161058e57829003601f168201915b50505050508482815181106105c3576105c2611145565b5b6020026020010181905250816001015f8281526020019081526020015f20600101548382815181106105f8576105f7611145565b5b602002602001018181525050808061060f90610ddd565b915050610508565b50509193909250565b60015481565b5f602052805f5260405f205f91509050805f01805461064490610ea9565b80601f016020809104026020016040519081016040528092919081815260200182805461067090610ea9565b80156106bb5780601f10610692576101008083540402835291602001916106bb565b820191905f5260205f20905b81548152906001019060200180831161069e57829003601f168201915b5050505050908060020154905082565b5f604051905090565b5f80fd5b5f80fd5b5f819050919050565b6106ee816106dc565b81146106f8575f80fd5b50565b5f81359050610709816106e5565b92915050565b5f8060408385031215610725576107246106d4565b5b5f610732858286016106fb565b9250506020610743858286016106fb565b9150509250929050565b5f80fd5b5f80fd5b5f601f19601f8301169050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b61079b82610755565b810181811067ffffffffffffffff821117156107ba576107b9610765565b5b80604052505050565b5f6107cc6106cb565b90506107d88282610792565b919050565b5f67ffffffffffffffff8211156107f7576107f6610765565b5b61080082610755565b9050602081019050919050565b828183375f83830152505050565b5f61082d610828846107dd565b6107c3565b90508281526020810184848401111561084957610848610751565b5b61085484828561080d565b509392505050565b5f82601f8301126108705761086f61074d565b5b813561088084826020860161081b565b91505092915050565b5f67ffffffffffffffff8211156108a3576108a2610765565b5b602082029050602081019050919050565b5f80fd5b5f6108ca6108c584610889565b6107c3565b905080838252602082019050602084028301858111156108ed576108ec6108b4565b5b835b8181101561093457803567ffffffffffffffff8111156109125761091161074d565b5b80860161091f898261085c565b855260208501945050506020810190506108ef565b5050509392505050565b5f82601f8301126109525761095161074d565b5b81356109628482602086016108b8565b91505092915050565b5f8060408385031215610981576109806106d4565b5b5f83013567ffffffffffffffff81111561099e5761099d6106d8565b5b6109aa8582860161085c565b925050602083013567ffffffffffffffff8111156109cb576109ca6106d8565b5b6109d78582860161093e565b9150509250929050565b5f602082840312156109f6576109f56106d4565b5b5f610a03848285016106fb565b91505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f5b83811015610a43578082015181840152602081019050610a28565b5f8484015250505050565b5f610a5882610a0c565b610a628185610a16565b9350610a72818560208601610a26565b610a7b81610755565b840191505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f819050602082019050919050565b5f82825260208201905092915050565b5f610ac982610a0c565b610ad38185610aaf565b9350610ae3818560208601610a26565b610aec81610755565b840191505092915050565b5f610b028383610abf565b905092915050565b5f602082019050919050565b5f610b2082610a86565b610b2a8185610a90565b935083602082028501610b3c85610aa0565b805f5b85811015610b775784840389528151610b588582610af7565b9450610b6383610b0a565b925060208a01995050600181019050610b3f565b50829750879550505050505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f819050602082019050919050565b610bbb816106dc565b82525050565b5f610bcc8383610bb2565b60208301905092915050565b5f602082019050919050565b5f610bee82610b89565b610bf88185610b93565b9350610c0383610ba3565b805f5b83811015610c33578151610c1a8882610bc1565b9750610c2583610bd8565b925050600181019050610c06565b5085935050505092915050565b5f6060820190508181035f830152610c588186610a4e565b90508181036020830152610c6c8185610b16565b90508181036040830152610c808184610be4565b9050949350505050565b610c93816106dc565b82525050565b5f602082019050610cac5f830184610c8a565b92915050565b5f6040820190508181035f830152610cca8185610a4e565b9050610cd96020830184610c8a565b9392505050565b7f61616161000000000000000000000000000000000000000000000000000000005f82015250565b5f610d14600483610a16565b9150610d1f82610ce0565b602082019050919050565b5f6020820190508181035f830152610d4181610d08565b9050919050565b7f62626262000000000000000000000000000000000000000000000000000000005f82015250565b5f610d7c600483610a16565b9150610d8782610d48565b602082019050919050565b5f6020820190508181035f830152610da981610d70565b9050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f610de7826106dc565b91507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8203610e1957610e18610db0565b5b600182019050919050565b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f610e4d82610e24565b9050919050565b610e5d81610e43565b82525050565b5f602082019050610e765f830184610e54565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f6002820490506001821680610ec057607f821691505b602082108103610ed357610ed2610e7c565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f60088302610f357fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82610efa565b610f3f8683610efa565b95508019841693508086168417925050509392505050565b5f819050919050565b5f610f7a610f75610f70846106dc565b610f57565b6106dc565b9050919050565b5f819050919050565b610f9383610f60565b610fa7610f9f82610f81565b848454610f06565b825550505050565b5f90565b610fbb610faf565b610fc6818484610f8a565b505050565b5b81811015610fe957610fde5f82610fb3565b600181019050610fcc565b5050565b601f82111561102e57610fff81610ed9565b61100884610eeb565b81016020851015611017578190505b61102b61102385610eeb565b830182610fcb565b50505b505050565b5f82821c905092915050565b5f61104e5f1984600802611033565b1980831691505092915050565b5f611066838361103f565b9150826002028217905092915050565b61107f82610a0c565b67ffffffffffffffff81111561109857611097610765565b5b6110a28254610ea9565b6110ad828285610fed565b5f60209050601f8311600181146110de575f84156110cc578287015190505b6110d6858261105b565b86555061113d565b601f1984166110ec86610ed9565b5f5b82811015611113578489015182556001820191506020850194506020810190506110ee565b86831015611130578489015161112c601f89168261103f565b8355505b6001600288020188555050505b505050505050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52603260045260245ffd5b5f6020820190508181035f83015261118a8184610a4e565b90509291505056fea2646970667358221220bab22ea1515b0dd9284916941f7439ff111c5a263ce511b20759c710f820d7f564736f6c63430008140033";

    private static String librariesLinkedBinary;

    public static final String FUNC_CREATEVOTE = "createVote";

    public static final String FUNC_GETVOTERESULT = "getVoteResult";

    public static final String FUNC_NEXTVOTEID = "nextVoteId";

    public static final String FUNC_SUBMITVOTE = "submitVote";

    public static final String FUNC_VOTEMAP = "voteMap";

    public static final Event VOTECREATED_EVENT = new Event("VoteCreated",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event VOTESUBMITTED_EVENT = new Event("VoteSubmitted",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}));
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

    public static List<VoteCreatedEventResponse> getVoteCreatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = new ArrayList<>();
        for (Log log : transactionReceipt.getLogs()) {
            Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTECREATED_EVENT, log);
            if (eventValues != null) {
                valueList.add(eventValues);
            }
        }

        ArrayList<VoteCreatedEventResponse> responses = new ArrayList<VoteCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            VoteCreatedEventResponse typedResponse = new VoteCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.voteId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.title = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
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

    public static List<VoteSubmittedEventResponse> getVoteSubmittedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = new ArrayList<>();
        for (Log log : transactionReceipt.getLogs()) {
            Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTECREATED_EVENT, log);
            if (eventValues != null) {
                valueList.add(eventValues);
            }
        }

        ArrayList<VoteSubmittedEventResponse> responses = new ArrayList<VoteSubmittedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            VoteSubmittedEventResponse typedResponse = new VoteSubmittedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.voteId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.itemIndex = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.voter = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static VoteSubmittedEventResponse getVoteSubmittedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTESUBMITTED_EVENT, log);
        VoteSubmittedEventResponse typedResponse = new VoteSubmittedEventResponse();
        typedResponse.log = log;
        typedResponse.voteId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.itemIndex = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.voter = (String) eventValues.getNonIndexedValues().get(0).getValue();
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

    public RemoteFunctionCall<Tuple3<String, List<String>, List<BigInteger>>> getVoteResult(
            BigInteger _voteId) {
        final Function function = new Function(FUNC_GETVOTERESULT,
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

    public RemoteFunctionCall<BigInteger> nextVoteId() {
        final Function function = new Function(FUNC_NEXTVOTEID,
                Arrays.<Type>asList(),
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

    public RemoteFunctionCall<Tuple2<String, BigInteger>> voteMap(BigInteger param0) {
        final Function function = new Function(FUNC_VOTEMAP,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple2<String, BigInteger>>(function,
                new Callable<Tuple2<String, BigInteger>>() {
                    @Override
                    public Tuple2<String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<String, BigInteger>(
                                (String) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue());
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

        public String voter;
    }
}
