/*


Adapted from http://rexdouglass.com/spatial-hexagon-binning-in-postgis/
Snapping inspired by https://medium.com/@goldrydigital/hex-grid-algorithm-for-postgis-4ac45f61d093
*/

CREATE OR REPLACE PROCEDURE clipAreas(n int)
AS
$$
declare
    big_area     RECORD;
    smaller_area RECORD;
    diff_geom    geometry;
    diff_area    double precision;
    num_done     int = 0;
BEGIN
    for i IN 1..n
        loop
            FOR big_area IN
                SELECT id, geom, area
                from fsc_cluster_nonoverlapping o
                where clipped = 0
                  and area = (select max(area) from fsc_cluster_nonoverlapping where clipped = 0)
                limit 1
                LOOP
                    num_done = num_done + 1;
                    if (mod(num_done, 100) = 0) then
                        RAISE NOTICE '% clipped', num_done;
                    end if;
                    for smaller_area in
                        SELECT id, geom, area
                        from fsc_cluster_nonoverlapping
                        where id != big_area.id and
                              area < big_area.area
                          and st_intersects(geom, big_area.geom)
                        loop
                            diff_geom = st_difference(smaller_area.geom, big_area.geom);
                            diff_area = st_area(diff_geom);
                            if GeometryType(diff_geom) = 'POLYGON' and (trunc(diff_area * 1000000)) > 80 then
                                if (smaller_area.area - diff_area > 0.0000000000000005) then
                                    update fsc_cluster_nonoverlapping
                                    set geom = diff_geom,
                                        area = st_area(diff_geom)
                                    where id = smaller_area.id;
                                end if;
                            else
                                delete
                                from fsc_cluster_nonoverlapping
                                where id = smaller_area.id;
                            end if;
                        end loop;
                    update fsc_cluster_nonoverlapping set clipped = 1 where id = big_area.id;
                    commit;
                END LOOP;
        end loop;
END;
$$ LANGUAGE plpgsql;