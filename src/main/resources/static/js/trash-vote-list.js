const accessToken = localStorage.getItem("accessToken");

function loadDeletedVotes() {
    fetch("https://votingv2-production.up.railway.app/api/votes/deleted", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + accessToken
        }
    })
        .then(res => res.json())
        .then(data => {
            const tbody = document.querySelector("#deletedTable tbody");
            tbody.innerHTML = "";
            data.forEach(vote => {
                const row = document.createElement("tr");
                row.innerHTML = `
                <td>${vote.id}</td>
                <td>${vote.title}</td>
                <td>${vote.description || "설명 없음"}</td>
                <td>
                    <button class="action-btn delete-btn" onclick="deletePermanently(${vote.id})">완전 삭제</button>
                    <button class="action-btn restore-btn" onclick="restoreVote(${vote.id})">복원</button>
                </td>
            `;
                tbody.appendChild(row);
            });
        })
        .catch(err => {
            alert("삭제된 투표 목록을 불러오는 데 실패했습니다.");
            console.error(err);
        });
}

function deletePermanently(id) {
    if (!confirm("정말 이 투표를 완전 삭제하시겠습니까? 복구할 수 없습니다.")) return;

    fetch(`https://votingv2-production.up.railway.app:8080/api/votes/${id}/force`, {
        method: "DELETE",
        headers: {
            "Authorization": "Bearer " + accessToken
        }
    })
        .then(res => {
            if (res.ok) {
                alert("완전 삭제 완료!");
                loadDeletedVotes();
            } else {
                alert("삭제 실패");
            }
        })
        .catch(err => {
            alert("⚠️ 서버 오류");
            console.error(err);
        });
}

function restoreVote(id) {
    if (!confirm("이 투표를 복원하시겠습니까?")) return;

    fetch(`https://votingv2-production.up.railway.app/api/votes/${id}/restore`, {
        method: "PATCH",
        headers: {
            "Authorization": "Bearer " + accessToken
        }
    })
        .then(res => {
            if (res.ok) {
                alert("복원 완료!");
                loadDeletedVotes();
            } else {
                alert("복원 실패");
            }
        })
        .catch(err => {
            alert("⚠️ 서버 오류");
            console.error(err);
        });
}

loadDeletedVotes();
