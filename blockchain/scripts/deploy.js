const { JsonRpcProvider, ContractFactory } = require("ethers");
const fs = require("fs");
const path = require("path");

async function main() {
    // 1. Ganache provider 연결
    const provider = new JsonRpcProvider("http://127.0.0.1:7545");

    // 2. Ganache 첫 번째 계정 사용
    const signer = await provider.getSigner(0);

    // 3. 컴파일된 Vote 컨트랙트 불러오기
    const artifactPath = path.join(__dirname, "../artifacts/contracts/Vote.sol/Vote.json");
    const artifact = JSON.parse(fs.readFileSync(artifactPath, "utf8"));
    const abi = artifact.abi;
    const bytecode = artifact.bytecode;

    // 4. ContractFactory 생성
    const voteFactory = new ContractFactory(abi, bytecode, signer);

    // 5. 배포
    const voteContract = await voteFactory.deploy();
    await voteContract.waitForDeployment();

    console.log(`✅ Vote Contract 배포 완료: ${voteContract.target}`);
}

// 배포 실행
main().catch((error) => {
    console.error(error);
    process.exit(1);
});
