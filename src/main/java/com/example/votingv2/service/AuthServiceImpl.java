package com.example.votingv2.service;

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

/**
 * 로그인 로직 구현 클래스
 * 1. 사용자 존재 여부 확인
 * 2. 비밀번호 검증
 * 3. 블록체인 키 매칭
 * 4. JWT 토큰 생성 및 응답
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;                // 사용자 조회용
    private final UserBlockchainKeyRepository userBlockchainKeyRepository; // 🔥 블록체인 키 레포지토리
    private final PasswordEncoder passwordEncoder;              // 비밀번호 암호화 비교용
    private final JwtTokenProvider jwtTokenProvider;            // JWT 생성기

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        System.out.println("✅ AuthServiceImpl.login() 호출됨");

        // 1. 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 검증 (평문 비교 또는 암호화 비교)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        /*
        // 암호화 검증 버전
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        */

        // 3. 블록체인 키 매칭
        assignBlockchainKeyToUserIfNotAssigned(user.getId());

        // 4. JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());

        // 5. 응답 객체 반환
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    // 🔥 블록체인 키 매칭 메서드 (AuthServiceImpl 내부 메서드)
    private void assignBlockchainKeyToUserIfNotAssigned(Long userId) {
        boolean alreadyAssigned = userBlockchainKeyRepository.findByUserId(userId).isPresent();
        if (alreadyAssigned) {
            System.out.println("✅ 이미 블록체인 키가 할당된 사용자입니다. (userId=" + userId + ")");
            return;
        }

        UserBlockchainKey freeKey = userBlockchainKeyRepository.findFirstByUserIdIsNull()
                .orElseThrow(() -> new IllegalStateException("남은 블록체인 키가 없습니다."));

        freeKey.setUserId(userId);
        userBlockchainKeyRepository.save(freeKey);

        System.out.println("✅ 블록체인 키 매칭 완료: userId=" + userId);
    }
}
