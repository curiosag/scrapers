CREATE TABLE places
(
    place_id           varchar(30) primary key,
    name               varchar(250) not null,
    plus_compound_code varchar(250),
    global_code        varchar(250),
    vicinity           varchar(512),
    found              timestamp  not null
);

CREATE INDEX places_name_idx ON places (name);

CREATE TABLE places_geo
(
    place_id varchar(30) primary key,
    geom     geometry( POINT) not null
);

CREATE INDEX places_geom_idx ON places_geo USING GIST (geom);

CREATE OR REPLACE FUNCTION addPlaceTimestamp()
    RETURNS TRIGGER
    LANGUAGE PLPGSQL
AS  $$
BEGIN
    NEW.found = now();
RETURN NEW;
END;
$$;

CREATE TRIGGER places_add_timestamp
    BEFORE INSERT
    ON places
    FOR EACH ROW
    EXECUTE PROCEDURE addPlaceTimestamp();