package net.dankito.deepthought.data.persistence.json;

import com.cedarsoftware.util.io.JsonWriter;

import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ganymed on 02/05/15.
 */
public class DeepThoughtJsonWriter extends JsonWriter {

  /**
   * Convert a Java Object to a JSON String.
   *
   * @param item Object to convert to a JSON String.
   * @param optionalArgs (optional) Map of extra arguments indicating how dates are formatted,
   * what fields are written out (optional).  For Date parameters, use the public static
   * DATE_TIME key, and then use the ISO_DATE or ISO_DATE_TIME indicators.  Or you can specify
   * your own custom SimpleDateFormat String, or you can associate a SimpleDateFormat object,
   * in which case it will be used.  This setting is for both java.util.Date and java.sql.Date.
   * If the DATE_FORMAT key is not used, then dates will be formatted as longs.  This long can
   * be turned back into a date by using 'new Date(longValue)'.
   * @return String containing JSON representation of passed
   *         in object.
   * @throws java.io.IOException If an I/O error occurs
   */
  public static String objectToJson(Object item, Map<String, Object> optionalArgs) throws IOException
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    JsonWriter writer = new DeepThoughtJsonWriter(stream, optionalArgs);
    writer.write(item);
    writer.close();
    return new String(stream.toByteArray(), "UTF-8");
  }

  public DeepThoughtJsonWriter(OutputStream out, Map<String, Object> optionalArgs) throws IOException {
    super(out, optionalArgs);
  }

//  @Override
//  protected boolean writeField(Object obj, Writer out, boolean first, String fieldName, Field field) throws IOException {
////    if(obj instanceof Entry && "tags".equals(fieldName)) {
////      return writeEntryTags((Entry)obj, out, first, fieldName, field);
////    }
////    else if(obj instanceof Tag && "entries".equals(fieldName))
////      return writeTagEntries((Tag)obj, out, first, fieldName, field);
////    else
//      return super.writeField(obj, out, first, fieldName, field);
//  }
//
//  protected boolean writeEntryTags(Entry entry, Writer out, boolean first, String fieldName, Field field) {
//    return false;
//  }
//
//  protected boolean writeTagEntries(Tag tag, Writer out, boolean first, String fieldName, Field field) {
//    return false;
//  }


  @Override
  protected void writeObject(Object obj, boolean showType) throws IOException {
    super.writeObject(obj, showType);

    this.out.flush(); // flush buffer from time to time
  }

  @Override
  protected void writeFieldImpl(Object parent, Object field, boolean showType, String fieldName, Class type) throws IOException {
    if(parent instanceof DeepThoughtApplication) {
      if("lastLoggedOnUser".equals(fieldName) || "localDevice".equals(fieldName))
        writeBaseEntityIdReference((BaseEntity)field, showType, type);
      else
        writeImpl(field, showType, type);
    }
    else if(parent instanceof User && "deepThoughts".equals(fieldName)) {
        writeImpl(field, showType, type);
    }
    else if(parent instanceof DeepThought && (field instanceof BaseEntity == false || "topLevelCategory".equals(fieldName) || "topLevelEntry".equals(fieldName)))
      writeImpl(field, showType, type);
    else if(parent instanceof Person && ("entryPersonAssociations".equals(fieldName) || "referenceBasePersonAssociations".equals(fieldName))) {
      writeImpl(field, showType, type);
    }
    else if(field instanceof Collection)
      writeCollectionItemIdReferences((Collection) field, showType, type);
    else if(field instanceof BaseEntity)
      writeBaseEntityIdReference((BaseEntity) field, showType, type);
    else
      writeImpl(field, showType, type);
  }

  protected void writeCollectionItemIdReferences(Collection col, boolean showType, Class type) throws IOException {
    final Writer out = this.out;

    out.write('{');
    tabIn(out);

//    out.write(',');
    newLine(out);

    writeType(col, out, type);
    out.write(',');
    newLine(out);

    out.write("\"@items\":[");

    tabIn(out);

    Iterator i = col.iterator();

    while (i.hasNext())
    {
      Object item = i.next();
      if(item instanceof BaseEntity) {
        writeBaseEntityIdReference((BaseEntity)item, showType, item.getClass());
      }
      else {
        writeCollectionElement(item);
      }

      if (i.hasNext())
      {
        out.write(',');
        newLine(out);
      }

    }

    tabOut(out);
    out.write(']');

    tabOut(out);
    out.write("}");
  }

  protected void writeBaseEntityIdReference(BaseEntity field, boolean showType, Class type) throws IOException {
    final Writer out = this.out;

    out.write('{');
    tabIn(out);

    writeId(field.getId() == null ? null : field.getId().toString());

    out.write(',');
    newLine(out);

    writeType(field, out, null);

    tabOut(out);
    out.write('}');
  }

  @Override
  protected Long getReferenceId(Object obj) {
    // TODO
//    if(obj instanceof BaseEntity)
//      return ((BaseEntity)obj).getId();

    return super.getReferenceId(obj);
  }

  @Override
  protected void traceReferences(Object root) {
//    super.traceReferences(root);
  }

  //  @Override
//  protected void addVisitedObject(Object obj, Long id) {
////    super.addVisitedObject(obj, id);
//  }

  @Override
  protected void addReferencedObject(Object obj, Long id) {
//    super.addReferencedObject(obj, id);
  }

//  @Override
  protected boolean isObjectReferenced(Object obj) {
//    return super.isObjectReferenced(obj);
    return true; // always write object's id
  }
}
