--
-- Set up a DB similar to
--
-- psql> create database eubon_ebv;
-- psql> \l eubon_ebv

--
-- Tiles for the maps
--
CREATE TABLE tiles(
  z smallint NOT NULL,
  x smallint NOT NULL,
  y smallint NOT NULL,
  x1 integer NOT NULL,
  y1 integer NOT NULL,
  year smallint NOT NULL,
  dataset_key uuid NOT NULL,
  PRIMARY KEY(z,x,y,x1,y1,year,dataset_key)
);
CREATE INDEX lookup_tiles ON tiles(z,x,y,year);

--
-- Dataset titles, existing only to allow easy summary lists, ordered alphabetically
--
CREATE TABLE datasets(
  dataset_key uuid NOT NULL,
  title text NOT NULL,
  PRIMARY KEY(dataset_key)
);
CREATE INDEX lookup_by_title ON datasets(title);

-- psql> \copy datasets from 'datasets.tsv';
-- psql> \copy tiles from 'tiles.tsv';
