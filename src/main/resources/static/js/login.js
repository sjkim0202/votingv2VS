// 로그인 폼이 제출되었을 때 실행되는 함수 등록
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault(); // 기본 폼 제출 동작(새로고침)을 막음

    // 사용자 입력값 가져오기
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        // 로그인 API 요청 보내기
        const response = await fetch('https://votingv2-backend.up.railway.app/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'  // JSON 형식으로 전송
            },
            body: JSON.stringify({ username, password }) // 로그인 정보 전송
        });

        // 응답 처리
        if (response.ok) {
            const data = await response.json(); // 응답 본문(JSON) 파싱

            // 토큰 저장 (이후 API 호출 시 사용 가능)
            localStorage.setItem('accessToken', data.accessToken);

            // 결과 출력
            document.getElementById('result').innerText = `✅ 로그인 성공!`;

            // 👉 로그인 성공 시 투표 목록 페이지로 이동
            window.location.href = 'vote-list.html';  // 원하는 페이지로 변경 가능
        } else {
            // 로그인 실패 시 메시지 출력
            document.getElementById('result').innerText = '❌ 로그인 실패 (아이디 또는 비밀번호 오류)';
        }
    } catch (error) {
        console.error('로그인 요청 중 오류 발생:', error);
        document.getElementById('result').innerText = '⚠️ 로그인 요청 실패';
    }
});
