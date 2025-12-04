# 백엔드 전용 Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# pom.xml만 먼저 복사하여 의존성 캐시 최적화
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 소스 코드 복사
COPY src ./src

# 빌드
RUN mvn clean package -DskipTests -B

# 런타임 스테이지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/target/mrdinner-backend-1.0.0.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

