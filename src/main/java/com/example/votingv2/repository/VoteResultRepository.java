package com.example.votingv2.repository;

import com.example.votingv2.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 투표 결과 조회용 레포지토리
 */
public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {
    Optional<VoteResult> findByUserIdAndVoteId(Long userId, Long voteId); // 중복 투표 확인
    List<VoteResult> findByVoteItemId(Long voteItemId);
    int countByVoteIdAndVoteItemId(Long voteId, Long voteItemId); // 득표 수 계산

}
