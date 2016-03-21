--
-- Set up a DB similar to
--
-- MariaDB [(none)]> create database eubon_ebv;
-- MariaDB [(none)]> use eubon_ebv;
-- MariaDB [eubon_ebv]> create user 'eubon'@'%' identified by '****';
-- MariaDB [eubon_ebv]> grant all on eubon_ebv.* to 'eubon'@'%' identified by '****';
-- MariaDB [eubon_ebv]> flush privileges;

--
-- Tiles for the maps
--
CREATE TABLE tiles(
  z TINYINT UNSIGNED NOT NULL,
  x TINYINT UNSIGNED NOT NULL,
  y TINYINT UNSIGNED NOT NULL,
  x1 MEDIUMINT UNSIGNED NOT NULL,
  y1 MEDIUMINT UNSIGNED NOT NULL,
  year SMALLINT UNSIGNED NOT NULL,
  dataset_key CHAR(36) NOT NULL,
  PRIMARY KEY(z,x,y,x1,y1,year,dataset_key),
  INDEX lookup(z,x,y,year)
) ENGINE = MyISAM;

--
-- Dataset titles, existing only to allow easy summary lists, ordered alphabetically
--
CREATE TABLE datasets(
  dataset_key CHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  PRIMARY KEY(dataset_key),
  INDEX lookupByTitle(title)
) ENGINE = MyISAM;

-- MariaDB [eubon_ebv]> load data local infile '/tmp/tiles.csv' into table tiles;
-- MariaDB [eubon_ebv]> load data infile '/tmp/datasets.csv' into table datasets;


