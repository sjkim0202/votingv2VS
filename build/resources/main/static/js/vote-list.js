const accessToken = localStorage.getItem("accessToken");
const role = localStorage.getItem("role");

function logout() {
    const confirmLogout = confirm("로그아웃 하시겠습니까?");
    if (!confirmLogout) return;
    window.location.href = "index.html";
    localStorage.removeItem("accessToken");
    localStorage.removeItem("role");


}

function loadVotes() {
    const loading = document.getElementById("loading");
    if (loading) loading.style.display = "block";

    fetch("https://kksl-voting.up.railway.app/api/votes", {
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

            // ✅ 모든 버튼 렌더링 완료 후 표시 (권한 따라 다르게 표시)
            document.querySelectorAll(".vote-card .actions, .action-btn").forEach(btn => {
                btn.style.display = "inline-block";
            });

            if (role === "ADMIN" || role === "DEVELOP") {
                document.querySelectorAll(".create-btn, .trash-btn").forEach(btn => {
                    btn.style.display = "inline-block";
                });
            } else {
                document.querySelectorAll(".create-btn, .trash-btn").forEach(btn => {
                    btn.style.display = "none";
                });
            }


            if (loading) loading.style.display = "none";
        })
        .catch(error => {
            console.error("오류:", error);
            alert("투표 목록을 불러오지 못했습니다.");
            if (loading) loading.style.display = "none";
        });
}

function renderCard(vote, container, isStarted) {
    const card = document.createElement("div");
    card.className = "vote-card";

    const status = vote.closed ? "마감" : isStarted ? "진행 중" : "시작 전";

    let actionButton = "";

    if (vote.closed) {
        actionButton = `<button class="result-btn action-btn" style="display:none" onclick="location.href='vote-result.html?id=${vote.id}'">결과 보기</button>`;
    } else if (vote.voted) {
        actionButton = `<button class="voted-btn action-btn" disabled style="display:none">투표 완료</button>`;
    } else {
        actionButton = `<button class="vote-btn action-btn" style="display:none" onclick="${isStarted ? `location.href='vote-detail.html?id=${vote.id}'` : `alert('투표 시작 전입니다.')`}">투표하기</button>`;
    }

    card.innerHTML = `
        <div class="title">${vote.title}</div>
        <div class="desc">설명: ${vote.description}</div>
        <div class="desc">기간: ${vote.startTime.slice(0, 16)} ~ ${vote.deadline.slice(0, 16)}</div>
        <div class="desc">상태: ${status}</div>
        <div class="actions" style="display:none">${actionButton}</div>
    `;

    container.appendChild(card);
}

function renderOpenVote(vote, isStarted) {
    const tbody = document.querySelector("#voteTable tbody");
    const row = document.createElement("tr");
    const titleCell = `<span>${vote.title}</span>`;

    let controlButtons = "";

    // 관리자 또는 개발자: 공개/비공개 전환 버튼
    if (role === "ADMIN" || role === "DEVELOP") {
        const toggleClass = vote.public ? "toggle-public" : "toggle-private";
        const toggleLabel = vote.public ? "비공개로 전환" : "공개로 전환";

        controlButtons += `
            <button class="action-btn ${toggleClass}" style="display:none" onclick="togglePublic(${vote.id}, this)">${toggleLabel}</button>
        `;
    }

    // 관리자 권한: 후보자 보기 + 삭제
    if (role === "ADMIN") {
        controlButtons += `
            <button class="action-btn view-btn" style="display:none" onclick="location.href='vote-detail.html?id=${vote.id}'">후보자 보기</button>
            <button class="action-btn delete-btn" style="display:none" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    }
    // 개발자 권한: 프리뷰 및 투표, 삭제
    else if (role === "DEVELOP") {
        if (vote.closed) {
            controlButtons += `
                <button class="action-btn vote-btn" style="display:none" onclick="location.href='vote-detail.html?id=${vote.id}'">투표하기<br>(마감됨)</button>
            `;
        } else {
            controlButtons += `
                <button class="action-btn preview-btn" style="display:none" onclick="location.href='vote-result.html?id=${vote.id}&preview=true'">미리<br>결과 보기</button>
            `;
            if (vote.voted) {
                controlButtons += `
                    <button class="action-btn voted-btn" style="display:none" disabled>투표 완료</button>
                `;
            } else {
                controlButtons += `
                    <button class="action-btn vote-btn" style="display:none" onclick="${isStarted ? `location.href='vote-detail.html?id=${vote.id}'` : `alert('투표 시작 전입니다.')`}">투표하기</button>
                `;
            }
        }

        controlButtons += `
            <button class="action-btn delete-btn" style="display:none" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    }
    // 일반 사용자: 투표 or 투표 완료
    else {
        if (vote.voted) {
            controlButtons += `
                <button class="action-btn voted-btn" style="display:none" disabled>투표 완료</button>
            `;
        } else {
            controlButtons += `
                <button class="action-btn vote-btn" style="display:none" onclick="${isStarted ? `location.href='vote-detail.html?id=${vote.id}'` : `alert('투표 시작 전입니다.')`}">투표하기</button>
            `;
        }
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
            <button class="action-btn ${toggleClass}" style="display:none" onclick="togglePublic(${vote.id}, this)">${toggleLabel}</button>
        `;
    }

    controlButtons += `
        <button class="action-btn result-btn" style="display:none" onclick="location.href='vote-result.html?id=${vote.id}'">결과 보기</button>
    `;

    if (role === "DEVELOP") {
        controlButtons += `
            <button class="action-btn voted-btn" style="display:none" onclick="location.href='vote-detail.html?id=${vote.id}'">투표하기<br>(마감됨)</button>
            <button class="action-btn delete-btn" style="display:none" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    } else if (role === "ADMIN") {
        controlButtons += `
            <button class="action-btn delete-btn" style="display:none" onclick="moveToTrash(${vote.id})">휴지통</button>
        `;
    }

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
    fetch(`https://kksl-voting.up.railway.app/api/votes/${voteId}/toggle-public`, {
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

    fetch(`https://kksl-voting.up.railway.app/api/votes/${voteId}/trash`, {
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

document.addEventListener("DOMContentLoaded", () => {
    const username = localStorage.getItem("username");
    if (username) {
        const userInfoDiv = document.getElementById("userInfo");
        userInfoDiv.textContent = username;
    }
});