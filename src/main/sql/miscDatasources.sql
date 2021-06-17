CREATE TABLE misc_place
(
    id                  SERIAL primary key,
    datasource_id       int not null,
    misc_location__id   int not null,
    name                varchar(80),
    deity               int not null,
    deity_local_name    varchar2(256),
    proprietary_keytype varchar(20),
    proprietary_key     varchar(80)
);

CREATE TABLE location_identifier
(
    id   SERIAL primary key,
    location_identifier_type_id integer,
    value varchar(120)
);

CREATE TABLE location_identifier_type
(
    id   SERIAL primary key,
    name varchar(20)
);

CREATE TABLE datasource
(
    id      SERIAL primary key,
    name    varchar(80),
    comment varchar(250),
    entered date
);

create unique index u_location_element_type (name) on location_element_type;

CREATE TABLE place_deity
(
    id   SERIAL primary key,
    deity_id integer,
    place_id integer
);

CREATE TABLE place_location_identifier
(
    id   SERIAL primary key,
    place_id integer,
    location_identifier_id integer
);
