#!/bin/bash

#remote debugging: /opt/jdk-15/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000  --enable-preview /home/ssm/dev/repo/scrapers/target/scrapers-1.0-SNAPSHOT-jar-with-dependencies.jar

# 9.82 etc ... left upper/right lower corner of rectangular area to be scraped. boundary detection and scrolling is too stupid to cope with fringes of real maps
# autorun ... starts scraping with 10 secs delay to give the headless browser the possibility to completeley load the page
# to debug add after -jar: -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000

# -Dprism.order=sw  ... that's hardware graphics acceleration switched off, seems to make trouble with Xvfb ("ES2 Prism: Error - GLX extension is not supported")

# fluxbox ... looks like using fluxbox the browser runs smoothly, without it it gets stuck in some strange UI state

export DISPLAY=:${1}
Xvfb $DISPLAY -screen 0 1920x1080x16 &
PID_XVFB=$!
fluxbox -display $DISPLAY &
x11vnc -display $DISPLAY -bg -forever -nopw -quiet -rfbport 59${1} &


echo "scraper zoom:18 marker:hindu_temple display:${DISPLAY} debugport:none vncport:59${1}"
LOGNAME="sp${DISPLAY}.log"
LOGNAME=${LOGNAME//[:]/}

while [ $? -eq 0 ]; do
  /opt/jdk-15/bin/java -Dprism.order=sw -jar --enable-preview -javaagent:./scrapers-1.0-SNAPSHOT.jar ./Launcher-jar-with-dependencies.jar 18 autorun TYPE_HINDU_TEMPLE 6000 > "${LOGNAME}"  2>&1
done

function finally {
  kill -9 $PID_XVFB
}

trap finally EXIT