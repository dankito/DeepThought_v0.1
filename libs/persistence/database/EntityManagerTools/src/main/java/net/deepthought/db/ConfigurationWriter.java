package net.deepthought.db;

import com.cedarsoftware.util.io.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 24/08/15.
 */
public class ConfigurationWriter extends JsonWriter {


  public static String objectToJson(Object item) throws IOException
  {
    return objectToJson(item, new HashMap<String, Object>());
  }

  public static String objectToJson(Object item, Map<String, Object> optionalArgs) throws IOException
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    JsonWriter writer = new ConfigurationWriter(stream, optionalArgs);
    writer.write(item);
    writer.close();
    return new String(stream.toByteArray(), "UTF-8");
  }

  public ConfigurationWriter(OutputStream out) throws IOException {
    super(out);
  }

  public ConfigurationWriter(OutputStream out, Map<String, Object> optionalArgs) throws IOException {
    super(out, optionalArgs);
  }

  @Override
  protected void writeObject(Object obj, boolean showType) throws IOException {
    if(obj instanceof Field) { // TODO: as well Constructors; Annotations
      writeReflectionField((Field) obj);
    }
    else if(obj instanceof Method) {
      writeReflectionMethod((Method) obj);
    }
    else if(obj instanceof Constructor) {
      writeReflectionConstructor((Constructor)obj);
    }
    else
      super.writeObject(obj, showType);
  }

  protected void writeReflectionField(Field field) {
    try {
      Class declaringClass = field.getDeclaringClass();
      String className = declaringClass.getName();
      String fieldName = field.getName();

      final Writer out = this.out;

      out.write('{');
      tabIn(out);
      JsonWriter.writeType(field, out, null);

      out.write(", \"" + ConfigurationReader.DeclaringClassFieldName + "\":\"" + className + "\"");
      out.write(", \"" + ConfigurationReader.FieldNameFieldName + "\":\"" + fieldName + "\"");

      tabOut(out);
      out.write('}');
    } catch(Exception ex) {
      String error = ex.getMessage();
    }
  }

  protected void writeReflectionMethod(Method method) {
    try {
      Class declaringClass = method.getDeclaringClass();
      String className = declaringClass.getName();
      String methodName = method.getName();
      Class[] parameters = method.getParameterTypes();

      final Writer out = this.out;

      out.write('{');
      tabIn(out);
      JsonWriter.writeType(method, out, null);

      out.write(", \"" + ConfigurationReader.DeclaringClassFieldName + "\":\"" + className + "\"");
      out.write(", \"" + ConfigurationReader.MethodNameFieldName + "\":\"" + methodName + "\"");

      out.write(", \"" + ConfigurationReader.ParametersFieldName + "\":[");

      for(int i = 0; i < parameters.length; i++) {
        out.write("{\"" + ConfigurationReader.ParameterFieldName + "\":\"" + parameters[i].getName() + "\"}");
        if(i < parameters.length - 1)
          out.write(",");
      }

      out.write(']');

      tabOut(out);
      out.write('}');
    } catch(Exception ex) {
      String error = ex.getMessage();
    }
  }

  protected void writeReflectionConstructor(Constructor constructor) {
    try {
      Class declaringClass = constructor.getDeclaringClass();
      String className = declaringClass.getName();
      Class[] parameters = constructor.getParameterTypes();

      final Writer out = this.out;

      out.write('{');
      tabIn(out);
      JsonWriter.writeType(constructor, out, null);

      out.write(", \"" + ConfigurationReader.DeclaringClassFieldName + "\":\"" + className + "\"");

      out.write(", \"" + ConfigurationReader.ParametersFieldName + "\":[");

      for(int i = 0; i < parameters.length; i++) {
        out.write("{\"" + ConfigurationReader.ParameterFieldName + "\":\"" + parameters[i].getName() + "\"}");
        if(i < parameters.length - 1)
          out.write(",");
      }

      out.write(']');

      tabOut(out);
      out.write('}');
    } catch(Exception ex) {
      String error = ex.getMessage();
    }
  }
}
