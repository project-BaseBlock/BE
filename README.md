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
<img src="https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white"/>
</p>

---

## 🧩 시스템 아키텍처 및 ERD
<img width="992" height="824" alt="스크린샷 2025-10-25 212059" src="https://github.com/user-attachments/assets/32ae10cb-3156-4fd1-97b0-66ef1b01bfc5" />
<img width="1536" height="1024" alt="ChatGPT Image 2025년 11월 14일 오후 06_43_48" src="https://github.com/user-attachments/assets/a2ec8d72-5911-46f8-91f8-0a7c52779fba" />

---

## 💡 주요 기능 요약
| 기능 | 설명 |
|------|------|
| **예매** | 경기 일정 조회 후 좌석 선택 및 예매 생성 |
| **결제** | Iamport 연동 결제 및 검증 |
| **NFT 발급** | 결제 완료 후 TicketNFT.sol 민팅 |
| **관리자 페이지** | 글 관리, 계정 권한 변경 |

---
## 💡 기능 상세 설

**예매** 경기 일정 조회 후 좌석 선택 및 예매 생성
<img width="909" height="943" alt="스크린샷 2025-10-25 213223" src="https://github.com/user-attachments/assets/53f95602-0e5e-4964-8e7c-366a0ca6968e" />
<img width="956" height="946" alt="스크린샷 2025-10-25 213308" src="https://github.com/user-attachments/assets/33291e0a-480c-4a05-8bc5-7a6a85d459c4" />
<img width="932" height="947" alt="스크린샷 2025-10-25 213341" src="https://github.com/user-attachments/assets/a3813242-1587-41ea-a28f-5ae65de64f70" />


**결제 및 NFT 발급** Iamport 연동 결제 및 검증, 결제 완료 후 TicketNFT.sol 민팅
<img width="985" height="942" alt="스크린샷 2025-10-25 213352" src="https://github.com/user-attachments/assets/cd5ae670-6acd-443d-95c9-810e5f4e7558" />
<img width="939" height="943" alt="스크린샷 2025-10-25 213406" src="https://github.com/user-attachments/assets/ee64a56b-72af-482c-873a-39392db8f2ce" />
<img width="931" height="940" alt="스크린샷 2025-10-25 213436" src="https://github.com/user-attachments/assets/f8bac697-f9fd-43eb-95fb-8b4b106d8598" />
<img width="1013" height="944" alt="스크린샷 2025-10-25 213446" src="https://github.com/user-attachments/assets/6c9d1f99-ffaa-4626-b51e-6079d66b4381" />

**관리자 페이지** 글 관리, 계정 권한 변경 
<img width="889" height="943" alt="스크린샷 2025-10-25 213521" src="https://github.com/user-attachments/assets/13860d25-2348-425a-a2d8-74612b02d97c" />
<img width="905" height="941" alt="스크린샷 2025-10-25 213557" src="https://github.com/user-attachments/assets/b303738c-05f3-45eb-a2e1-2bcbb8714907" />


---

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
