# 백엔드 배포 가이드

이 레포지토리는 백엔드(Spring Boot)만 포함되어 있습니다.

## Render 배포 방법

### 방법 1: render.yaml 사용 (추천)

1. **GitHub에 레포지토리 푸시**
   ```bash
   git add .
   git commit -m "Initial commit"
   git push origin main
   ```

2. **Render 대시보드에서 배포**
   - [Render](https://render.com/) 접속
   - "New" → "Blueprint" 선택
   - GitHub 레포지토리 연결
   - Render가 자동으로 `render.yaml`을 인식하여 서비스 생성

3. **환경 변수 설정**
   Render 대시보드 → 서비스 → "Environment" 탭에서 다음 변수 설정:
   ```env
   SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/[DATABASE]?useSSL=true&requireSSL=true&serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci
   SPRING_DATASOURCE_USERNAME=[USERNAME]
   SPRING_DATASOURCE_PASSWORD=[PASSWORD]
   JWT_SECRET=[최소 32자 이상의 랜덤 문자열]
   FRONTEND_URL=https://[프론트엔드 URL].onrender.com
   ```

### 방법 2: 수동 설정

1. **Render 대시보드 접속**
   - [Render](https://render.com/)에 로그인

2. **새 Web Service 생성**
   - "New" → "Web Service" 선택
   - GitHub 레포지토리 연결
   - 다음 설정 입력:
     - **Name**: `mrdinner-backend`
     - **Environment**: `Docker`
     - **Dockerfile Path**: `./Dockerfile`
     - **Docker Context**: `.` (현재 디렉토리)

3. **환경 변수 설정** (위와 동일)

## 환경 변수 설명

- `SPRING_DATASOURCE_URL`: AivenDB 또는 다른 MySQL 데이터베이스 연결 URL
- `SPRING_DATASOURCE_USERNAME`: 데이터베이스 사용자명
- `SPRING_DATASOURCE_PASSWORD`: 데이터베이스 비밀번호
- `JWT_SECRET`: JWT 토큰 서명에 사용할 시크릿 키 (최소 32자)
- `FRONTEND_URL`: 프론트엔드 URL (CORS 설정용)

## 배포 후 확인

- 백엔드 URL: `https://[서비스명].onrender.com`
- 헬스 체크: `https://[서비스명].onrender.com/api/menus`

