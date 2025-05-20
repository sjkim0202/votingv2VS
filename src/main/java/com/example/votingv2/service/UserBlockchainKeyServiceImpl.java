package com.example.votingv2.service;

import com.example.votingv2.entity.User;
import com.example.votingv2.entity.UserBlockchainKey;
import com.example.votingv2.repository.UserBlockchainKeyRepository;
import com.example.votingv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBlockchainKeyServiceImpl implements UserBlockchainKeyService {

    private final UserRepository userRepository;
    private final UserBlockchainKeyRepository userBlockchainKeyRepository;

    @Override
    public String getAddressByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserBlockchainKey key = userBlockchainKeyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("사용자의 지갑 정보가 존재하지 않습니다."));

        return key.getAddress();
    }
}
