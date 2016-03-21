--
-- Run the following SQL, export it as tab delimited with no headers
-- suitable for loading into MySQL.
-- Because we are going to tab files, remove lines breaks, tabs and clean double spaces.
--
SELECT key, trim(regexp_replace(regexp_replace(title, E'[\\n\\r]+', ' ', 'g' ), '\s+', ' ', 'g')) AS title
FROM dataset
WHERE deleted IS NULL;
