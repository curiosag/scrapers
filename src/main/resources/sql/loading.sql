insert into dataload (id, name, comment, entered, datasource_id) values (20, 'r_temple_list_Bengal_a.csv', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);
insert into dataload (id, name, comment, entered, datasource_id) values (21,  'r_temple_list_Jharkhand.csv', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);
insert into dataload (id, name, comment, entered, datasource_id) values (22, 'r_temple_list_Kerala.csv', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);
insert into dataload (id, name, comment, entered, datasource_id) values (23, 'r_temple_list_Madhya Pradesh.csv', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);
insert into dataload (id, name, comment, entered, datasource_id) values (24, 'r_temple_list_outside India_05_01.csv', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);
insert into dataload (id, name, comment, entered, datasource_id) values (25, 't_temple_list_Himachal.xlsx', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);
insert into dataload (id, name, comment, entered, datasource_id) values (26, 't_temple_list_JK.xlsx', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);
insert into dataload (id, name, comment, entered, datasource_id) values (27, 't_temple_list_Karnataka.xlsx', 'I gave in to the world champion in being stubborn', CURRENT_DATE, 1);

insert into place_provided(dataload_id, proprietary_id, name, country, state, district, address, deity_name, deity_local_name, description, geom, contact_id, incharge)
select 27, "Sl No.", substr("Temple Name", 0, 120), 'India', 'Karnataka', "District", "Address", "Main Deity", "Deity Name", null, null, null,  null from "t_temple_list_Karnataka" t;

alter sequence places_noloc_id_seq restart with 79057;

select max(id) from place_provided;


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