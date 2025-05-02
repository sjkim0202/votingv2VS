// scripts/deploy.js

// Hardhat에서 ethers 라이브러리 사용
const hre = require("hardhat");

async function main() {
    // Vote.sol 스마트컨트랙트 가져오기
    const Vote = await hre.ethers.getContractFactory("Vote");

    // 배포 요청 (생성자 없음 → 인자 없음)
    const vote = await Vote.deploy();

    // 배포 완료까지 기다리기
    await vote.waitForDeployment();

    const contractAddress = await vote.getAddress();
    console.log("✅ Vote 컨트랙트 배포 완료! 주소:", contractAddress);


}

// 예외 처리 포함 실행
main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
/*
0. 노드 실행
npx hardhat node

0.5 배포
npx hardhat run scripts/deploy.js --network localhost

1. 하드햇 콘솔 실행
터미널에 입력: npx hardhat console --network localhost

2. Vote 스마트컨트랙트 연결 (배포된 주소로 붙이기)
const Vote = await ethers.getContractFactory("Vote");
const vote = await Vote.attach("0x5FbDB2315678afecb367f032d93F642f64180aa3");

예: const vote = await Vote.attach("0x5fbdb2315678afecb367f032d93f642f64180aa3");

3. 투표 생성하기
첫 번째 투표: 제목 = "팀 김강송이 캡스톤 순위 예측", 항목 = Java, Python, Solidity
await vote.createVote("팀 김강송이 캡스톤 순위 예측", ["1등", "2등", "3등", "4등"]);

4. 투표 제출하기 (voteId = 1, 항목 인덱스 = 0 → Java)
await vote.submitVote(2, 0); // 기본 계정으로 Java 선택

5. 다른 계정으로 투표하기
const accounts = await ethers.getSigners();            // 기본 계정 목록 불러오기
await vote.connect(accounts[1]).submitVote(1, 2);      // 두 번째 계정이 Solidity 선택


6. 투표 결과 확인하기
const result = await vote.getVoteResult(2);
const result2 = await vote.getVoteResult(2);
console.log(result2);

// 예상 출력:
// [
//   '좋아하는 언어는?',
//   [ 'Java', 'Python', 'Solidity' ],
//   [ BigNumber { value: "1" }, BigNumber { value: "0" }, BigNumber { value: "1" } ]
// ]*/


// 하드햇 죽이기
// netstat -ano | findstr :8545
// taskkill /PID <PID번호> /F
// taskkill /PID 18628 /F