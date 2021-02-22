FROM balenalib/raspberrypi3-debian-openjdk:latest
MAINTAINER Victor Zoubok <victorofff@gmail.com>

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    APP_SLEEP=0  \
    JAVA_OPTS="" \
    RUN_ARGS=""


WORKDIR /root
COPY docker/rpi/entrypoint.sh entrypoint.sh

RUN mkdir -p config
COPY docker/config/bitmex-account-prod.yml config/bitmex-account-prod.yml

RUN chmod 755 entrypoint.sh
ADD target/tradingbot-app-0.0.1-SNAPSHOT.jar tradingbot-app-0.0.1-SNAPSHOT.jar

EXPOSE 8090

ENTRYPOINT ["./entrypoint.sh"]