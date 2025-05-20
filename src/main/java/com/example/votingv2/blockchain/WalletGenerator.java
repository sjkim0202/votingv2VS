package com.example.votingv2.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

public class WalletGenerator {

    public static Credentials generateNewWallet() throws Exception {
        ECKeyPair keyPair = Keys.createEcKeyPair(); // 랜덤한 키 쌍 생성
        return Credentials.create(keyPair);
    }
}
