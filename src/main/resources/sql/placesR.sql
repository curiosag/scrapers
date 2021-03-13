
CREATE TABLE place
(
    id                 integer primary key,
    name               varchar(250) not null,
    plus_compound_code varchar(250),
    global_code        varchar(250),
    vicinity           varchar(512),
    geom geometry (POINT) not null
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

drop table if exists place_region;
create table place_region
(
    id SERIAL
        constraint region_place_pk
        primary key,
    region_id int not null,
    place_id int not null
);

create unique index region_place_region_id_place_id_uindex
    on place_region (region_id, place_id);


insert into places_noloc(datasource_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name, description)
select 10, null, temple, 'India', 'Punjab', "District", locality, "Main Deity", null, null from "temple_list_Punjab";


 create table contact                  | table | sm
 create table contact_place            | table | sm
 create table contact_place_noloc      | table | sm
 create table datasource               | table | sm
 create table places                   | table | sm
create table  place_region             | table | sm
 create table places_noloc             | table | sm
 create table places_scraped           | table | sm
 create table places_scraped_lola      | table | sm
 create table places_staging           | table | sm
 create table region                   | table | sm
 create table region_lola              | table | sm
 create table regions                  | table | sm
 create table searches                 | table | sm
