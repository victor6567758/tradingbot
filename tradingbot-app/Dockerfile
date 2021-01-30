FROM openjdk:11
MAINTAINER Victor Zoubok <victorofff@gmail.com>

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    APP_SLEEP=0  \
    JAVA_OPTS="" \
    RUN_ARGS=""

#RUN addgroup --system trader
#RUN adduser --system trader --ingroup trader
#WORKDIR /home/trader
WORKDIR /root
COPY entrypoint.sh entrypoint.sh

RUN chmod 755 entrypoint.sh

#RUN chown trader:trader entrypoint.sh
#USER trader:trader

ADD target/tradingbot-app-0.0.1-SNAPSHOT.jar tradingbot-app-0.0.1-SNAPSHOT.jar

EXPOSE 8090

#HEALTHCHECK --interval=60s --timeout=4s CMD curl --fail http://localhost:8090/ || exit 1
ENTRYPOINT ["./entrypoint.sh"]

