require("@nomicfoundation/hardhat-toolbox");

module.exports = {
  defaultNetwork: "hardhat",
  networks: {
    hardhat: {
      chainId: 31337, // 하드햇 기본 chainId
      accounts: {
        mnemonic: "test test test test test test test test test test test junk" // ⭐ 고정된 mnemonic
      }
    },
  },
  solidity: {
    version: "0.8.28", // 네 프로젝트 솔리디티 버전 맞춤
    settings: {
      optimizer: {
        enabled: true,
        runs: 200,
      },
    },
  },
};
// "랜덤 주소를 쓰고 싶으면 accounts: { mnemonic: ~ } 이걸 통째로 지워라."