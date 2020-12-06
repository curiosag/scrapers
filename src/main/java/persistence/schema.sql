drop table scrapeSlice;
create table scrapeSlice
(
  id        INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  dtFrom    varchar(10)      not null,
  dtTo      varchar(10)      not null,
  pagesDone INTEGER UNSIGNED not null default 0,
  sliceDone INTEGER UNSIGNED not null default 0
);

update scrapeSlice
set pagesDone = 0,
    sliceDone = 0;

create table scrapeStatus
(
  id       INTEGER UNSIGNED PRIMARY KEY,
  lastPage INTEGER UNSIGNED
);
insert into scrapeStatus (id, lastPage)
values (1, 0);

