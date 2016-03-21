package org.gbif.eubon.udf;

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
import org.apache.hadoop.hive.serde2.objectinspector.primitive.DoubleObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.WritableDoubleObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.WritableIntObjectInspector;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

/**
 * A UDF that allows the caller to determine which tile address a coordinate will fall on at a given zoom.
 */
public class TileAddressUdf extends GenericUDF {


  private ObjectInspectorConverters.Converter intConverter;
  private ObjectInspectorConverters.Converter doubleConverter;

  private final Object[] result = new Object[2];
  private final LongWritable xWritable = new LongWritable();
  private final LongWritable yWritable = new LongWritable();


  @Override
  public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
    if (arguments.length != 4) {
      throw new UDFArgumentLengthException("tileAddress() takes four arguments: lat, lon, zoom, tileSize");
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

    if ((arguments[3].getCategory() != ObjectInspector.Category.PRIMITIVE) || !arguments[3].getTypeName()
                                                                                           .equals(serdeConstants.INT_TYPE_NAME)) {
      throw new UDFArgumentException("tileAddress(): tileSize has to be an int");
    }


    doubleConverter = ObjectInspectorConverters.getConverter(arguments[0],
                                                             PrimitiveObjectInspectorFactory.writableDoubleObjectInspector);
    intConverter = ObjectInspectorConverters.getConverter(arguments[2],
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
    System.out.println("Calling...");
    double lat = ((DoubleWritable) doubleConverter.convert(arguments[0].get())).get();
    double lon = ((DoubleWritable) doubleConverter.convert(arguments[1].get())).get();
    int zoom = 0; ((IntWritable) intConverter.convert(arguments[2].get())).get();
    int tileSize = 4096; ((IntWritable) intConverter.convert(arguments[3].get())).get();

    Point point = mercatorPoint(mercator(lon, lat), tileSize);
    int res = 1 << zoom;
    int pixelX = (int) Math.floor(point.x * res);
    int pixelY = (int) Math.floor(point.y * res);

    xWritable.set(pixelX);
    yWritable.set(pixelY);
    return result;
  }

  public Point mercatorPoint(Point point, int tileSize) {
    int t2 = tileSize >> 1;
    return new Point(t2 + point.x * tileSize, t2 - point.y * tileSize);
  }

  public Point mercator(double lon, double lat) {
    double reprojectedLat = Math.min(Math.max(lat, -89.189), 89.189);
    reprojectedLat = Math.PI * (reprojectedLat / 180.0);
    reprojectedLat = 0.5 * Math.log(Math.tan(0.25 * Math.PI + 0.5 * reprojectedLat)) / Math.PI;
    double reprojectedLon = lon / 360.0;
    return new Point(reprojectedLon, reprojectedLat);
  }

  @Override
  public String getDisplayString(String[] children) {
    return "tileAddress";
  }

  //
  public static final class Point {

    double x;
    double y;

    private Point(double x, double y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this).add("x", x).add("y", y).toString();
    }
  }

}
