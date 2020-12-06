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

drop table scrapeRow;
create table scrapeRow
(
  id                        INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  indicationOfProduct       varchar(512)     not null,
  st13                      varchar(25)      not null,
  designNumber              varchar(10)      not null,
  ownerName                 varchar(1024)    not null,
  ownerCountryCode          varchar(3),
  ownerAddress              varchar(1024),

  representativeName        varchar(120)     not null,
  representativeCountryCode varchar(3),
  representativeAddress     varchar(512),

  designOffice              varchar(4),
  designatedTerritory       varchar(120),
  locarnoClassification     varchar(8)       not null,
  status                    varchar(40),

  applicationDate           varchar(10)      not null,
  registrationDate          varchar(10)      not null,
  publicationDate           varchar(10),
  expiryDate                varchar(10),

  sliceNumber               INTEGER UNSIGNED not null,
  pageNumber                INTEGER UNSIGNED not null,

  urlOwnerDetails           varchar(120)     not null,
  urlRepresentativeDetails  varchar(120)     not null
);



