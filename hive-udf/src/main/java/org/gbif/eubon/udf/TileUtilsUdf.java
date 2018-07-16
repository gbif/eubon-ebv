package org.gbif.eubon.udf;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

/**
 * A UDF that allows the caller to determine which tile address a coordinate will fall on at a given zoom.
 */
public class TileUtilsUdf extends GenericUDF {


  private final Object[] result = new Object[2];
  private final LongWritable xWritable = new LongWritable();
  private final LongWritable yWritable = new LongWritable();

  private ObjectInspectorConverters.Converter latConverter;
  private ObjectInspectorConverters.Converter lngConverter;
  private ObjectInspectorConverters.Converter zoomConverter;

  @Override
  public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
    if (arguments.length != 3) {
      throw new UDFArgumentLengthException("tileAddress() takes four arguments: lat, lon, zoom");
    }

    List<String> fieldNames = new ArrayList<String>();
    List<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();


    if ((arguments[0].getCategory() != ObjectInspector.Category.PRIMITIVE) || !arguments[0].getTypeName()
                                                                                 .equals(serdeConstants.DOUBLE_TYPE_NAME)) {
      throw new UDFArgumentException("tileAddress(): lat has to be double");
    }

    if ((arguments[1].getCategory() != ObjectInspector.Category.PRIMITIVE) || !arguments[1].getTypeName()
                                                                                 .equals(serdeConstants.DOUBLE_TYPE_NAME)) {
      throw new UDFArgumentException("tileAddress(): lon has to be double");
    }

    if ((arguments[2].getCategory() != ObjectInspector.Category.PRIMITIVE) || !arguments[2].getTypeName()
                                                                                 .equals(serdeConstants.INT_TYPE_NAME)) {
      throw new UDFArgumentException("tileAddress(): zoom has to be an int");
    }



    latConverter = ObjectInspectorConverters.getConverter(arguments[0],
                                                          PrimitiveObjectInspectorFactory.writableDoubleObjectInspector);
    lngConverter = ObjectInspectorConverters.getConverter(arguments[1],
                                                          PrimitiveObjectInspectorFactory.writableDoubleObjectInspector);
    zoomConverter = ObjectInspectorConverters.getConverter(arguments[2],
                                                            PrimitiveObjectInspectorFactory.writableIntObjectInspector);

    fieldNames.add("x");
    fieldNames.add("y");
    fieldOIs.add(PrimitiveObjectInspectorFactory.writableLongObjectInspector);
    fieldOIs.add(PrimitiveObjectInspectorFactory.writableLongObjectInspector);
    result[0] = xWritable;
    result[1] = yWritable;

    return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
  }

  @Override
  public Object evaluate(DeferredObject[] arguments) throws HiveException {
    double lat = ((DoubleWritable) latConverter.convert(arguments[0].get())).get();
    double lng = ((DoubleWritable) lngConverter.convert(arguments[1].get())).get();
    if (!isPlottable(lat,lng)) {
      return null;
    }
    int zoom = ((IntWritable) zoomConverter.convert(arguments[2].get())).get();

    Point2D normalizedPixels = toNormalisedPixelCoords(lat, lng);
    int scale = 1 << zoom;
    // truncating to int removes the fractional pixel offset
    int x = (int) (normalizedPixels.getX() * scale);
    int y = (int) (normalizedPixels.getY() * scale);

    xWritable.set(x);
    yWritable.set(y);
    return result;
  }

  @Override
  public String getDisplayString(String[] strings) {
    return "tileAddress()";
  }

  /**
   * Google maps cover ±85 degrees only.
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
