#!/bin/bash

kubectl delete pv --all
kubectl delete configmap happy-trader-2-configmap -n happy-trader
kubectl delete pvc happy-trader-2-volume-claim -n happy-trader
kubectl delete svc happy-trader-2-service -n happy-trader
kubectl delete deployment happy-trader-2 -n happy-trader
kubectl delete namespaces happy-trader

