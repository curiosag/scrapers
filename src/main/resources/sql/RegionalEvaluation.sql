select count(1)
from temple.place_scraped p
         join temple.region r on r.name = 'India' and public.st_within(p.geom, r.geom);


with recursive region as (
    select r.id, r.name, r.level
    from temple.region r
    where level = 1
    union
    select s.id, s.name, s.level
    from temple.region s
             inner join region c on s.parent_id = c.id
)
select *
from region;

create view r1 as
select *
from temple.region r
where level = 1;

create view r2 as
select *
from temple.region r
where level = 2;

create view r3 as
select *
from temple.region r
where level = 3;

create or replace view r123 as
select r1.name as country, r2.name as state, r3.name as district, r1.id as country_id, r2.id as state_id , r3.id as district_id, r3.geom district_geom
from r1
         join r2 on r1.id = r2.parent_id
         join r3 on r2.id = r3.parent_id
order by country, state, district;

create or replace view r12 as
select r1.name as country, r2.name as state, r1.id as country_id, r2.id as state_id, r2.geom state_geom
from r1
         join r2 on r1.id = r2.parent_id
order by country, state;

create or replace view scraped_by_country_state_district as
    select r.country, r.state, r.district, count(1) from r123 r join temple.place_scraped p on st_within(p.geom, r.district_geom)
group by r.country, r.state, r.district
order by r.country, r.state, r.district;

create or replace view scraped_by_country_state as
select r.country, r.state, count(1) from r12 r join temple.place_scraped p on st_within(p.geom, r.state_geom)
group by r.country, r.state
order by r.country, r.state;

create or replace view scraped_by_country as
select r.name, count(1) from r1 r join temple.place_scraped p on st_within(p.geom, r.geom)
group by r.name
order by r.name;