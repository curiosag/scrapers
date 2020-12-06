drop table googleScholarItem;
create table googleScholarItem
(
  id              INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  pageNumber      INTEGER UNSIGNED   not null,
  depth           INTEGER UNSIGNED   not null,

  cid             varchar(15) unique not null,
  title           varchar(512)       not null,
  description     varchar(2048),
  year            INTEGER UNSIGNED   not null,
  authors         varchar(1024)      not null,
  numberCitations INTEGER UNSIGNED   not null,
  urlCitations    varchar(120)       not null
);

drop table googleScholarCitation;
create table googleScholarCitation
(
  id         INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  cid_citing varchar(15) not null,
  cid_cited  varchar(15) not null,
  UNIQUE (cid_citing, cid_cited)
);


