#!/bin/sh -e

echo "The rpi application will start in ${APP_SLEEP}s..." && sleep ${APP_SLEEP}
exec java ${JAVA_OPTS} \
    -Djava.security.egd=file:/dev/./urandom       \
    -DPROD_LOG_PATH=${PROD_REAL_LOG_PATH}         \
    -DPROD_LOG_LEVEL=${PROD_REAL_LOG_LEVEL}       \
    -DPROD_CONF_YML=/run/secrets/config-secret    \
    -jar "${HOME}/tradingbot-app-0.0.1-SNAPSHOT.jar" ${RUN_ARGS} "$@"