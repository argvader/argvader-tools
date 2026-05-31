FROM clojure:temurin-21-tools-deps AS builder
WORKDIR /app
COPY libs libs
COPY tools tools
COPY apps/ttrpg_session/service/deps.edn apps/ttrpg_session/service/deps.edn
RUN cd apps/ttrpg_session/service && clj -P
COPY apps/ttrpg_session/service .
RUN clj -M:uberdeps

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y libopus0 && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/app.jar app.jar
COPY --from=builder /app/resources resources
EXPOSE 8888
CMD ["java", "-jar", "app.jar", "prod"]
