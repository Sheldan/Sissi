#!/bin/bash
set -o allexport
source .env
set +o allexport
envsubst < res/prometheus-scrapper-password > res/prometheus-scrapper-password-filled