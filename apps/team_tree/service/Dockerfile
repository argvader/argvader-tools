FROM openjdk:14-jdk-alpine3.10

ADD target/service.jar /target/team-tree.jar
EXPOSE 8888
CMD ["java", "-cp", "target/team-tree.jar", "clojure.main", "-m", "team-tree.core", "prod"]
