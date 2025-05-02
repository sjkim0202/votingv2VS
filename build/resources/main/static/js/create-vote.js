const fallbackImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjE2MCIgdmlld0JveD0iMCAwIDEyMCAxNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEyMCIgaGVpZ2h0PSIxNjAiIGZpbGw9IiNlZWUiLz48dGV4dCB4PSI2MCIgeT0iODAiIGZpbGw9IiM3Nzc3NzciIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxMCI+Tm8gSW1hZ2U8L3RleHQ+PC9zdmc+';

let currentPromiseTarget = null;

function addItem() {
    const container = document.getElementById("items-container");
    const itemDiv = document.createElement("div");
    itemDiv.className = "vote-item";
    const id = Date.now();

    itemDiv.innerHTML = `
        <button type="button" class="remove-btn">삭제</button>
        <img id="preview-${id}" src="${fallbackImage}" alt="이미지 미리보기" />
        <div class="vote-info">
            <input type="text" class="item-text" placeholder="후보자 이름" required />
            <div class="description-wrapper">
                <textarea class="item-description" placeholder="설명 (선택)"></textarea>
                <button type="button" class="promise-btn">공약 ＋</button>
            </div>
            <input type="file" accept="image/*" class="image-input" data-preview="preview-${id}" />
        </div>
    `;

    itemDiv.querySelector(".remove-btn").addEventListener("click", () => {
        container.removeChild(itemDiv);
    });

    itemDiv.querySelector(".image-input").addEventListener("change", function () {
        const file = this.files[0];
        const previewId = this.dataset.preview;
        const previewEl = document.getElementById(previewId);

        if (file && previewEl) {
            const reader = new FileReader();
            reader.onload = function (e) {
                previewEl.src = e.target.result;
                previewEl.dataset.base64 = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    });

    container.appendChild(itemDiv);
}

document.addEventListener("click", function (e) {
    if (e.target.classList.contains("promise-btn")) {
        currentPromiseTarget = e.target;
        const textarea = currentPromiseTarget.closest(".description-wrapper").querySelector(".item-description");
        document.getElementById("promise-textarea").value = textarea.dataset.promise || "";
        document.getElementById("promise-modal").style.display = "block";
    }
});

document.getElementById("save-promise").addEventListener("click", function () {
    const text = document.getElementById("promise-textarea").value.trim();
    if (currentPromiseTarget) {
        const textarea = currentPromiseTarget.closest(".description-wrapper").querySelector(".item-description");
        textarea.dataset.promise = text;
        currentPromiseTarget.textContent = text ? "공약 ✔" : "공약 ＋";
    }
    document.getElementById("promise-modal").style.display = "none";
});

document.getElementById("cancel-promise").addEventListener("click", function () {
    document.getElementById("promise-modal").style.display = "none";
});

const token = localStorage.getItem("accessToken");

document.getElementById("vote-form").addEventListener("submit", async (e) => {
    e.preventDefault();

    const confirmed = confirm("정말 이 내용으로 투표를 생성하시겠습니까?");
    if (!confirmed) return;

    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const deadline = document.getElementById("deadline").value;
    const startTime = document.getElementById("startTime").value;

    const itemTexts = document.querySelectorAll(".item-text");
    const itemDescriptions = document.querySelectorAll(".item-description");
    const previews = document.querySelectorAll(".vote-item img");

    const items = [];

    for (let i = 0; i < itemTexts.length; i++) {
        const text = itemTexts[i].value.trim();
        const desc = itemDescriptions[i].value.trim();
        const promise = itemDescriptions[i].dataset.promise || "";
        const rawBase64 = (previews[i].dataset.base64 || "").split(",")[1];
        const imageData = rawBase64 || null;

        if (text) {
            items.push({ itemText: text, description: desc, promise: promise, image: imageData });
        }
    }

    if (!token) {
        document.getElementById('result').innerText = '⚠️ 로그인 정보가 없습니다. 다시 로그인 해주세요.';
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/api/votes", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ title, description, startTime, deadline, items })
        });

        if (response.ok) {
            alert('✅ 투표가 성공적으로 생성되었습니다!');
            window.location.href = 'vote-list.html';
        } else {
            alert('❌ 투표 생성 실패');
        }
    } catch (error) {
        console.error('투표 생성 중 오류:', error);
        alert('⚠️ 네트워크 오류');
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const startInput = document.getElementById("startTime");
    const deadlineInput = document.getElementById("deadline");

    function getKSTNowISO(offsetHours = 0) {
        const now = new Date();
        const kst = new Date(now.getTime() + (9 + offsetHours) * 60 * 60 * 1000);
        kst.setSeconds(0, 0);
        return kst.toISOString().slice(0, 16);
    }

    startInput.min = getKSTNowISO();

    startInput.addEventListener("change", function () {
        if (startInput.value) {
            deadlineInput.min = startInput.value;
        }
    });
});
