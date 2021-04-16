
create view staging_place_scraped_unique as
select o.* from staging_place_scraped o , (
    select min(id) minid from staging_place_scraped
    group by place_id) i
where o.id = i.minid;

delete from temple.place_scraped p
where exists(
    select 1 from  staging_place_scraped_unique s
    where s.place_id = p.place_id);

insert into temple.place_scraped(id, place_id, name, global_code, address, vicinity, geom, deity, deity_local, batch)
select 53399 + row_number() over (), place_id, name, plus_compound_code, vicinity, null, st_setsrid(st_point(u.lon, u.lat), 4326), null,null, 5
from  staging_place_scraped_unique u;

select * from temple.place_scraped where id = 53050;

select max(id) from temple.place_scraped; --53399
select min(id), max(id) from staging_place_scraped_unique;