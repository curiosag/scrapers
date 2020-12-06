CREATE TABLE regions
(
    region_id SERIAL primary key,
    name0     varchar(80),
    name1     varchar(80),
    name2     varchar(80),
    type      integer     not null,
    geom      geometry( POLYGON) not null
);