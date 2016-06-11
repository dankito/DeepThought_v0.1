package net.dankito.deepthought.data.exchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by ganymed on 14/07/15.
 */
public class FirefoxPluginCommunicator extends NanoHTTPD {

  public FirefoxPluginCommunicator() throws IOException {
    super(27388);
    start();
  }

  @Override
  public Response serve(IHTTPSession session) {
    Map<String, List<String>> decodedQueryParameters =
        decodeParameters(session.getQueryParameterString());

    Method method = session.getMethod();
    String postBody = session.getQueryParameterString();
    // or you can access the POST request's parameters
    String postParameter = session.getParms().get("parameter");

    final HashMap<String, String> map = new HashMap<String, String>();
    try { session.parseBody(map); } catch(Exception ex) {
      String error = ex.getMessage();
    }

    if(map.containsKey("postData")) { // a Post body has been send along

    }

    StringBuilder sb = new StringBuilder();
    sb.append("Access-Control-Allow-Origin: https://developer.mozilla.org");
    sb.append("<html>");
    sb.append("<head><title>Debug Server</title></head>");
    sb.append("<body>");
    sb.append("<h1>Debug Server</h1>");

    sb.append("<p><blockquote><b>URI</b> = ").append(
        String.valueOf(session.getUri())).append("<br />");

    sb.append("<b>Method</b> = ").append(
        String.valueOf(session.getMethod())).append("</blockquote></p>");

    sb.append("<h3>Headers</h3><p><blockquote>").
        append(toString(session.getHeaders())).append("</blockquote></p>");

    sb.append("<h3>Parms</h3><p><blockquote>").
        append(toString(session.getParms())).append("</blockquote></p>");

    sb.append("<h3>Parms (multi values?)</h3><p><blockquote>").
        append(toString(decodedQueryParameters)).append("</blockquote></p>");

//    try {
//      Map<String, String> files = new HashMap<String, String>();
//      session.parseBody(files);
//      sb.append("<h3>Files</h3><p><blockquote>").
//          append(toString(files)).append("</blockquote></p>");
//
//      BufferedInputStream is = new BufferedInputStream(session.getInputStream());
//      final char[] buffer = new char[2048];
//      final StringBuilder out = new StringBuilder();
//      try (Reader in = new InputStreamReader(is, "UTF-8")) {
//        for (;;) {
//          int rsz = in.read(buffer, 0, buffer.length);
//          if (rsz < 0)
//            break;
//          out.append(buffer, 0, rsz);
//        }
//
//        String message = out.toString();
//        if (message.length() > 0) {
//
//        }
//      }
//      catch (UnsupportedEncodingException ex) {
//    /* ... */
//      }
//      catch (IOException ex) {
//      /* ... */
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }

    sb.append("</body>");
    sb.append("</html>");

    return newFixedLengthResponse(sb.toString());
  }

  private String toString(Map<String, ? extends Object> map) {
    if (map.size() == 0) {
      return "";
    }
    return unsortedList(map);
  }

  private String unsortedList(Map<String, ? extends Object> map) {
    StringBuilder sb = new StringBuilder();
    sb.append("<ul>");
    for (Map.Entry entry : map.entrySet()) {
      listItem(sb, entry);
    }
    sb.append("</ul>");
    return sb.toString();
  }

  private void listItem(StringBuilder sb, Map.Entry entry) {
    sb.append("<li><code><b>").append(entry.getKey()).
        append("</b> = ").append(entry.getValue()).append("</code></li>");
  }

}
