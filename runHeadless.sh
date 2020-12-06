#!/bin/bash

Xvfb :99 &>/dev/null &
export DISPLAY=:99

#remote debugging: /opt/jdk-15/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000  --enable-preview /home/ssmertnig/dev/repo/scrapers/target/scrapers-1.0-SNAPSHOT-jar-with-dependencies.jar

/opt/jdk-15/bin/java -jar --enable-preview /home/ssmertnig/dev/repo/scrapers/target/scrapers-1.0-SNAPSHOT-jar-with-dependencies.jar