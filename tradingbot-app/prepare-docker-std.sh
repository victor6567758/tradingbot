#!/bin/bash

docker login
docker build -t trading-bot-2.0 -f docker/standard/Dockerfile .
docker tag trading-bot-2.0 dockerzzzzzzzzzzzz123/trading-bot-2.0:latest
docker push dockerzzzzzzzzzzzz123/trading-bot-2.0:latest