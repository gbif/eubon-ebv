# EUBON-EBV

*European Union Biodiversity Observation Network – Essential Biodiversity Variables*

The current pilot project is a map based browser allowing users to discover datasets that may be suitable for Species Distribution EBVs.  Users can explore using the map interface, adjust time sliders, and then select a cell of interest which will list the datasets that intersect.

The application is a simple Dropwizard based app, sitting on a MySQL database containing a few 10's of millions of rows.  The Dropwizard application serves mapbox VectorTiles to the browser for rendering using Mapbox WebGL JS libraries.

To prepare the data, Hive HQL using a custom UDF is run and the table extracted and loaded into MySQL.  Additionally a simple export of the Registry DB (PostgreSQL) is used.  At this point, information is static and a one time export is made.

http://eubon-ebv.gbif.org/
