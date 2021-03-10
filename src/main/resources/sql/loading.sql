

UPDATE public."n_temple_list_Telengana" set "Temple Name" = substr("Temple Name", 0, 120), "Deity Name" = substr("Deity Name", 0,   120);

insert into place_provided(dataload_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name, description, geom, contact_id, incharge)
select 2, "Sl No.", "Temple Name", 'India', 'Telengana', "District", "Address", "Main Deity", "Deity Name", null, null, null, null from public."n_temple_list_Telengana" t;

insert into place_provided(dataload_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name, description, geom, contact_id, incharge)
select 13, row_number() over (), substr(trim(temple), 0, 120), 'India', trim(state), trim("District"), trim(locality),substr(trim("Main Deity"), 0, 120), null, trim(comment), null, null, null from public."temple_list_Tripura" t;

insert into place_provided(dataload_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name, description, geom, contact_id, incharge)
select 14, "SlNo.", substr("Temple Name", 0, 120), 'India', 'Andhra Pradesh', "District", "Address", substr("Main Deity", 0, 120),  null, null, null,null, null from public.n_temple_list_andhra t;

insert into place_provided(dataload_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name, description, geom, contact_id, incharge)
select 15, "No", substr("Business Name", 0, 120), 'India', 'Andhra Pradesh', null, "Address", null,  null, null,
       public.st_geomfromtext('POINT(' || "Lat" || ' ' || "Long" || ')', 4326),
       null, null from public.templs_in_535591_andhra t
where "Long" is not null;

insert into place_provided(dataload_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name, description, geom, contact_id, incharge)
select 15, "No", substr("Business Name", 0, 120), 'India', 'Andhra Pradesh', null, "Address", null,  null, null,
       null,
       null, null from public.templs_in_535591_andhra t
where "Long" is  null;