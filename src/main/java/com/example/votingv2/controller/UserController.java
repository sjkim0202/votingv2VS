package com.example.votingv2.controller;

import com.example.votingv2.entity.User;
import com.example.votingv2.entity.UserBlockchainKey;
import com.example.votingv2.repository.UserBlockchainKeyRepository;
import com.example.votingv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserBlockchainKeyRepository userBlockchainKeyRepository;

    // 지갑 주소 조회 API
    @GetMapping("/wallet-address")
    public ResponseEntity<String> getWalletAddress(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        UserBlockchainKey key = userBlockchainKeyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 블록체인 키가 존재하지 않습니다."));

        return ResponseEntity.ok(key.getAddress());
    }
}
