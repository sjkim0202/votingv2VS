package com.example.votingv2.service;

import com.example.votingv2.blockchain.BlockchainVoteService;
import com.example.votingv2.dto.VoteRequest;
import com.example.votingv2.dto.VoteResponse;
import com.example.votingv2.dto.VoteResultResponseDto;
import com.example.votingv2.entity.*;
import com.example.votingv2.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.votingv2.repository.VoteRepository;
import com.example.votingv2.repository.VoteItemRepository;
import com.example.votingv2.blockchain.BlockchainVoteService;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 투표 생성, 조회, 사용자 투표 제출 및 삭제를 담당하는 서비스
 */
@Repository
@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final VoteItemRepository voteItemRepository;
    private final VoteResultRepository voteResultRepository;
    private final BlockchainVoteService blockchainVoteService;
    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);

    /**
     * 투표 생성 처리
     */
    @Transactional
    public void createVote(VoteRequest request) {
        // 1. DB에 저장
        Vote vote = Vote.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .deadline(request.getDeadline())
                .isClosed(false)
                .createdAt(LocalDateTime.now())
                .build();
        voteRepository.save(vote);

        List<VoteItem> voteItems = request.getItems().stream()
                .map(item -> VoteItem.builder()
                        .vote(vote)
                        .itemText(item.getItemText())
                        .description(item.getDescription())
                        .promise(item.getPromise())
                        .image(item.getImage() != null && !item.getImage().startsWith("data:")
                                ? "data:image/png;base64," + item.getImage()
                                : item.getImage())
                        .build())
                .collect(Collectors.toList());

        voteItemRepository.saveAll(voteItems);

        // 2. Blockchain에 등록 (서버 지갑 사용)
        try {
            List<String> itemNames = request.getItems().stream()
                    .map(VoteRequest.VoteItemRequest::getItemText)
                    .toList();

            BigInteger blockchainVoteId = blockchainVoteService.createVoteAsServer(vote.getTitle(), itemNames);
            vote.setBlockchainVoteId(blockchainVoteId);
        } catch (Exception e) {
            throw new RuntimeException("블록체인에 투표 등록 실패", e);
        }
    }

    @Transactional
    public void submitVote(Long voteId, VoteRequest request, String username) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔐 auth.getName(): " + auth.getName());
        System.out.println("🔐 authenticated: " + auth.isAuthenticated());
        System.out.println("🔐 username: " + username);
        System.out.println("❗ 중복 투표 발견 → ID: ...");  // ← 이게 찍히면 중복

        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표 없음"));

        if (vote.getBlockchainVoteId() == null) {
            throw new IllegalStateException("블록체인 투표 ID 없음");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        System.out.println("👤 user ID: " + user.getId());
        System.out.println("📦 vote ID: " + vote.getId());

        // ✅ 중복 검사 로그
        Optional<VoteResult> existing = voteResultRepository.findByUserIdAndVoteId(user.getId(), vote.getId());
        if (existing.isPresent()) {
            VoteResult vr = existing.get();
            System.out.println("❗ 중복 투표 발견 → VoteResult ID: " + vr.getId() +
                    ", user: " + vr.getUser().getId() + ", vote: " + vr.getVote().getId());
            throw new IllegalStateException("이미 참여한 투표입니다.");
        } else {
            System.out.println("✅ 중복 투표 아님 → 계속 진행");
        }

        VoteItem selectedItem = voteItemRepository.findById(request.getSelectedItemId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 항목이 존재하지 않습니다."));

        if (!selectedItem.getVote().getId().equals(voteId)) {
            throw new IllegalArgumentException("선택한 항목이 이 투표에 속하지 않습니다.");
        }


        // ✅ 투표 결과 DB 저장
        VoteResult voteResult = VoteResult.builder()
                .user(user)
                .vote(vote)
                .voteItem(selectedItem)
                .votedAt(LocalDateTime.now())
                .build();

        VoteResult savedResult = voteResultRepository.saveAndFlush(voteResult);
        System.out.println("✅ VoteResult 저장 완료 → ID: " + savedResult.getId());

        // ✅ 블록체인 인덱스 계산
        try {
            List<VoteItem> items = voteItemRepository.findByVoteIdOrderByIdAsc(voteId);
            int itemIndex = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(selectedItem.getId())) {
                    itemIndex = i;
                    break;
                }
            }

            if (itemIndex == -1) {
                throw new IllegalStateException("항목 인덱스를 찾을 수 없습니다.");
            }

            System.out.println("🧾 블록체인 투표 인덱스: " + itemIndex);
            blockchainVoteService.submitVoteAsServer(vote.getBlockchainVoteId(), BigInteger.valueOf(itemIndex));
            System.out.println("✅ 블록체인 투표 완료");
        } catch (Exception e) {
            System.err.println("⚠️ 블록체인 투표 실패: " + e.getMessage());
            // 블록체인 실패 시 DB 롤백 여부는 트랜잭션 정책에 따라 결정
            throw new RuntimeException("블록체인 트랜잭션 실패", e);
        }
    }




    /**
     * 투표 단건 조회 (항목 포함)
     */
    public VoteResponse getVoteById(Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 투표가 존재하지 않습니다."));

        List<VoteItem> items = voteItemRepository.findByVoteIdOrderByIdAsc(id);

        return VoteResponse.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .description(vote.getDescription())
                .deadline(vote.getDeadline())

                .createdAt(vote.getCreatedAt())
                .items(items.stream()
                        .map(item -> VoteResponse.Item.builder()
                                .itemId(item.getId())
                                .itemText(item.getItemText())
                                .description(item.getDescription())
                                .promise(item.getPromise())
                                .image(item.getImage() != null && !item.getImage().startsWith("data:")
                                    ? "data:image/png;base64," + item.getImage()
                                    :item.getImage())
                                .build())
                        .toList())
                .build();
    }

    /**
     * 전체 투표 목록 조회
     */
    public List<VoteResponse> getAllVotes() {
        return voteRepository.findAll().stream()
                .filter(vote -> !vote.isDeleted())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void moveToTrash(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투표가 존재하지 않습니다."));
        vote.setDeleted(true);
        voteRepository.save(vote);
    }

    @Transactional
    public void restoreFromTrash(Long voteId) {
        Vote vote = voteRepository.findByIdAndIsDeletedTrue(voteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 삭제된 투표가 존재하지 않습니다."));
        vote.setDeleted(false);
        voteRepository.save(vote);
    }

    @Transactional
    public void hardDeleteVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표 없음"));

        // 투표 결과 및 항목도 함께 삭제
        List<VoteItem> voteItems = voteItemRepository.findByVoteIdOrderByIdAsc(voteId);
        for (VoteItem item : voteItems) {
            voteResultRepository.deleteAll(voteResultRepository.findByVoteItemId(item.getId()));
        }
        voteItemRepository.deleteAll(voteItems);
        voteRepository.delete(vote);
    }

    public List<VoteResponse> getDeletedVotes() {
        return voteRepository.findAllByIsDeletedTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 내부 변환 로직: Vote 엔티티 → VoteResponse DTO
     */
    private VoteResponse toResponse(Vote vote) {
        List<VoteItem> items = voteItemRepository.findByVoteIdOrderByIdAsc(vote.getId());

        //  현재 시간이 마감일 이후면 true
        boolean isClosed = LocalDateTime.now().isAfter(vote.getDeadline());

        return VoteResponse.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .description(vote.getDescription())
                .deadline(vote.getDeadline())
                .isClosed(isClosed) //  여기서 실시간 계산된 값 사용
                .startTime(vote.getStartTime())  // 추가
                .createdAt(vote.getCreatedAt())
                .isPublic(vote.isPublic())
                .isDeleted(vote.isDeleted())
                .items(items.stream()
                        .map(item -> VoteResponse.Item.builder()
                                .itemId(item.getId())
                                .itemText(item.getItemText())
                                .description(item.getDescription())
                                .promise(item.getPromise())
                                .image(item.getImage() != null && !item.getImage().startsWith("data:")
                                        ? "data:image/png;base64," + item.getImage()
                                        :item.getImage())
                                .build())
                        .toList())
                .build();
    }
    public int countVotesByItem(Long voteId, Long itemId) {
        return voteResultRepository.countByVoteIdAndVoteItemId(voteId, itemId);
    }

    @Transactional
    public void togglePublicStatus(Long voteId) {   // 공개여부 서비스
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표 없음"));
        vote.setPublic(!vote.isPublic());
    }

    public Map<String, Object> getBlockchainVoteResult(String username, Long voteId) throws Exception {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표 없음"));

        if (vote.getBlockchainVoteId() == null) {
            throw new IllegalStateException("해당 투표는 블록체인에 생성되지 않았습니다.");
        }

        return blockchainVoteService.getVoteResultServer(vote.getBlockchainVoteId());
    }
    public List<VoteResponse> getAllVotesWithVotedFlag(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        return voteRepository.findAll().stream()
                .filter(vote -> !vote.isDeleted())
                .map(vote -> toResponseWithVotedFlag(vote, user))
                .collect(Collectors.toList());

    }
    private VoteResponse toResponseWithVotedFlag(Vote vote, User user) {
        List<VoteItem> items = voteItemRepository.findByVoteIdOrderByIdAsc(vote.getId());
        boolean isClosed = LocalDateTime.now().isAfter(vote.getDeadline());

        boolean voted = voteResultRepository
                .findByUserIdAndVoteId(user.getId(), vote.getId())
                .isPresent();

        return VoteResponse.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .description(vote.getDescription())
                .deadline(vote.getDeadline())
                .isClosed(isClosed)
                .startTime(vote.getStartTime())
                .createdAt(vote.getCreatedAt())
                .isPublic(vote.isPublic())
                .isDeleted(vote.isDeleted())
                .voted(voted) //
                .items(items.stream()
                        .map(item -> VoteResponse.Item.builder()
                                .itemId(item.getId())
                                .itemText(item.getItemText())
                                .description(item.getDescription())
                                .promise(item.getPromise())
                                .image(item.getImage() != null && !item.getImage().startsWith("data:")
                                        ? "data:image/png;base64," + item.getImage()
                                        : item.getImage())
                                .build())
                        .toList())
                .build();
    }
}
