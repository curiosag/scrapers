
-- level 1
insert into temple.region(id, parent_id, name, level, geom, type)
select s.id, s.id, s.name, 1, public.st_flipcoordinates(s.geom), 0 from lola_region_staging s
where s.name = 'Pakistan';

-- level 2
insert into temple.region(id, parent_id, name, level, geom, type)
select s.id, 50, s.name, 2, public.st_flipcoordinates(s.geom), 0 from lola_region_staging s;


-- level 3
insert into temple.region(id, parent_id, name, level, geom, type)
select s.id, r.id, s.name, 3, public.st_flipcoordinates(s.geom), 0 from lola_region_staging s join temple.region r on s.id > 55 and s.name_sup = r.name;

-- level 3 or so

insert into temple.region(id, parent_id, name, level, geom, type)
select l.id, s.id, l.name, 3, public.st_flipcoordinates(l.geom), 0 from lola_region_staging l,
                                                                        (select * from temple.region where parent_id in (select id from temple.region where parent_id = 38) ) s
where s.name = l.name_sup;


insert into temple.region(id, parent_id, name, level, geom, type)
select l.id, s.id, l.name, 3, public.st_flipcoordinates(l.geom), 0
from lola_region_staging l,
     (select name, id from temple.region r where r.parent_id  = 38 ) s
where s.name = l.name_sup;

select distinct name_sup from lola_region_staging;

with recursive country as (
    select r.id, r.name, r.level
    from temple.region r
    where name = 'Pakistan'
    union
    select s.id, s.name, s.level
    from temple.region s
             inner join country c on s.parent_id = c.id
) select * from country;