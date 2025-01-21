FROM openjdk:17-oracle

# 빌드된 JAR 파일 복사
COPY ./build/libs/star-be-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]