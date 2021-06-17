copy fsc_staging
    from '/var/pg_fileio/centralIndiaFat.csv'
delimiter ';' quote '"' CSV;

select count(1) from fsc_staging;

insert into history_fsc_place select * from fsc_place on conflict do nothing ;
delete from fsc_place;
insert into fsc_place (geom) select st_setsrid(st_point(lo, la), 4326) from fsc_staging;

insert into fsc_area(geom)
select ST_GeometryN(geom,28) from temple.region where name = 'India';

select st_numgeometries(geom) from temple.region where name = 'India';

insert into history_scrape_job
select id, area, busy, current_lat, current_lon, started, finished, place_type, batch
from temple.scrape_job;

delete from temple.scrape_job;

select min(id) from fsc_area;
select max(id) from history_scrape_job; --

insert into temple.scrape_job(id, area, busy, current_lat, current_lon, started, finished, place_type, batch)
select (select max(id) from history_scrape_job) + (fsc.id - (select min(id) from fsc_area) + 1),geom, 0, 0,0,null, null, 'any', 7
from fsc_area fsc;

create table history_scrape_job as select * from temple.scrape_job;
delete from temple.scrape_job;

insert into temple.scrape_job(area, busy, current_lat, current_lon, place_type, batch)
select geom, 0, 0,0, 'TYPE_HINDU_TEMPLE', 6
from fsc_area_selected fsc;

update temple.scrape_job set place_type ='TYPE_HINDU_TEMPLE' where place_type ='hindu_temple';

delete from staging_place_scraped;
delete from temple.scrape_job where batch=3;


insert into history_fsc_place (id, geom, batch)
select id, geom, batch from fsc_place;
delete from fsc_place;
delete from fsc_staging;

SELECT version();

copy fsc_staging from '/var/pg_fileio/scanSouthIndiaGaps.csv' CSV delimiter ';';
select count(1) from fsc_staging;

insert into fsc_place (batch, geom)
select 6, ST_SetSRID(ST_MakePoint(s.lo, s.la),4326)
from fsc_staging s;

delete from public.fsc_clusters;

insert into public.fsc_clusters(geom, count, clst_id)
select st_convexhull(ST_Collect(st_buffer(geom, 0.008, 'quad_segs=8'))) as geom, count(1), clst_id
from (select p.geom, public.ST_ClusterDBSCAN(p.geom, eps := 0.014, minpoints := 1) OVER () AS clst_id
      from fsc_place p join temple.region r on r.name = 'India' and st_within(p.geom, r.geom)
     ) s
group by clst_id;

delete from public.fsc_cluster_nonoverlapping;
insert into public.fsc_cluster_nonoverlapping (geom, count, clst_id, area)
select geom, count, clst_id, st_area(geom)
from public.fsc_clusters f
where not exists(
        select 1
        from public.fsc_clusters ff
        where ff.clst_id != f.clst_id
          and public.st_within(f.geom, ff.geom));

select count(1) from fsc_cluster_nonoverlapping;

delete from fsc_grid;
delete from fsc_grid_1;
delete from fsc_grid_2;

insert into fsc_grid_1
select distinct sq.geom from
    st_squaregrid(
            0.045, --0.02
            (select ST_Envelope(r.geom)
             from temple.region r where name = 'India')
        ) as sq
        join temple.region r on r.name = 'India' and st_intersects(r.geom, sq.geom)
        join fsc_place fp on st_intersects(fp.geom, sq.geom);

create index fsc_grid_1_geom_idx on fsc_grid_1 using gist(geom);
create index fsc_grid_2_geom_idx on fsc_grid_2 using gist(geom);

select count(1) from fsc_grid_2;

delete from fsc_area_selected;

insert into fsc_area_selected(geom, area)
select distinct g2.geom,  st_area(g2.geom) as geom from fsc_grid_1 g1 join fsc_grid_2 g2 on st_within(g1.geom, g2.geom)
group by g2.geom
having count(1) = 4;

insert into fsc_area_selected(geom, area)
select distinct g1.geom,  st_area(g1.geom) as geom from fsc_grid_1 g1 where not exists(
    select 1 from fsc_area_selected s where st_within(g1.geom, s.geom));

insert into temple.scrape_job(area, busy, current_lat, current_lon, started, finished, place_type, batch)
select geom, 0, 0,0,null, null, 'TYPE_HINDU_TEMPLE', 7
from fsc_area_selected fsc;


insert into fsc_area_selected (geom, area)
select distinct g.geom, st_area(g.geom) -- distinct: join creates strange blow-up
from fsc_grid g
         join fsc_cluster_nonoverlapping c on trunc(area * 1000000) > 50000 and st_intersects(g.geom, c.geom)
where not exists (select 1 from fsc_area_selected sel where st_within(g.geom, sel.geom));


insert into temple.scrape_job(area, busy, current_lat, current_lon, started, finished, place_type, batch)
select geom, 0, 0,0,null, null, 'TYPE_HINDU_TEMPLE', 7
from fsc_grid_2 fsc;





create table g1
select distinct g2.geom as geom from fsc_grid_1 g1 join fsc_grid_2 g2 on st_within(g1.geom, g2.geom)
                                                   join fsc_cluster_nonoverlapping c
                                                        on trunc(area * 1000000) > 50000 and st_within(g2.geom, c.geom)


create table g2 as
select distinct g2.geom as geom from fsc_grid_1 g1 join fsc_grid_2 g2 on st_within(g1.geom, g2.geom)
                                                   join fsc_cluster_nonoverlapping c
                                                       on trunc(area * 1000000) > 50000 and st_within(g2.geom, c.geom)
group by g2.geom
having count(1) = 4;

create index g2_geom_idx on g2 using gist(geom);

select count(1) from g2;

delete from g1;
insert into  g1(geom)
select distinct st_union(g0.geom) as geom from fsc_grid g0 join fsc_grid_1 g1 on st_within(g0.geom, g1.geom)
                                                           join fsc_cluster_nonoverlapping c on trunc(area * 1000000) > 50000 and st_within(g1.geom, c.geom)
group by g1.geom
having count(1) = 4;

delete from fsc_area_selected;

insert into fsc_area_selected (geom, area)
select distinct g2.geom, st_area(g2.geom) -- distinct: join creates strange blow-up
from g2;

insert into fsc_area_selected (geom, area)
select distinct g1.geom, st_area(g1.geom) -- distinct: join creates strange blow-up
from g1 where not exists (select 1 from g2 where st_within(g1.geom, g2.geom));

insert into fsc_area_selected (geom, area)
select distinct g.geom, st_area(g.geom) -- distinct: join creates strange blow-up
from fsc_grid g
         join fsc_cluster_nonoverlapping c on trunc(area * 1000000) > 50000 and st_intersects(g.geom, c.geom)
where not exists (select 1 from fsc_area_selected sel where st_within(g.geom, sel.geom));

insert into fsc_area_selected (geom, area)
select geom, area from fsc_cluster_nonoverlapping c where trunc(area * 1000000) <= 50000;


delete from fsc_area_selected where area < 0.00041;

select (select sum(area) from fsc_area_selected) / (select sum(st_area(nn.area)) from temple.scrape_job nn where nn.batch = 6);

select count(1) from fsc_area_selected;