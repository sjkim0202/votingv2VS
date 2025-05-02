package com.example.votingv2.repository;

import com.example.votingv2.entity.UserBlockchainKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBlockchainKeyRepository extends JpaRepository<UserBlockchainKey, Long> {
    Optional<UserBlockchainKey> findFirstByUserIdIsNull();
    Optional<UserBlockchainKey> findByUserId(Long userId);

}
