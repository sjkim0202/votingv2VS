package com.example.votingv2.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class VoteResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private boolean isClosed;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private boolean isPublic;
    private boolean isDeleted;
    private boolean voted;


    // ✅ 투표 항목 정보를 담는 내부 클래스 리스트
    private List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private Long itemId;
        private String itemText;
        private String description;
        private String image;
        private String promise;
    }
}
