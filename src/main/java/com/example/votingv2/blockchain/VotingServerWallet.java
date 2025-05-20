package com.example.votingv2.blockchain;

import org.web3j.crypto.Credentials;

public class VotingServerWallet {
    private static final String PRIVATE_KEY = "0cc66486e54168a8e31cb89cb2c4fd13544635b857dcb54720aa6e381a6b6fc2"; // MetaMask 지갑의 private key

    public static Credentials getCredentials() {
        return Credentials.create(PRIVATE_KEY);
    }
}
