# Setup Gradle
FROM gradle:7.6.0-jdk8 AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY build.gradle settings.gradle $APP_HOME

COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/src
USER root
RUN chown -R gradle /home/gradle/src

RUN gradle build || return 0
COPY . .
RUN gradle clean build

# Setup Java
FROM amazoncorretto:8
ENV ARTIFACT_NAME=stipula-node-1.0-SNAPSHOT.jar
ENV APP_HOME=/usr/app

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

# Run
EXPOSE 8080
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}