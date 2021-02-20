#!/bin/bash

docker login
docker build -t trading-bot-2.0-pi -f docker/rpi/Dockerfile .
docker tag trading-bot-2.0-pi dockerzzzzzzzzzzzz123/trading-bot-2.0-pi:latest
docker push dockerzzzzzzzzzzzz123/trading-bot-2.0-pi:latest