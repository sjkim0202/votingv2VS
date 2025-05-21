const params = new URLSearchParams(window.location.search);
const voteId = params.get("id");
const token = localStorage.getItem("accessToken");
const role = localStorage.getItem("role");  // ✅

const fallbackImage = 'data:image/svg+xml;base64,' + btoa(`
    <svg width="120" height="160" xmlns="https://www.w3.org/2000/svg">
        <rect width="120" height="160" fill="#eee"/>
        <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#777" font-size="12">No Image</text>
    </svg>
`);

// 투표 상세 로딩
if (!voteId) {
    alert("잘못된 접근입니다. (투표 ID 없음)");
} else {
    fetch(`https://votingv2-production-708e.up.railway.app/api/votes/${voteId}`, {
        headers: { Authorization: `Bearer ${token}` }
    })
        .then(res => {
            if (!res.ok) throw new Error("투표 상세 정보 조회 실패");
            return res.json();
        })
        .then(vote => {
            document.title = vote.title;
            document.getElementById("page-header").textContent = vote.title;

            const container = document.getElementById("vote-items");

            vote.items.forEach(item => {
                const imageSrc = item.image
                    ? (item.image.startsWith("data:") ? item.image : `data:image/png;base64,${item.image}`)
                    : fallbackImage;

                const promise = item.promise || "";
                const description = item.description || "";

                const div = document.createElement("div");
                div.className = "vote-item";
                div.innerHTML = `
                <img src="${imageSrc}" class="vote-image" alt="이미지" />
                <div class="vote-info">
                    <div class="title">[${item.itemText || "제목 없음"}]</div>
                    <div class="desc">${description}</div>
                </div>
                <div class="vote-right">
                    <button type="button" class="promise-btn">공약 ＋</button>
                    <input type="radio" name="selectedItemId" value="${item.itemId}" required />
                </div>
                <div class="promise" style="display: none;">${promise}</div>
            `;
                container.appendChild(div);
            });

            // ✅ admin일 때 투표하기 버튼, 라디오 버튼 제거
            if (role === "ADMIN") {
                const submitButton = document.querySelector('button[type="submit"]');
                if (submitButton) {
                    submitButton.style.display = "none";
                }
                document.querySelectorAll('.vote-right').forEach(rightDiv => {
                    const radio = rightDiv.querySelector('input[type="radio"]');
                    if (radio) {
                        radio.remove();
                    }
                });
            }
        })
        .catch(err => {
            console.error(err);
            alert("❌ 투표 정보를 불러오지 못했습니다.");
        });
}

// 투표 제출 처리
document.getElementById("vote-form").addEventListener("submit", async e => {
    e.preventDefault();

    const selected = document.querySelector('input[name="selectedItemId"]:checked');
    if (!selected) {
        alert("⚠️ 항목을 선택해주세요.");
        return;
    }

    const selectedTitle = selected.closest(".vote-item")?.querySelector(".title")?.innerText || "선택한 후보자";
    const confirmed = confirm(`${selectedTitle}\n이 후보자에게 투표하시겠습니까?`);
    if (!confirmed) return;

    // 🔍 디버깅용 로그
    console.log("🟡 accessToken:", token);
    console.log("🟢 투표 제출 요청", {
        url: `https://votingv2-production-708e.up.railway.app/api/votes/${voteId}/vote`,
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
        },
        body: { itemIndex: selected.value }
    });

    try {
        const res = await fetch(`https://votingv2-production-708e.up.railway.app/api/votes/${voteId}/vote`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({ itemIndex: selected.value })
        });

        if (res.ok) {
            alert("✅ 투표가 완료되었습니다!");
            window.location.href = "vote-list.html";
        } else {
            alert("❌ 이미 참여한 투표입니다!");
        }
    } catch (err) {
        console.error("투표 중 오류:", err);
        alert("⚠️ 네트워크 오류");
    }
});

// 공약 보기 버튼 이벤트
document.addEventListener("click", e => {
    if (e.target.classList.contains("promise-btn")) {
        const voteItem = e.target.closest(".vote-item");
        const img = voteItem.querySelector(".vote-image")?.src || fallbackImage;
        const promiseText = voteItem.querySelector(".promise")?.innerText || "";

        document.getElementById("promise-preview").src = img;
        document.getElementById("promise-text").innerText = promiseText;
        document.getElementById("promise-modal").style.display = "block";
    }
});

// 공약 모달 닫기
document.getElementById("close-promise").addEventListener("click", () => {
    document.getElementById("promise-modal").style.display = "none";
});
