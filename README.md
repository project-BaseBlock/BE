<h1 align="center">⚾ BaseBlock</h1>

<p align="center">
블록체인 기반 야구 경기 예매·결제·NFT 티켓 발급 플랫폼  
<br/>
<a href="https://fe-bdj.pages.dev/" target="_blank"><b>👉 https://fe-bdj.pages.dev/</b></a>
</p>

---

## 🧱 프로젝트 개요
> **BaseBlock**은 야구 예매 시스템에 **블록체인(NFT)** 기술을 접목하여  
> 경기 예매, 결제, 발권의 투명성과 소유권을 강화한 **웹 통합 플랫폼**입니다.

- **핵심 목표:** 실사용 가능한 NFT 예매 시스템 구현  
- **차별점:** 결제 이후 자동 NFT 발급, 마이페이지 내 소유권 확인  
- **주요 기능:**
  - 경기 일정 조회 및 예매
  - Iamport 결제 및 검증
  - Ethereum NFT 발급 (TicketNFT.sol)
  - 마이페이지 티켓 관리
  - 관리자 페이지 (글/계정 권한 관리)

---

## ⚙️ 기술 스택 (Tech Stack)

### 🖥 Frontend
<p align="left">
<img src="https://img.shields.io/badge/React-61DAFB?style=flat&logo=react&logoColor=black"/>
<img src="https://img.shields.io/badge/Vite-646CFF?style=flat&logo=vite&logoColor=white"/>
<img src="https://img.shields.io/badge/Axios-5A29E4?style=flat&logo=axios&logoColor=white"/>
<img src="https://img.shields.io/badge/Cloudflare%20Pages-F38020?style=flat&logo=cloudflare&logoColor=white"/>
</p>

### 🔧 Backend
<p align="left">
<img src="https://img.shields.io/badge/Java-007396?style=flat&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white"/>
<img src="https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white"/>
<img src="https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white"/>
<img src="https://img.shields.io/badge/Swagger-85EA2D?style=flat&logo=swagger&logoColor=black"/>
</p>

### ⛓ Blockchain & Payment
<p align="left">
<img src="https://img.shields.io/badge/Solidity-363636?style=flat&logo=solidity&logoColor=white"/>
<img src="https://img.shields.io/badge/Web3j-F16822?style=flat&logo=ethereum&logoColor=white"/>
<img src="https://img.shields.io/badge/Iamport-00A9E0?style=flat&logo=paypal&logoColor=white"/>
<img src="https://img.shields.io/badge/Ethereum-3C3C3D?style=flat&logo=ethereum&logoColor=white"/>
</p>

### ☁ Infra / DevOps
<p align="left">
<img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=flat&logo=amazonec2&logoColor=white"/>
<img src="https://img.shields.io/badge/Nginx-009639?style=flat&logo=nginx&logoColor=white"/>
<img src="https://img.shields.io/badge/systemd-222222?style=flat&logo=linux&logoColor=white"/>
<img src="https://img.shields.io/badge/Jenkins-D24939?style=flat&logo=jenkins&logoColor=white"/>
<img src="https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white"/>
</p>

---

## 🧩 시스템 아키텍처
(아키텍처 다이어그램 이미지 삽입 위치)

---

## 💡 주요 기능 요약
| 기능 | 설명 |
|------|------|
| **예매** | 경기 일정 조회 후 좌석 선택 및 예매 생성 |
| **결제** | Iamport 연동 결제 및 검증 |
| **NFT 발급** | 결제 완료 후 TicketNFT.sol 민팅 |
| **게시판** | 사용자 커뮤니티 기능 및 댓글 작성 |
| **관리자 페이지** | 글 관리, 계정 권한 변경 |
| **트러블슈팅** | 환경변수 오류, 403 접근 거부 등 주요 이슈 기록 |

---

## 🚀 실행 방법 (Getting Started)
```bash
# 백엔드
./gradlew build
scp build/libs/baseblock.jar ubuntu@<EC2_IP>:/home/ubuntu/app/
ssh ubuntu@<EC2_IP>
sudo systemctl restart baseblock.service

# 프론트엔드
npm install
npm run dev

🧠 트러블슈팅 사례

1️⃣ NFT 미발급 오류

원인: ticket.contract-address 오입력으로 Bean 생성 실패

조치: 환경변수 수정 후 재배포 → 정상 txHash 반환

예방: Jenkins 환경변수 검증 스크립트 추가

2️⃣ 403 Forbidden 오류

원인: React 요청 경로(/api/user/info)와 Spring Security 경로(/user/info) 불일치

조치: 경로 수정 및 Swagger 문서 업데이트

교훈: 문서화의 중요성, 협업 시 변경 내역 공유 체계 확립

🧭 프로젝트 회고

“결제, NFT 발급, 인증 등 다양한 기술 스택이 연결된 만큼
작은 설정 오류 하나가 전체 서비스 장애로 이어질 수 있다는 점을 직접 경험했습니다.
이를 계기로 트러블슈팅 문서화와 Runbook 관리의 중요성을 깨달았고,
협업 시 명확한 기록과 전달 체계가 품질을 좌우한다는 것을 체감했습니다.”
