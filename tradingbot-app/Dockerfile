FROM azul/zulu-openjdk-alpine:11
MAINTAINER Victor Zoubok <victorofff@gmail.com>

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    APP_SLEEP=0 \
    JAVA_OPTS="" \
    RUN_ARGS=""

VOLUME /tmp

# Add a user admin to run our application so that it doesn't need to run as root
RUN adduser -D -s /bin/sh admin
WORKDIR /home/admin

COPY entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh && chown admin:admin entrypoint.sh
USER admin

ADD target/tradingbot-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar tradingbot-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar

ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 8090