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
                        .build())
                .collect(Collectors.toList());

        voteItemRepository.saveAll(voteItems);

        // 2. Blockchain에 등록 (서버 지갑 사용)
        try {
            List<String> itemNames = voteItems.stream()
                    .map(VoteItem::getItemText)
                    .collect(Collectors.toList());

            BigInteger blockchainVoteId = blockchainVoteService.createVoteAsServer(vote.getTitle(), itemNames);
            vote.setBlockchainVoteId(blockchainVoteId);
        } catch (Exception e) {
            throw new RuntimeException("블록체인에 투표 등록 실패", e);
        }
    }

    @Transactional
    public void submitVote(Long voteId, int itemIndex, String username) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("투표 없음"));

        if (vote.getBlockchainVoteId() == null) {
            throw new IllegalStateException("블록체인 투표 ID 없음");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        List<VoteItem> items = voteItemRepository.findByVote(vote);
        if (itemIndex < 0 || itemIndex >= items.size()) {
            throw new IllegalArgumentException("항목 인덱스 오류");
        }

        VoteItem selectedItem = items.get(itemIndex);

        // DB 저장
        VoteResult voteResult = VoteResult.builder()
                .user(user)
                .vote(vote)
                .voteItem(selectedItem)
                .votedAt(LocalDateTime.now())
                .build();
        voteResultRepository.save(voteResult);


        try {
            blockchainVoteService.submitVoteAsServer(vote.getBlockchainVoteId(), BigInteger.valueOf(itemIndex));
        } catch (Exception e) {
            throw new RuntimeException("투표 실패", e);
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



}
