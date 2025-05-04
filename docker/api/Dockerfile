# Must match with gradle version in `gradle/wrapper/gradle-wrapper.properties`
# and jvm toolchain version in `build.gradle.kts
FROM gradle:8.13-jdk-21-and-24-alpine AS build

WORKDIR /app

COPY . .

RUN gradle build \
        # Exclude test tasks
        -x test



# Must match with jvm toolchain version in `build.gradle.kts
FROM amazoncorretto:24-alpine3.20-jdk

RUN apk add --no-cache \
    # Required for healthcheck
    curl

COPY --from=build /app/build/libs/urlshortener-0.0.1-SNAPSHOT.jar /app/api.jar

ENV SPRING_PROFILES_ACTIVE=default,production
CMD ["java", "-jar", "/app/api.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl --fail http://127.0.0.1:8080/actuator/health || exit 1
