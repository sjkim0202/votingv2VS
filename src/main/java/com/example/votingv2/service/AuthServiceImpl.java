package com.example.votingv2.service;

import com.example.votingv2.blockchain.WalletGenerator;
import com.example.votingv2.dto.LoginRequest;
import com.example.votingv2.dto.LoginResponse;
import com.example.votingv2.entity.User;
import com.example.votingv2.entity.UserBlockchainKey;
import com.example.votingv2.repository.UserRepository;
import com.example.votingv2.repository.UserBlockchainKeyRepository;
import com.example.votingv2.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;

import java.util.Optional;

/**
 * 로그인 로직 구현 클래스
 * 1. 사용자 존재 여부 확인
 * 2. 비밀번호 검증
 * 3. 블록체인 키 등록 (없을 경우)
 * 4. JWT 토큰 생성 및 응답
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserBlockchainKeyRepository userBlockchainKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        System.out.println("✅ AuthServiceImpl.login() 호출됨");

        // 1. 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 검증
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 블록체인 키 없으면 생성
        Optional<UserBlockchainKey> optionalKey = userBlockchainKeyRepository.findByUserId(user.getId());
        if (optionalKey.isEmpty()) {
            try {
                Credentials credentials = WalletGenerator.generateNewWallet();

                UserBlockchainKey newKey = UserBlockchainKey.builder()
                        .user(user)
                        .privateKey(credentials.getEcKeyPair().getPrivateKey().toString(16))
                        .publicKey(credentials.getEcKeyPair().getPublicKey().toString(16))
                        .address(credentials.getAddress())
                        .build();

                userBlockchainKeyRepository.save(newKey);
            } catch (Exception e) {
                throw new RuntimeException("지갑 생성 실패", e);
            }
        }

        // 4. JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());

        // 5. 응답 반환
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}
