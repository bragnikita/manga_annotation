#!/bin/bash

TARGET=${1:-"ALL"}
if [[ $TARGET = "ALL" || $TARGET = "web" ]]; then
  WEB="ok"
fi
if [[ $TARGET = "ALL" || $TARGET = "server" ]]; then
  SERVER="ok"
fi
if [[ -z $WEB && -z $SERVER ]]; then
  FILE=$TARGET
fi


if [[ -n $WEB ]]; then
  (cd front && npm run-script build)
  mkdir -p dist/site
  cp -r front/dist/ dist/site
  rsync -a -P dist/site __magireco_services:/opt/bitnami/apps/ma/
fi
if [[ -n $SERVER ]]; then
  (cd back/boot_app && ./gradlew bootJar)
  cp back/boot_app/build/libs/server.jar dist
  rsync -a -P dist/server.jar __magireco_services:/opt/bitnami/apps/ma/
#  cp back/production.properties dist/application.properties
#  cp back/start.sh dist/
fi
if [[ -n $FILE ]]; then
  rsync -a -P dist/${FILE} __magireco_services:/opt/bitnami/apps/ma/
fi
