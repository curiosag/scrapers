    /*
    * sudo apt-get install postgresql-client
    * docker run --name gis -e POSTGRES_PASSWORD=<pwd> -d postgis/postgis
    *
    * docker exec -it my-postgres(gis) bash


      psql -h 172.17.0.2 -p 5432 -U postgres -W
     
      \l .. list dbs
      \dt .. list tables in db
      \c <database_name> switch to db
      \d <table_name> describe table
      \? available commands
      SELECT version();

      for outside connection find out IP of postgres on docker: docker inspect <Container-ID>

      geometries use SRIDs -> Spatial Reference Identifier, defaults to 4326 WGS 84 long lat (World Geodetic System 1984)
    * */