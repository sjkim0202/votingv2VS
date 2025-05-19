const accessToken = localStorage.getItem("accessToken");
const role = localStorage.getItem("role");

function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("role");
    alert("로그아웃 되었습니다.");
    window.location.href = "index.html";
}

function loadVotes() {
    fetch("https://votingv2-production-708e.up.railway.app/api/votes", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + accessToken
        }
    })
        .then(response => response.json())
        .then(data => {
            const tbody = document.querySelector("#voteTable tbody");
            const endedBody = document.querySelector("#endedVoteTable tbody");
            const voteCards = document.getElementById("voteCards");
            const endedCards = document.getElementById("endedVoteCards");

            tbody.innerHTML = "";
            endedBody.innerHTML = "";

            if (role !== "DEVELOP") {
                voteCards.innerHTML = "";
                endedCards.innerHTML = "";
            }

            data.sort((a, b) => (a.public === b.public ? 0 : a.public ? 1 : -1));

            if (role !== "ADMIN" && role !== "DEVELOP") {
                data = data.filter(vote => vote.public);
            }

            data.forEach(vote => {
                const now = new Date();
                const start = new Date(vote.startTime);
                const isStarted = now >= start;

                if (vote.closed) {
                    renderClosedVote(vote);
                    if (role !== "DEVELOP") {
                        renderCard(vote, endedCards, isStarted);
                    }
                } else {
                    renderOpenVote(vote, isStarted);
                    if (role !== "DEVELOP") {
                        renderCard(vote, voteCards, isStarted);
                    }
                }
            });

            if (role !== "ADMIN" && role !== "DEVELOP") {
                document.querySelectorAll(".admin-only").forEach(el => el.style.display = "none");
            }
        })
        .catch(error => {
            console.error("오류:", error);
            alert("투표 목록을 불러오지 못했습니다.");
        });
}

function renderCard(vote, container, isStarted) {
    const card = document.createElement("div");
    card.className = "vote-card";

    const status = vote.closed ? "마감" : isStarted ? "진행 중" : "시작 전";

    let actionButton = "";

    if (vote.closed) {
        actionButton = `<button class="result-btn" onclick="location.href='vote-result.html?id=${vote.id}'">결과 보기</button>`;
    } else {
        actionButton = `<button class="vote-btn" onclick="${isStarted ? `location.href='vote-detail.html?id=${vote.id}'` : `alert('투표 시작 전입니다.')`}">투표하기</button>`;
    }

    card.innerHTML = `
        <div class="title">${vote.title}</div>
        <div class="desc">설명: ${vote.description}</div>
        <div class="desc">기간: ${vote.startTime.slice(0, 16)} ~ ${vote.deadline.slice(0, 16)}</div>
        <div class="desc">상태: ${status}</div>
        <div class="actions">${actionButton}</div>
    `;

    container.appendChild(card);
}

function renderOpenVote(vote, isStarted) {
    const tbody = document.querySelector("#voteTable tbody");
    const row = document.createElement("tr");
    const titleCell = `<span>${vote.title}</span>`;

    let controlButtons = "";

    if (role === "ADMIN" || role === "DEVELOP") {
        const toggleClass = vote.public ? "toggle-public" : "toggle-private";
        const toggleLabel = vote.public ? "비공개로 전환" : "공개로 전환";

        controlButtons += `
            <button class="action-btn ${toggleClass}" onclick="togglePublic(${vote.id}, this)">${toggleLabel}</button>
        `;
    }

    if (role === "ADMIN") {
        controlButtons += `
            <button class="action-btn view-btn" onclick="location.href='vote-detail.html?id=${vote.id}'">후보자 보기</button>
            <button class="action-btn delete-btn" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    } else if (role === "DEVELOP") {
        if (vote.closed) {
            controlButtons += `
                <button class="action-btn vote-btn" onclick="location.href='vote-detail.html?id=${vote.id}'">투표하기<br>(마감됨)</button>
            `;
        } else {
            controlButtons += `
                <button class="action-btn preview-btn" onclick="location.href='vote-result.html?id=${vote.id}&preview=true'">미리<br>결과 보기</button>
                <button class="action-btn vote-btn" onclick="${isStarted ? `location.href='vote-detail.html?id=${vote.id}'` : `alert('투표 시작 전입니다.')`}">투표하기</button>
            `;
        }
        // 개발자는 휴지통 버튼을 맨 마지막에 추가
        controlButtons += `
            <button class="action-btn delete-btn" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    } else {
        // 일반 유저는 절대 휴지통 버튼 없음!
        controlButtons += `
            <button class="action-btn vote-btn" onclick="${isStarted ? `location.href='vote-detail.html?id=${vote.id}'` : `alert('투표 시작 전입니다.')`}">투표하기</button>
        `;
    }

    row.innerHTML = `
        <td>${vote.id}</td>
        <td>${titleCell}</td>
        <td>${vote.description}</td>
        <td>${vote.startTime.replace("T", " ").substring(0, 16)}</td>
        <td>${vote.deadline.replace("T", " ").substring(0, 16)}</td>
        <td>${vote.closed ? "마감" : isStarted ? "진행 중" : "시작 전"}</td>
        <td>${controlButtons}</td>
    `;

    tbody.appendChild(row);
}

function renderClosedVote(vote) {
    const endedBody = document.querySelector("#endedVoteTable tbody");
    const row = document.createElement("tr");
    const titleCell = `<span>${vote.title}</span>`;

    let controlButtons = "";

    if (role === "ADMIN" || role === "DEVELOP") {
        const toggleClass = vote.public ? "toggle-public" : "toggle-private";
        const toggleLabel = vote.public ? "비공개로 전환" : "공개로 전환";

        controlButtons += `
            <button class="action-btn ${toggleClass}" onclick="togglePublic(${vote.id}, this)">${toggleLabel}</button>
        `;
    }

    controlButtons += `
        <button class="action-btn result-btn" onclick="location.href='vote-result.html?id=${vote.id}'">결과 보기</button>
    `;

    if (role === "DEVELOP") {
        controlButtons += `
            <button class="action-btn voted-btn" onclick="location.href='vote-detail.html?id=${vote.id}'">투표하기<br>(마감됨)</button>
            <button class="action-btn delete-btn" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    } else if (role === "ADMIN") {
        controlButtons += `
            <button class="action-btn delete-btn" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    }
    // 유저는 여기서도 휴지통 안 보임!

    row.innerHTML = `
        <td>${vote.id}</td>
        <td>${titleCell}</td>
        <td>${vote.description || '설명이 없습니다.'}</td>
        <td>${vote.startTime.replace("T", " ").substring(0, 16)}</td>
        <td>${vote.deadline.replace("T", " ").substring(0, 16)}</td>
        <td>마감</td>
        <td>${controlButtons}</td>
    `;

    endedBody.appendChild(row);
}

function togglePublic(voteId, btn) {
    fetch(`https://votingv2-production-708e.up.railway.app/api/votes/${voteId}/toggle-public`, {
        method: "PATCH",
        headers: {
            "Authorization": "Bearer " + accessToken
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("전환 실패");
            return res.text();
        })
        .then(() => {
            const isCurrentlyPublic = btn.classList.contains("toggle-public");

            if (isCurrentlyPublic) {
                btn.innerText = "공개로 전환";
                btn.classList.remove("toggle-public");
                btn.classList.add("toggle-private");
            } else {
                btn.innerText = "비공개로 전환";
                btn.classList.remove("toggle-private");
                btn.classList.add("toggle-public");
            }
        })
        .catch(err => {
            alert("⚠️ 공개 상태 전환 실패: " + err.message);
        });
}

function moveToTrash(voteId) {
    if (!confirm("이 투표를 휴지통으로 이동하시겠습니까?")) return;

    fetch(`https://votingv2-production-708e.up.railway.app/api/votes/${voteId}/trash`, {
        method: "PATCH",
        headers: {
            "Authorization": "Bearer " + accessToken
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("이동 실패");
            alert("휴지통으로 이동되었습니다.");
            loadVotes();
        })
        .catch(err => {
            alert("⚠️ 이동 실패: " + err.message);
        });
}

loadVotes();


// ✅ 관리자면 투표 생성 버튼 보여주기
if (role === "ADMIN" || role === "DEVELOP") {
    document.querySelectorAll(".admin-only").forEach(el => el.style.display = "inline-block");
}

