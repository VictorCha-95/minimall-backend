# 1. Java 21 버전의 경량 베이스 이미지 사용
FROM openjdk:21-jdk-slim

# 2. 빌드된 JAR 파일을 컨테이너 안으로 복사
COPY build/libs/minimall-0.0.1-SNAPSHOT.jar /app.jar

# 3. 컨테이너가 실행될 때 자동으로 이 명령을 수행
ENTRYPOINT ["java", "-jar", "/app.jar"]