package org.gbif.eubon.ebv.resource;

import org.gbif.eubon.ebv.data.DataDAO;
import org.gbif.eubon.ebv.data.DatasetName;
import org.gbif.eubon.ebv.data.GridCount;

import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import no.ecc.vectortile.VectorTileEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple resource that returns a demo tile.
 */
@Path("/")
@Singleton
public final class TileResource {

  private static final Logger LOG = LoggerFactory.getLogger(TileResource.class);

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
  private static final int TILE_SIZE = 256;
  private static final Random RANDOM = new Random();

  private final DataDAO dataDao;

  public TileResource(DataDAO dataDao) {
    this.dataDao = dataDao;
  }

  @GET
  @Path("{z}/{x}/{y}.pbf")
  @Timed
  @Produces("application/x-protobuf")
  public byte[] tile(
    @PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y,
    @QueryParam("minYear") Integer minYear, @QueryParam("maxYear") Integer maxYear,
    @Context HttpServletResponse response
  ) {

    int minYearAsInt = minYear == null ? 1900 : minYear;
    int maxYearAsInt = maxYear == null ? 2020 : maxYear; // if this runs longer than 2020, it'd be a mirable

    long start = System.currentTimeMillis();
    List<GridCount> cells = dataDao.lookup(z, x, y, minYearAsInt, maxYearAsInt);
    LOG.debug("{},{},{} with years {}-{} returned {} cells in {} msecs", z, x, y, minYearAsInt, maxYearAsInt,
              cells.size(), (System.currentTimeMillis() - start));

    // open the tiles to the world (especially your friendly localhost developer!)
    response.addHeader("Allow-Control-Allow-Methods", "GET,OPTIONS");
    response.addHeader("Access-Control-Allow-Origin", "*");

    start = System.currentTimeMillis();

    int tileSize = 256;
    VectorTileEncoder encoder = new VectorTileEncoder(256, 0, true);

    int zoomAhead = 2;
    int cellSize = tileSize / (1 << zoomAhead);
    int cellsPerTile = tileSize / cellSize;
    LOG.debug("Cell size {}", cellSize);

    List<Polygon> polys = Lists.newArrayList();
    for (GridCount cell : cells) {

      // grids are always addressed from world 0,0 so remove the offsets to reference from 0,0 on the tile itself
      int x1 = cell.getX() - x * cellsPerTile;
      int y1 = cell.getY() - y * cellsPerTile;

      // now project them onto the grid instead of pixels
      int left = x1 * cellSize;
      int top = y1 * cellSize;

      Coordinate[] coordinates = {
        new Coordinate(left, top),
        new Coordinate(left + cellSize, top),
        new Coordinate(left + cellSize, top + cellSize),
        new Coordinate(left, top + cellSize),
        new Coordinate(left, top)
      };
      LinearRing linear = GEOMETRY_FACTORY.createLinearRing(coordinates);
      Polygon poly = new Polygon(linear, null, GEOMETRY_FACTORY);
      polys.add(poly);

      Map<String, String> meta = Maps.newHashMap();
      meta.put("id", z + "/" + x + "/" + y + "/" + cell.getX() + "/" + cell.getY());
      meta.put("count", Integer.toString(cell.getCount()));
      encoder.addFeature("coverage", meta, poly);

    }

    /*
    Map<String, String> meta = Maps.newHashMap();
    meta.put("name", "fake");
    MultiPolygon multiPolygon = new MultiPolygon(Iterables.toArray(polys, Polygon.class), GEOMETRY_FACTORY);
    encoder.addFeature("coverage", meta, multiPolygon);
    */

    byte[] tile = encoder.encode();
    LOG.debug("Encoded in {} msecs", (System.currentTimeMillis() - start));

    return tile;
  }

  @GET
  @Path("{z}/{x}/{y}/{x1}/{y1}.json")
  @Timed
  @Produces("application/json")
  public String datasets(
    @PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y,
    @PathParam("x1") int x1, @PathParam("y1") int y1,
    @QueryParam("minYear") Integer minYear, @QueryParam("maxYear") Integer maxYear,
    @Context HttpServletResponse response
  ) {

    int minYearAsInt = minYear == null ? 1900 : minYear;
    int maxYearAsInt = maxYear == null ? 2020 : maxYear; // if this runs longer than 2020, it'd be a mirable

    long start = System.currentTimeMillis();
    List<DatasetName> datasets = dataDao.datasets(z, x, y, x1, y1, minYearAsInt, maxYearAsInt);
    LOG.info("{},{},{},{},{} with years {}-{} returned {} cells in {} msecs",
             z,
             x,
             y,
             x1,
             y1,
             minYearAsInt,
             maxYearAsInt,
             datasets.size(),
             (System.currentTimeMillis() - start));

    // open the tiles to the world (especially your friendly localhost developer!)
    response.addHeader("Allow-Control-Allow-Methods", "GET,OPTIONS");
    response.addHeader("Access-Control-Allow-Origin", "*");

    StringBuffer json = new StringBuffer("{\"datasets\":[");
    for (int i = 0; i < datasets.size(); i++) {
      DatasetName dataset = datasets.get(i);

      String name = dataset.getName().replaceAll("\"", "'");
      name = name.replaceAll("\\{", "(");
      name = name.replaceAll("\\}", ")");
      name = name.replaceAll("\\[", "(");
      name = name.replaceAll("\\]", "(");

      json.append("{\"key\":\"" + dataset.getKey() + "\",\"name\":\"" + name + "\"}");
      if (i + 1 < datasets.size()) {
        json.append(",");
      }
    }
    json.append("]}");
    return json.toString();
  }

}
