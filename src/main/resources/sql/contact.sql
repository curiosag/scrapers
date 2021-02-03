drop table if exists contact;
    create table contact
(
    id       serial not null
        constraint contact_data_pk
            primary key,
    phone1   varchar(15),
    phone2   varchar(15),
    address  varchar(128),
    zip_code varchar(15)
);

drop table if exists contact_place_noloc;
create table contact_place_noloc
(
    id         serial  not null
        constraint place_noloc_contact_pk
            primary key,

    contact_id integer not null references contact (id),
    place_id   integer not null references places_noloc (id)
);

create unique index contact_place_noloc_place_id_contact_id_uindex
    on contact_place_noloc (place_id, contact_id);

drop table if exists contact_place;
create table contact_place
(
    id         serial  not null
        constraint place_contact_pk
            primary key,

    contact_id integer not null references contact (id),
    place_id   integer not null references places_scraped (id)
);

create unique index contact_place_place_id_contact_id_uindex
    on contact_place (place_id, contact_id);