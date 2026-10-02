FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests package && \
    mv target/car-booking-service-*.jar target/app.jar

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build /build/target/app.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]