#!/bin/sh
set -e

chmod +x ./gradlew
exec ./gradlew bootRun
