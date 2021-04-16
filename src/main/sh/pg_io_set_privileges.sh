#!/bin/bash

while :; do
  for f in /var/pg_fileio/*; do
    if [[ -O "$0" ]]; then
      if [ ! -w $f ]; then
        chmod g+w $f
      fi
    fi
  done
  sleep 10
done
