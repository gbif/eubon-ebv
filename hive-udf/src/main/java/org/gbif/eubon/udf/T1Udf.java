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
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.LongWritable;

/**
 * A test UDF
 */
public class T1Udf extends GenericUDF {


  private ObjectInspectorConverters.Converter doubleConverter;

  private final Object[] result = new Object[2];
  private final LongWritable xWritable = new LongWritable();
  private final LongWritable yWritable = new LongWritable();


  @Override
  public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
    if (arguments.length != 1) {
      throw new UDFArgumentLengthException("test() takes one arguments: double");
    }

    List<String> fieldNames = new ArrayList<String>();
    List<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();


    if ((arguments[0].getCategory() != ObjectInspector.Category.PRIMITIVE) || !arguments[0].getTypeName()
                                                                                 .equals(serdeConstants.DOUBLE_TYPE_NAME)) {
      throw new UDFArgumentException("test(): lat has to be double");
    }

    doubleConverter = ObjectInspectorConverters.getConverter(arguments[0],
                                                             PrimitiveObjectInspectorFactory.writableDoubleObjectInspector);


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
    System.out.println(arguments.length);
    System.out.println(arguments[0].get());
    System.out.println(arguments[0].get().getClass());

    double lat = ((DoubleWritable) doubleConverter.convert(arguments[0].get())).get();

    System.out.println(lat);
    xWritable.set(0);
    yWritable.set(0);
    return result;
  }

  @Override
  public String getDisplayString(String[] strings) {
    return("Test");
  }
}
