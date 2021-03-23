select count(*) from jk_staging; --1165

select count(*), a.name,a.plus_compound_code,a.vicinity from jk_staging a, places_staging b
where (a.name = b.name or a.name is null and b.name is null )
  and (a.plus_compound_code = b.plus_compound_code or a.plus_compound_code is null and b.plus_compound_code is null )
  and (a.vicinity = b.vicinity or a.vicinity is null and b.vicinity is null )
group by a.name,a.plus_compound_code,a.vicinity
having count(*) > 1;


SELECT public.ST_GeomFromText('LINESTRING(-71.160281 42.258729,-71.160837 42.259113,-71.161144 42.25932)');

alter sequence temple.contact_id_seq minvalue 230 start 230 increment by 1 restart 230;

set search_path = public;

drop table fast_scan_clusters;
create table fast_scan_clusters as
select st_convexhull(ST_Collect(st_buffer(geom, 0.015, 'quad_segs=8'))) as geom, count(1), clst_id
from (select p.geom, ST_ClusterDBSCAN(geom, eps := 0.02, minpoints := 1) OVER () AS clst_id
      from temple.fast_scan_lola_place p
     ) s
group by clst_id;

select count(1)
from fast_scan_clusters;

create index pfast_scan_clusters_geom_idx
    on fast_scan_clusters using gist (geom);


drop table fsc_nonoverlapping;
create table fsc_nonoverlapping as
select *
from fast_scan_clusters f
where not exists(
        select 1
        from fast_scan_clusters ff
        where ff.clst_id != f.clst_id
          and st_within(f.geom, ff.geom));