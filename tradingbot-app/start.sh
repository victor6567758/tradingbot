#!/bin/bash
java -DPROD_LOG_PATH=c:\tmp\logs\happy-trader_manual\ -DPROD_LOG_LEVEL=DEBUG -DPROD_CONF_YML=... -jar target\tradingbot-app-0.0.1-SNAPSHOT.jar