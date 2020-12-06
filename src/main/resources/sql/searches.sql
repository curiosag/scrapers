CREATE TABLE searches
(
    search_id SERIAL primary key,
    lat double precision not null,
    lon double precision not null,
    done integer default 0
);