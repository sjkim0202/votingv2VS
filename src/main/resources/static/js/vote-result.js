// URL 쿼리스트링에서 투표 ID 추출
const params = new URLSearchParams(location.search);
const voteId = params.get("id");

// 로컬 스토리지에서 accessToken 가져오기
const token = localStorage.getItem("accessToken");

// 기본(fallback) 이미지 설정 (후보자 이미지가 없는 경우 사용)
const fallbackImage =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgODAgMTAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxyZWN0IHdpZHRoPSI4MCIgaGVpZ2h0PSIxMDAiIGZpbGw9IiNlZWUiLz4KICA8dGV4dCB4PSI0MCIgeT0iNTAiIGZpbGw9IiM3Nzc3NzciIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZvbnQtc2l6ZT0iMTIiIGZvbnQtZmFtaWx5PSJBcmlhbCI+Tm8gSW1hZ2U8L3RleHQ+Cjwvc3ZnPg==";

// 투표 결과 불러오는 비동기 함수
async function loadResults() {
    try {
        // 1. DB 기준 투표 결과 가져오기
        const voteRes = await fetch(`https://kksl-voting.up.railway.app/api/votes/${voteId}`, {
            headers: { Authorization: "Bearer " + token },
        });
        const vote = await voteRes.json();

        // 투표 제목 표시
        document.getElementById("vote-title").innerText = vote.title;

        // 후보자 리스트 컨테이너
        const container = document.getElementById("candidate-list");

        // 차트 색상 팔레트
        const colorPalette = [
            "#4caf50",  // 초록
            "#3f51b5",  // 남색
            "#ff9800",  // 주황
            "#e91e63",  // 진분홍
            "#9c27b0",  // 보라
            "#03a9f4",  // 하늘색
            "#8bc34a",  // 연초록
            "#f44336",  // 빨강
            "#00bcd4",  // 청록
            "#cddc39",  // 라임
            "#ff5722",  // 딥오렌지
            "#795548",  // 브라운
            "#607d8b",  // 블루그레이
            "#ffc107",  // 밝은노랑
            "#673ab7"   // 인디고보라
        ];

        const voteCounts = []; // 후보별 득표수 저장
        let totalVotes = 0;     // 전체 득표수 합계

        // 후보별 득표수 가져오기
        for (let item of vote.items) {
            const countRes = await fetch(
                `https://kksl-voting.up.railway.app/api/votes/${voteId}/items/${item.itemId}/count`,
                { headers: { Authorization: "Bearer " + token } }
            );
            const count = parseInt(await countRes.text(), 10);
            voteCounts.push({ item, count });
            totalVotes += count;
        }

        // 2. 후보자 카드 생성하여 화면에 추가
        voteCounts.forEach(({ item, count }) => {
            const div = document.createElement("div");
            div.className = "candidate";
            div.innerHTML = `
        <img src="${item.image || fallbackImage}" alt="이미지">
        <div><strong>${item.itemText}</strong></div>
        <div>${count}표</div>
      `;
            container.appendChild(div);
        });

        // 3. DB 기준 도넛 차트 생성
        if (voteCounts.length === 0) {
            // 데이터가 없을 때 표시
            document.getElementById("db-empty").innerText = "조회된 데이터 없음";
        } else {
            // 데이터가 있을 때 차트 생성
            const ctx = document.getElementById("voteChart").getContext("2d");
            new Chart(ctx, {
                type: "doughnut",
                data: {
                    labels: voteCounts.map(v => v.item.itemText),
                    datasets: [{
                        data: voteCounts.map(v => v.count),
                        backgroundColor: colorPalette,
                        borderWidth: 1
                    }]
                },
                options: {
                    plugins: {
                        legend: { position: 'bottom' } // 범례를 하단에 표시
                    }
                }
            });
        }

        // 4. 블록체인 기준 투표 결과 가져오기
        const blockchainRes = await fetch(
            `https://kksl-voting.up.railway.app/api/votes/${voteId}/results/blockchain`,
            { headers: { Authorization: "Bearer " + token } }
        );
        const blockchainVote = await blockchainRes.json();

        // 5. 블록체인 기준 도넛 차트 생성
        if (!blockchainVote.items || blockchainVote.items.length === 0) {
            // 블록체인 데이터가 없을 때
            document.getElementById("blockchain-empty").innerText = "조회된 데이터 없음";
        } else {
            // 블록체인 데이터가 있을 때 차트 생성
            const blockchainCtx = document.getElementById("blockchainChart").getContext("2d");
            new Chart(blockchainCtx, {
                type: "doughnut",
                data: {
                    labels: blockchainVote.items, // 후보명 리스트
                    datasets: [{
                        data: blockchainVote.counts, // 각 후보 득표수 리스트
                        backgroundColor: [
                            "#f48fb1",  // 연분홍
                            "#90caf9",  // 연하늘
                            "#c5e1a5",  // 연연두
                            "#ffe082",  // 연노랑
                            "#ce93d8",  // 연보라
                            "#80cbc4",  // 연청록
                            "#a5d6a7",  // 파스텔그린
                            "#ffab91",  // 피치
                            "#b39ddb",  // 라벤더
                            "#ffcc80",  // 라이트오렌지
                            "#ffecb3",  // 베이지옐로우
                            "#b0bec5",  // 라이트그레이블루
                            "#d7ccc8",  // 그레이브라운
                            "#ffe0b2",  // 크림오렌지
                            "#d1c4e9"   // 은은한 연보라
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    plugins: {
                        legend: { position: 'bottom' }
                    }
                }
            });
        }
    } catch (error) {
        // 에러 처리: 콘솔 출력 및 사용자에게 알림
        console.error(error);
        alert("투표 결과를 불러오는 중 오류가 발생했습니다.");
    }
}

// 페이지 로딩 시 실행
loadResults();