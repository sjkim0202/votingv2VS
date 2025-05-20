// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/*
===============================================
🗳️ 블록체인 기반 투표 스마트컨트랙트
-----------------------------------------------
✅ 기능 요약
1. createVote(title, itemNames)
   → 투표 생성 (제목 + 항목들)

2. submitVote(voteId, itemIndex)
   → 사용자 투표 제출 (중복 방지 서버 측 처리)

3. getVoteResult(voteId, itemIndex)
   → 항목별 득표 수 결과 확인

🔔 로그 이벤트 포함:
- VoteCreated: 투표 생성시 발생
- VoteSubmitted: 사용자 투표시 발생
===============================================
*/

contract Vote {
    // 🔔 투표 생성 시 발생하는 이벤트
    event VoteCreated(uint indexed voteId, string title);

    // 🔔 투표 제출 시 발생하는 이벤트 (사용자 주소는 생략됨)
    event VoteSubmitted(uint indexed voteId, uint indexed itemIndex);

    // ✅ 항목 구조: 이름 + 득표 수
    struct VoteItem {
        string name;
        uint voteCount;
    }

    // ✅ 하나의 투표에 대한 구조
    struct VoteData {
        string title;
        mapping(uint => VoteItem) items;
        uint itemCount;
        bool exists;
    }

    // 🔧 전체 투표 저장소: voteId → Vote
    mapping(uint => VoteData) public voteMap;

    // 📌 다음 투표 ID (0부터 시작)
    uint public voteCounter;

    // ✅ 투표 생성 함수
    function createVote(string memory _title, string[] memory _itemNames) public {
        voteMap[voteCounter].title = _title;
        voteMap[voteCounter].itemCount = _itemNames.length;
        voteMap[voteCounter].exists = true;

        for (uint i = 0; i < _itemNames.length; i++) {
            voteMap[voteCounter].items[i] = VoteItem(_itemNames[i], 0);
        }

        emit VoteCreated(voteCounter, _title);
        voteCounter++;
    }

    // ✅ 투표 제출 함수 (msg.sender 없이, 서버가 모든 트랜잭션을 보냄)
    function submitVote(uint _voteId, uint _itemIndex) public {
        VoteData storage v = voteMap[_voteId];
        // 유효한 투표인지 확인
        require(v.exists, "vote does not exist");
        // 유효한 항목 인덱스인지 확인
        require(_itemIndex < v.itemCount, "invalid item index");
        // 선택한 항목의 득표 수 증가
        v.items[_itemIndex].voteCount++;
        // 이벤트 로그 (사용자 주소 없음)
        emit VoteSubmitted(_voteId, _itemIndex);
    }

    // ✅ 득표 수 조회 함수
    function getVoteResult(uint _voteId, uint _itemIndex) public view returns (uint) {
        return voteMap[_voteId].items[_itemIndex].voteCount;
    }

    function getAllVoteResults(uint _voteId) public view returns (
        string memory title,
        string[] memory itemNames,
        uint[] memory counts
    ) {
        VoteData storage v = voteMap[_voteId];
        require(v.exists, "vote does not exist");

        string[] memory names = new string[](v.itemCount);
        uint[] memory resultCounts = new uint[](v.itemCount);

        for (uint i = 0; i < v.itemCount; i++) {
            names[i] = v.items[i].name;
            resultCounts[i] = v.items[i].voteCount;
        }

        return (v.title, names, resultCounts);
    }

}
