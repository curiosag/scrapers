CREATE TABLE misc_place
(
    id SERIAL primary key,
    datasource_id int not null,
    misc_location_id int not null,
    name     varchar(80),
    deity  int not null,
    deity_local_name varchar2(256),
    proprietary_keytype varchar(20),
    proprietary_key varchar(80)
);

CREATE TABLE misc_location_element(
    id SERIAL primary key,
    location_type varchar(20),
    value varchar2(512)
);

CREATE TABLE location_element_type(
    id  SERIAL primary key,
    name varchar(20)
);

create unique index u_location_element_type (name) on location_element_type;

CREATE TABLE misc_datasource
(
    id SERIAL primary key,
    name     varchar(80),
    comment varchar(250),
    entered date
);