package org.gbif.eubon.udf;

import java.awt.geom.Point2D;

public class Test {

  public static void main(String[] args) {
    double lat = 45;
    double lon = 90;
    int tileSize = 4096;
    int zoom = 1;

    Point2D normalizedPixels = toNormalisedPixelCoords(lat, lon);

    int scale = 1 << zoom;

    // truncating to int removes the fractional pixel offset
    int x = (int) (normalizedPixels.getX() * scale);
    int y = (int) (normalizedPixels.getY() * scale);
    System.out.println(x + ":" + y);
  }

  /**
   * Google maps cover +/- 85 degrees only.
   * @return true if the location is plottable on a map
   */
  static boolean isPlottable(Double lat, Double lng) {
    return lat != null && lng != null && lat >= -85d && lat <= 85d && lng >= -180 && lng <= 180;
  }

  /**
   * Returns the lat/lng as an "Offset Normalized Mercator" pixel coordinate.
   * This is a coordinate that runs from 0..1 in latitude and longitude with 0,0 being
   * top left. Normalizing means that this routine can be used at any zoom level and
   * then multiplied by a power of two to get actual pixel coordinates.
   */
  static Point2D toNormalisedPixelCoords(double lat, double lng) {
    if (lng > 180) {
      lng -= 360;
    }
    lng /= 360;
    lng += 0.5;
    lat = 0.5 - ((Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI) / 2.0);
    return new Point2D.Double(lng, lat);
  }

}
