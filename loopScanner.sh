#!/bin/bash

# debug using: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:8000

Xvfb :99 -screen 1920x1080x16 &>/dev/null &
export DISPLAY=:99

echo "sanner zoom 15 any markers"

while [ $? -eq 0 ]; do
  /opt/jdk-15/bin/java -Dprism.order=sw -jar --enable-preview -javaagent:./scrapers-1.0-SNAPSHOT.jar ./Launcher-jar-with-dependencies.jar 15 autorun any 2>&1 > scanner.log
done