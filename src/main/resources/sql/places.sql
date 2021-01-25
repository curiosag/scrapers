
CREATE TABLE places
(
    id                 integer primary key,
    place_id           varchar(30),
    name               varchar(250) not null,
    plus_compound_code varchar(250),
    global_code        varchar(250),
    vicinity           varchar(512),
    geom geometry( POINT) not null
);

CREATE INDEX places_geom_idx ON places USING GIST (geom);

CREATE TABLE places_scraped
(
    place_id           varchar(30) primary key,
    name               varchar(250) not null,
    plus_compound_code varchar(250),
    global_code        varchar(250),
    vicinity           varchar(512),
    geom     geometry( POINT) not null
);

CREATE INDEX places_scraped_geom_idx ON places_scraped USING GIST (geom);

create table places_staging
(
    serialid           SERIAL not null,
    load               varchar(30),
    place_id           varchar(30),
    name               varchar(250),
    deity              varchar(250),
    plus_compound_code varchar(250),
    global_code        varchar(250),
    vicinity           varchar(512),
    geom               geometry( Point)
);
