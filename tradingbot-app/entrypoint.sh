#!/bin/sh -e

echo "The application will start in ${APP_SLEEP}s..." && sleep ${APP_SLEEP}
exec java ${JAVA_OPTS} \
    -Djava.security.egd=file:/dev/./urandom \
    -jar "${HOME}/tradingbot-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar" ${RUN_ARGS} "$@"