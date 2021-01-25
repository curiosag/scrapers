select count(*) from jk_staging; --1165

select count(*), a.name,a.plus_compound_code,a.vicinity from jk_staging a, places_staging b
where (a.name = b.name or a.name is null and b.name is null )
  and (a.plus_compound_code = b.plus_compound_code or a.plus_compound_code is null and b.plus_compound_code is null )
  and (a.vicinity = b.vicinity or a.vicinity is null and b.vicinity is null )
group by a.name,a.plus_compound_code,a.vicinity
having count(*) > 1;