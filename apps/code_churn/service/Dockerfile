FROM openjdk:14-jdk-alpine3.10

# install git
RUN apk add --no-cache git

# add credentials on build
RUN mkdir ~/.ssh && ln -s ./git-cli-key ~/.ssh/id_rsa

# make sure domain is accepted
RUN touch /root/.ssh/known_hosts
RUN ssh-keyscan github.org >> /root/.ssh/known_hosts

ADD target/service.jar /target/code-churn.jar
EXPOSE 8888
CMD ["java", "-cp", "target/code-churn.jar", "clojure.main", "-m", "code-churn.core", "prod"]
