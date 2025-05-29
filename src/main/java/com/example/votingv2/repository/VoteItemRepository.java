package com.example.votingv2.repository;

import com.example.votingv2.entity.Vote;
import com.example.votingv2.entity.VoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 투표 항목(후보자 등) 레포지토리
 */
public interface VoteItemRepository extends JpaRepository<VoteItem, Long> {
    List<VoteItem> findByVoteIdOrderByIdAsc(Long voteId); // 특정 투표에 속한 항목들 조회
}
