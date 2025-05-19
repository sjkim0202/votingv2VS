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
   → 사용자 투표 제출 (중복 방지)

3. getVoteResult(voteId)
   → 항목별 득표 수 결과 확인

🔔 로그 이벤트 포함:
- VoteCreated: 투표 생성시 발생
- VoteSubmitted: 사용자 투표시 발생
===============================================
*/


contract Vote {

    // 🔔 투표 생성 시 발생하는 이벤트
    event VoteCreated(uint indexed voteId, string title);

    // 🔔 투표 제출 시 발생하는 이벤트
    event VoteSubmitted(uint indexed voteId, uint indexed itemIndex, address voter);

    // ✅ 항목 구조: 이름 + 득표 수
    struct VoteItem {
        string name;      // 항목 이름
        uint voteCount;   // 이 항목에 받은 득표 수
    }

    // ✅ 하나의 투표에 대한 구조
    struct VoteData {
        string title;                       // 투표 제목
        mapping(uint => VoteItem) items;    // 항목 목록 (인덱스 기반)
        uint itemCount;                     // 항목 개수
        mapping(address => bool) hasVoted;  // 중복 투표 방지용
    }

    // 🔧 전체 투표 저장소: voteId → Vote
    mapping(uint => VoteData) public voteMap;

    // 📌 다음 투표 ID (1부터 시작)
    uint public nextVoteId = 1;

    // ✅ 투표 생성 함수
    // title: 투표 제목
    // _itemNames: 투표 항목 이름들 (문자열 배열)
    function createVote(string memory _title, string[] memory _itemNames) public {
        VoteData storage v = voteMap[nextVoteId];   // 현재 voteId에 해당하는 공간 확보
        v.title = _title;
        v.itemCount = _itemNames.length;

        // 항목들을 순회하며 등록
        for (uint i = 0; i < _itemNames.length; i++) {
            v.items[i] = VoteItem(_itemNames[i], 0);
        }

        emit VoteCreated(nextVoteId, _title);   // 🔔 이벤트 로그 발생

        nextVoteId++;
    }

    // ✅ 투표 제출 함수
    // _voteId: 투표 ID
    // _itemIndex: 선택한 항목의 인덱스
    function submitVote(uint _voteId, uint _itemIndex) public {
        VoteData storage v = voteMap[_voteId];

        require(!v.hasVoted[msg.sender], "aaaa");         // 중복 방지
        require(_itemIndex < v.itemCount, "bbbb");   // 범위 체크

        v.items[_itemIndex].voteCount++;         // 선택한 항목의 득표 수 증가
        v.hasVoted[msg.sender] = true;           // 이 주소는 이미 투표함

        emit VoteSubmitted(_voteId, _itemIndex, msg.sender);
    }
    // ✅ 투표 결과 확인 함수
    // return: 제목, 항목 이름 배열, 득표 수 배열
    function getVoteResult(uint _voteId) public view returns (
        string memory title,
        string[] memory itemNames,
        uint[] memory voteCounts
    ) {
        VoteData storage v = voteMap[_voteId];
        title = v.title;

        itemNames = new string[](v.itemCount);   // 항목 이름들 저장할 배열
        voteCounts = new uint[](v.itemCount);    // 득표 수 저장할 배열

        for (uint i = 0; i < v.itemCount; i++) {
            itemNames[i] = v.items[i].name;
            voteCounts[i] = v.items[i].voteCount;
        }
    }
}
