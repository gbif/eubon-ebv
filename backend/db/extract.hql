--
-- Scripts for extracting the summary data from the occurrence data in Hive
-- Requires that the Hive UDF is built and registered.
--

CREATE TEMPORARY FUNCTION tile AS 'org.gbif.eubon.udf.TileUtilsUdf';

--
--  Improve performance by creating a temp table
--
CREATE TABLE eubon.source AS SELECT
  decimalLatitude, decimalLongitude, year, datasetKey
FROM prod_f.occurrence_hdfs
WHERE
  decimalLatitude IS NOT NULL AND decimalLatitude BETWEEN -85 AND 85 AND
  decimalLongitude IS NOT NULL AND decimalLongitude BETWEEN -180 AND 180 AND
  hasGeospatialIssues = false AND
  year IS NOT NULL AND year >= 1900 AND
  basisOfRecord != "FOSSIL_SPECIMEN" AND basisOfRecord != "LIVING_SPECIMEN";

SET hive.exec.parallel = true;

CREATE TABLE eubon.tiles ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' AS

SELECT 0 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 0) as a,
    tile(decimalLatitude, decimalLongitude, 2) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 1 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 1) as a,
    tile(decimalLatitude, decimalLongitude, 3) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 2 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 2) as a,
    tile(decimalLatitude, decimalLongitude, 4) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 3 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 3) as a,
    tile(decimalLatitude, decimalLongitude, 5) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 4 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 4) as a,
    tile(decimalLatitude, decimalLongitude, 6) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 5 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 5) as a,
    tile(decimalLatitude, decimalLongitude, 7) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 6 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 6) as a,
    tile(decimalLatitude, decimalLongitude, 8) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 7 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 7) as a,
    tile(decimalLatitude, decimalLongitude, 9) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

UNION ALL

SELECT 8 AS zoom, a.x AS x, a.y AS y, b.x AS x1, b.y AS y2, year, datasetKey
FROM (
  SELECT
    tile(decimalLatitude, decimalLongitude, 8) as a,
    tile(decimalLatitude, decimalLongitude, 10) as b,
    10 * (floor(year)/10) as year,
    datasetKey
  FROM eubon.source
) t1
GROUP BY a.x, a.y, b.x, b.y, year, datasetKey

