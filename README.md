<p align="center">
  <img width="800" alt="Logo" src="https://github.com/user-attachments/assets/ad2443eb-89cb-4312-bd66-41a352da7252" />
</p>

# ⚾ BaseBlock

<p align="center">
블록체인 기반 야구 경기 예매·결제·NFT 티켓 발급 플랫폼  
<br/>
<a href="https://fe-bdj.pages.dev/" target="_blank"><b>👉 https://fe-bdj.pages.dev/</b></a>
</p>

---

## 🧱 프로젝트 개요
> **BaseBlock**은 야구 예매 시스템에 **블록체인(NFT)** 기술을 결합하여  
> 예매·결제·발권의 투명성과 소유권을 강화한 통합 웹 서비스입니다.

- **핵심 목표:** 실사용 가능한 NFT 기반 티켓 시스템 구현  
- **차별점:** 결제 후 TicketNFT 자동 발급  
- **주요 기능:**
  - 경기 일정 조회 및 예매  
  - Iamport 결제 및 검증  
  - Ethereum NFT 발급 (TicketNFT.sol)  
  - 마이페이지 티켓 관리  
  - 관리자 페이지 제공  

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

<p align="center">
  <img src="https://github.com/user-attachments/assets/32ae10cb-3156-4fd1-97b0-66ef1b01bfc5" width="48%">
  <img src="https://github.com/user-attachments/assets/a2ec8d72-5911-46f8-91f8-0a7c52779fba" width="48%">
</p>

---

## 💡 주요 기능 요약
| 기능 | 설명 |
|------|------|
| **예매** | 경기 일정 조회 후 좌석 선택 → 예약 생성 |
| **결제** | Iamport 결제 및 검증 |
| **NFT 발급** | 결제 완료 후 TicketNFT.sol 민팅 |
| **관리자 페이지** | 글 관리, 계정 권한 변경 기능 |

---

## 💡 기능 상세 설명

### **1) 예매 기능 — 경기 일정 조회 & 좌석 선택**

<p align="center">
  <img src="https://github.com/user-attachments/assets/53f95602-0e5e-4964-8e7c-366a0ca6968e" width="32%">
  <img src="https://github.com/user-attachments/assets/33291e0a-480c-4a05-8bc5-7a6a85d459c4" width="32%">
  <img src="https://github.com/user-attachments/assets/a3813242-1587-41ea-a28f-5ae65de64f70" width="32%">
</p>

---

### **2) 결제 및 NFT 발급 — Iamport 결제 → TicketNFT.sol 민팅**

<p align="center">
  <img src="https://github.com/user-attachments/assets/cd5ae670-6acd-443d-95c9-810e5f4e7558" width="24%">
  <img src="https://github.com/user-attachments/assets/ee64a56b-72af-482c-873a-39392db8f2ce" width="24%">
  <img src="https://github.com/user-attachments/assets/f8bac697-f9fd-43eb-95fb-8b4b106d8598" width="24%">
  <img src="https://github.com/user-attachments/assets/6c9d1f99-ffaa-4626-b51e-6079d66b4381" width="24%">
</p>

---

### **3) 관리자 페이지 — 글/계정 권한 관리**

<p align="center">
  <img src="https://github.com/user-attachments/assets/13860d25-2348-425a-a2d8-74612b02d97c" width="48%">
  <img src="https://github.com/user-attachments/assets/b303738c-05f3-45eb-a2e1-2bcbb8714907" width="48%">
</p>

---

## 🧠 트러블슈팅 사례

### **1️⃣ NFT 미발급 오류**
- 원인: 잘못된 `ticket.contract-address`로 Web3Config Bean 생성 실패  
- 조치: EC2 환경변수 수정 후 재배포 → 정상 `txHash` 반환  
- 교훈: 배포 시 환경변수 검증 루틴 필요

### **2️⃣ 403 Forbidden 오류**
- 원인: React 요청 경로(`/api/user/info`)와 Spring Security 경로(`/user/info`) 불일치  
- 조치: API 엔드포인트 통일 및 Swagger 문서 업데이트  
- 교훈: 문서화 부족이 협업 장애로 이어짐

---

## 🧭 프로젝트 회고
> **결제, NFT 발급, 인증 등 다양한 기술 스택이 유기적으로 연결되어 있어  
작은 설정 하나가 전체 장애로 이어질 수 있음을 경험했습니다.**  
이를 계기로 **트러블슈팅 문서화**와 **Runbook 관리의 중요성**을 절실히 깨달았고,  
협업에서는 **명확한 기록과 변경사항 공유가 품질을 좌우한다는 것**을 체감했습니다.
