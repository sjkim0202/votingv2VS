document.getElementById("login-form").addEventListener("submit", function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch("https://kksl-voting.up.railway.app/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, password })
    })
        .then(response => {
            if (!response.ok) throw new Error("로그인 실패");
            return response.json();
        })
        .then(data => {
            // 로그인 성공 시 페이드아웃
            document.body.classList.remove("fade-in");
            document.body.style.opacity = "0";

            // 페이드아웃 완료 후 페이지 이동
            setTimeout(() => {
                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("username", data.username);
                localStorage.setItem("role", data.role);
                window.location.href = "vote-list.html"; // ✅ 이동 페이지
            }, 400);
        })
        .catch(error => {
            document.getElementById("result").innerText = "❌ 로그인 실패: " + error.message;
        });
});

// 페이지 로드 시 페이드인
window.addEventListener("DOMContentLoaded", () => {
    document.body.classList.add("fade-in");
});