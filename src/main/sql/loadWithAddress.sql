
--insert into places_noloc (datasource_id, proprietary_id, name, country, state, district, address)
insert into places_scraped(id, place_id, source_id, name, vicinity, geom)
select (row_number() over() + (select max(id) from places_scraped)),
       "Sl No",  8, "Temple Name",
       "District" || ', '|| "Address 1" || '; '|| "Address 2" || ' '|| "Pin Code",
       (st_geomfromtext('POINT(' || SPLIT_PART("Google Map Location Coordinates", ',', 1) || ' ' || SPLIT_PART("Google Map Location Coordinates", ',', 2) || ')', 4326))
from "staging_ThalasseryDivision" where "Google Map Location Coordinates" is  not null;


select count(1)from places_scraped s
                        join contact_place cp on s.id = cp.place_id
                        join contact c on cp.contact_id = c.id
where source_id = 8;

delete from places_scraped
where source_id = 8;

insert into contact (phone1, phone2, address, zip_code, proprietary_id)
select "Telephone", "Mobile", "Address 1" || '; '|| "Address 2" || ' '|| "Pin Code", "Pin Code", "Sl No"
from "staging_ThalasseryDivision" where "Google Map Location Coordinates" is   null;

insert into contact_place (contact_id, place_id)
select c.id, p.id from contact c join places_scraped p on cast(p.place_id as integer) = c.proprietary_id
where p.source_id = 8;

