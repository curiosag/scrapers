
CREATE TABLE places
(
    id                 integer primary key,
    place_id           varchar(30),
    name               varchar(250) not null,
    deity_local_name   varchar(256),
    plus_compound_code varchar(250),
    global_code        varchar(250),
    vicinity           varchar(512),
    geom geometry( POINT) not null
);

CREATE INDEX places_geom_idx ON places USING GIST (geom);

CREATE TABLE places_noloc
(
    id                 SERIAL primary key,
    datasource_id      integer,
    proprietary_id     varchar(12),
    name               varchar(128) not null,
    country            varchar(20),
    state              varchar(20),
    district           varchar(20),
    location_info      varchar(40),
    deity_name         varchar(20),
    deity_local_name   varchar(128),
    address            varchar(128)
);

CREATE TABLE datasource
(
    id      SERIAL primary key,
    name    varchar(80),
    comment varchar(250),
    entered date
);

CREATE TABLE places_scraped
(
    place_id           varchar(30),
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
