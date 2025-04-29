package com.example.votingv2.service;

import com.example.votingv2.blockchain.BlockchainVoteService;
import com.example.votingv2.dto.VoteRequest;
import com.example.votingv2.dto.VoteResponse;
import com.example.votingv2.dto.VoteResultResponseDto;
import com.example.votingv2.entity.*;
import com.example.votingv2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 투표 생성, 조회, 사용자 투표 제출 및 삭제를 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final VoteItemRepository voteItemRepository;
    private final VoteResultRepository voteResultRepository;
    private final BlockchainVoteService blockchainVoteService;

    /**
     * 투표 생성 처리
     */
    public VoteResponse createVote(VoteRequest request) {
        User admin = userRepository.findByUsername("admin1")
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정이 존재하지 않습니다."));

        Vote vote = Vote.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .createdBy(admin)
                .isPublic(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .startTime(request.getStartTime())  // 추가
                .build();

        Vote savedVote = voteRepository.save(vote);

        if (request.getItems() != null) {
            List<VoteItem> items = request.getItems().stream()
                    .map(itemReq -> VoteItem.builder()
                            .vote(savedVote)
                            .itemText(itemReq.getItemText())
                            .description(itemReq.getDescription())
                            .promise(itemReq.getPromise())
                            .image(itemReq.getImage() != null && !itemReq.getImage().startsWith("data:")
                                    ? "data:image/png;base64," + itemReq.getImage()
                                    : itemReq.getImage())
                            .build())
                    .toList();
            voteItemRepository.saveAll(items);
        }
        // ✅ 블록체인에도 투표 생성
        try {
            List<String> itemTexts = request.getItems().stream()
                    .map(VoteRequest.VoteItemRequest::getItemText)
                    .toList();
            blockchainVoteService.createVote(request.getTitle(), itemTexts);
        } catch (Exception e) {
            System.err.println("⚠️ 블록체인 투표 생성 실패: " + e.getMessage());
        }

        return toResponse(savedVote);
    }

    /**
     * 사용자 투표 제출 처리
     */
    public void submitVote(Long voteId, VoteRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표 없음"));

        VoteItem selectedItem = voteItemRepository.findById(request.getSelectedItemId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 항목 없음"));

        // 중복 투표 방지
        if (voteResultRepository.findByUserIdAndVoteId(user.getId(), voteId).isPresent()) {
            throw new IllegalStateException("이미 투표하셨습니다.");
        }

        VoteResult result = VoteResult.builder()
                .user(user)
                .vote(vote)
                .voteItem(selectedItem)
                .votedAt(LocalDateTime.now())
                .build();

        voteResultRepository.save(result);
        // ✅ 블록체인에도 투표 기록
        try {
            // 1. 해당 투표에 속한 항목들 순서대로 가져오기
            List<VoteItem> items = voteItemRepository.findByVoteIdOrderByIdAsc(voteId);

            // 2. 선택된 항목의 인덱스 계산
            int itemIndex = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(selectedItem.getId())) {
                    itemIndex = i;
                    break;
                }
            }

            System.out.println("🧾 itemIndex: " + itemIndex);
            System.out.println("🧾 items.size(): " + items.size());

            if (itemIndex == -1) {
                throw new IllegalStateException("항목 인덱스를 찾을 수 없습니다.");
            }

            // 3. 블록체인 submit 호출
            blockchainVoteService.submitVote(
                    BigInteger.valueOf(voteId),
                    BigInteger.valueOf(itemIndex)
            );
        } catch (Exception e) {
            System.err.println("⚠️ 블록체인 투표 제출 실패: " + e.getMessage());
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


    @Transactional //블록체인 결과조회
    public VoteResultResponseDto getBlockchainResult(Long voteId) throws Exception {
        Map<String, Object> result = blockchainVoteService.getVoteResult(BigInteger.valueOf(voteId));

        @SuppressWarnings("unchecked")
        List<String> items = (List<String>) result.get("items");

        @SuppressWarnings("unchecked")
        List<BigInteger> counts = (List<BigInteger>) result.get("counts");

        String title = (String) result.get("title");

        return new VoteResultResponseDto(title, items, counts);
    }


}
