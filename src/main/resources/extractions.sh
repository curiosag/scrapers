psql PhHhSka85GT6

cat UncategorizedIndia.csv | awk -vFPAT='([^,]*)|("[^"]+")' -vOFS=, '{print "     INSERT INTO places_staging (name, deity, vicinity, geom) values (\x27"$1"\x27,\x27"$3"\x27,\x27"$2"\x27, ST_GeomFromText(\x27POINT("$6" "$7")\x27));"}' | sed "s/\"//g" >uc.sql

cat SriLankaWithDeities.csv | sed "s/|/\//g" | awk -vFPAT='([^,]*)|("[^"]+")' -vOFS=, '{gsub(/\,/, ".", $5);gsub(/\,/, ".", $6);print $1 "|" $2  "|"  $4  "|"  $5  "|"  $6}' | sed "s/\"//g" | sed -E "s/([a-zA-z0-9]+)'([a-zA-z0-9]+)/\1''\2/g" >SL_pipe.csv
cat SL_pipe.csv | awk -F '|' '{printf "INSERT INTO srilanka_staging (name, deity, vicinity, geom) values(\x27%s\x27, \x27%s\x27, \x27%s\x27, ST_GeomFromText(\x27POINT(%s %s)\x27));\n", $1,  $2, $3, $4, $5 }'

grep "[a-zA-z]'[a-zA-z]"

# using jq command line json stuff

cat temples_Orissa.csv | sed "s/|/\//g" | awk -vFPAT='([^,]*)|("[^"]+")' -vOFS=, 'print $1 "|" $2  "|"  $4  "|"  $5  "|"  $6  "|"  $4}' |
  sed "s/\"/\x27/g" | awk -F '|' '{printf "insert into places_noloc (datasource_id, proprietary_id, name, country, state, location_info) values (3, \x27%s\x27, %s, \x27India\x27, %s, %s);\n",  $1, $2, $4, $3}' \
    >orissa.sql

# gsub(/\,/, ".", $5) ... manipulate column 5 only
cat Tamil_Nadu_Database.csv | sed "s/|/\//g" | awk -vFPAT='([^,]*)|("[^"]+")' -vOFS=, '{gsub(/\,/, ".", $5);gsub(/\,/, ".", $6);print $1 "|" $2 "|" $3  "|"  $4  "|"  $5  "|" $7}' |
  sed "s/\"/\x27/g" | sed "s/||/|\x27\x27|/g" | awk -F '|' '{printf "insert into places_noloc (datasource_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name) values (4, \x27%s\x27, %s, \x27India\x27, \x27Tamil Nadu\x27, %s, %s, %s, %s);\n",  $1, $2, $3, $6, $4, $5}' \
    >Tamil_Nadu_Database.sql

# sed 1d: drop 1st column
# sed "s/[ \t]*\"[ \t]*/\x27/g" ... change " to ' and remove leading and trailing whitespace
# sed "s/[ \t]*Dt\.//g" ... remove the District abbreviation
cat temple_list_Chattisgarh_a_s.csv | sed '1d;s/|/\//g' | awk -vFPAT='([^,]*)|("[^"]+")' -vOFS=, '{gsub(/^ /,"");print $1 "|" $2 "|" $3  "|"  $4  "|"  $5 "|"  $6 "|" $7}' |
  sed "s/[ \t]*\"[ \t]*/\x27/g" |
  sed "s/[ \t]*Dt\.//g" |
  awk -F '|' '{printf "insert into places_noloc (datasource_id, proprietary_id, name, country, state, district, address, deity_name) values (5, \x27%s\x27, %s, \x27India\x27, \x27Chattisgarh\x27, %s, %s, %s);\n",  $1, $2, $4, $3, $6}' \
    >temple_list_Chattisgarh_a_s.sql

cat temple_list_Pondicheery_1.csv |
  sed '1d;s/|/\//g' | # drop 1st column, pipe -> slash
  sed "s/[ \t]*\"[ \t]*/\"/g" | # remove leading and trailing whitespace around "
  sed "s/\",,\"/\"||\"/g" | # comma to pipe for empty columns
  sed "s/\",\"/\"|\"/g" | # comma to pipe when between quotes
  sed "s/^,//g" | # remove 1st comma, leading column is empty
  sed "s/\"/\x27/g" | # double to single quote
  awk -F '|' '{printf "insert into places_noloc (datasource_id, proprietary_id, name, country, state, district, address, deity_name) values (6, null, %s, \x27India\x27, \x27Puducherry\x27, %s, %s, %s);\n",  $1, $3, $2, $5}' \
  > temple_list_Pondicheery_1.sql
