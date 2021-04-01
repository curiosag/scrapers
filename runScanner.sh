#!/bin/bash

#remote debugging: /opt/jdk-15/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000  --enable-preview /home/ssm/dev/repo/scrapers/target/scrapers-1.0-SNAPSHOT-jar-with-dependencies.jar

# 9.82 etc ... left upper/right lower corner of rectangular area to be scraped. boundary detection and scrolling is too stupid to cope with fringes of real maps
# autorun ... starts scraping with 10 secs delay to give the headless browser the possibility to completeley load the page


Xvfb :99 &>/dev/null &
export DISPLAY=:99


/opt/jdk-15/bin/java -jar --enable-preview -javaagent:/home/ssmertnig/dev/repo/scrapers/target/scrapers-1.0-SNAPSHOT.jar /home/ssmertnig/dev/repo/scrapers/target/Launcher-jar-with-dependencies.jar 15 autorun any >> scanner.log


