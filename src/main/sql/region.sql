DROP TABLE IF EXISTS region;
CREATE TABLE region
(
    id        integer primary key,
    parent_id integer,
    name      varchar(80)            not null,
    level     integer                not null,
    type      integer                not null,
    geom      geometry(MULTIPOLYGON) not null
);

CREATE INDEX region_geom_idx ON region USING GIST (geom);
CREATE INDEX region_name_idx ON region (name);

DROP TABLE IF EXISTS lola_region;
create table lola_region
(
    id        integer primary key,
    parent_id integer                      not null,
    name      varchar(80)                  not null,
    level     integer                      not null,
    type      integer                      not null,
    geom      geometry(MultiPolygon, 4326) not null
);

DROP TABLE IF EXISTS staging_region;
create table staging_region
(
    id        integer                      not null,
    name      varchar(80)                  not null,
    name_sup  varchar(80)                  ,
    geom      geometry(MultiPolygon, 4326) not null
);

DROP VIEW IF EXISTS staging_region_unions;
create view staging_region_unions as
SELECT row_number() over() num, r.name_sup, r.name,  ST_Multi(ST_Union(r.geom)) as geom
FROM staging_region r
GROUP BY r.name_sup, r.name;


insert into region(id, parent_id, name, level, type, geom)
select id, parent_id, name, level, type, st_flipcoordinates(geom) from region_lola
where region_lola.id >= 48;