package com.example.votingv2.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 투표 생성 요청과 사용자 투표 제출 요청을 함께 처리하는 DTO
 */
@Getter
public class VoteRequest {

    // 투표 생성용 필드들
    private String title;               // 투표 제목
    private String description;         // 투표 설명
    private OffsetDateTime deadline;     // 마감 기한
    private List<VoteItemRequest> items; // 항목 리스트
    private OffsetDateTime startTime;


    // ✅ 사용자 투표용 필드 (항목 선택 시 사용)
    @Setter
    private Long selectedItemId;        // 사용자가 선택한 항목 ID

    // 항목 요청 내부 클래스
    @Getter
    @Setter
    public static class VoteItemRequest {
        private String itemText;        // 항목 이름
        private String description;     // 항목 설명
        private String image;
        private String promise;
    }
}