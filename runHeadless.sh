#!/bin/bash

Xvfb :99 &>/dev/null &
export DISPLAY=:99

#remote debugging: /opt/jdk-15/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000  --enable-preview /home/ssm/dev/repo/scrapers/target/scrapers-1.0-SNAPSHOT-jar-with-dependencies.jar

# 9.82 etc ... left upper/right lower corner of rectangular area to be scraped. boundary detection and scrolling is too stupid to cope with fringes of real maps
# autorun ... starts scraping with 10 secs delay to give the headless browser the possibility to completeley load the page
/opt/jdk-15/bin/java -jar --enable-preview -javaagent:./scrapers-1.0-SNAPSHOT.jar ./scrapers-1.0-SNAPSHOT-jar-with-dependencies.jar 27.78 85.14 27.62 85.47 21 autorun