package com.example.votingv2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 투표 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "Vote")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 투표 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 설명

    private LocalDateTime deadline; // 마감일



    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy; // 생성한 관리자 계정

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;        // 시작시간

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성 시각

    @Column(name = "is_public")
    private boolean isPublic;   // 공개여부

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "blockchain_vote_id")
    private BigInteger blockchainVoteId;

    @Column(name = "is_closed")
    private Boolean isClosed;
}
