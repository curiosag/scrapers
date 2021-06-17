create view admin_rolemenmbers as
SELECT r.rolname as username,r1.rolname as "role"
FROM pg_catalog.pg_roles r JOIN pg_catalog.pg_auth_members m
                                ON (m.member = r.oid)
                           JOIN pg_roles r1 ON (m.roleid=r1.oid)
WHERE r.rolcanlogin
ORDER BY 1;

create view admin_grants_standard as
SELECT grantor, grantee, table_schema, table_name, privilege_type
FROM information_schema.table_privileges
where grantee='standard';
