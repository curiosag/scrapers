psql PhHhSka85GT6

cat UncategorizedIndia.csv | awk -vFPAT='([^,]*)|("[^"]+")' -vOFS=, '{print "     INSERT INTO places_staging (name, deity, vicinity, geom) values (\x27"$1"\x27,\x27"$3"\x27,\x27"$2"\x27, ST_GeomFromText(\x27POINT("$6" "$7")\x27));"}' | sed "s/\"//g" > uc.sql


cat SriLankaWithDeities.csv | sed "s/|/\//g" | awk -vFPAT='([^,]*)|("[^"]+")' -vOFS=, '{gsub(/\,/, ".", $5);gsub(/\,/, ".", $6);print $1 "|" $2  "|"  $4  "|"  $5  "|"  $6}' | sed "s/\"//g" | sed -E "s/([a-zA-z0-9]+)'([a-zA-z0-9]+)/\1''\2/g" > SL_pipe.csv
cat SL_pipe.csv | awk -F '|' '{printf "INSERT INTO srilanka_staging (name, deity, vicinity, geom) values(\x27%s\x27, \x27%s\x27, \x27%s\x27, ST_GeomFromText(\x27POINT(%s %s)\x27));\n", $1,  $2, $3, $4, $5 }'

grep "[a-zA-z]'[a-zA-z]"
