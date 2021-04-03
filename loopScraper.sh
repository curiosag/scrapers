#!/bin/bash

#remote debugging: /opt/jdk-15/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000  --enable-preview /home/ssm/dev/repo/scrapers/target/scrapers-1.0-SNAPSHOT-jar-with-dependencies.jar

# 9.82 etc ... left upper/right lower corner of rectangular area to be scraped. boundary detection and scrolling is too stupid to cope with fringes of real maps
# autorun ... starts scraping with 10 secs delay to give the headless browser the possibility to completeley load the page
# to debug add after -jar: -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000

# -Dprism.order=sw  ... that's hardware graphics acceleration switched off, seems to make trouble with Xvfb ("ES2 Prism: Error - GLX extension is not supported")

Xvfb :99 -screen 1920x1080x16 &>/dev/null &
export DISPLAY=:99

echo "scraper zoom 18 hindu_temple markers"

while [ $? -eq 0 ]; do
  /opt/jdk-15/bin/java -Dprism.order=sw -jar --enable-preview -javaagent:./scrapers-1.0-SNAPSHOT.jar ./Launcher-jar-with-dependencies.jar 18 autorun hindu_temple 2>&1 >scraper.log
done
