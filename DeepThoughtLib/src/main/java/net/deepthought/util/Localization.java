package net.deepthought.util;

import net.deepthought.data.model.enums.ApplicationLanguage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by ganymed on 14/12/14.
 */
public class Localization {

  private final static Logger log = LoggerFactory.getLogger(Localization.class);


  public final static String StringsResourceBundleName = "Strings";


  protected static Locale LanguageLocale = Locale.getDefault();

  protected static ResourceBundle StringsResourceBundle = null;


  public static Locale getLanguageLocale() {
    return LanguageLocale;
  }

  public static void setLanguageLocale(Locale languageLocale) {
    LanguageLocale = languageLocale;
    Locale.setDefault(languageLocale);
    StringsResourceBundle = ResourceBundle.getBundle(StringsResourceBundleName, LanguageLocale, new UTF8Control());
  }

  public static void setLanguageLocale(ApplicationLanguage language) {
    try {
      if(language != null && language.getLanguageKey().equals(LanguageLocale.getLanguage()) == false) {
//      setLanguageLocale(Locale.forLanguageTag(language.getLanguageKey())); // crashes on older Androids
        setLanguageLocale(new Locale(language.getLanguageKey()));
      }
    } catch(Exception ex) {
      log.error("Could not find Locale for ApplicationLanguage's LanguageKey " + language.getLanguageKey() + " of ApplicationLanguage " + language.getName(), ex);
    }
  }

  public static ResourceBundle getStringsResourceBundle() {
    return StringsResourceBundle;
  }


  static {
    try {
      StringsResourceBundle = ResourceBundle.getBundle(StringsResourceBundleName, LanguageLocale, new UTF8Control());
    } catch(Exception ex) {
      log.error("Could not load " + StringsResourceBundleName + ". No Strings will now be translated, only their resource keys will be displayed.", ex);
    }
  }


  public static String getLocalizedString(String resourceKey) {
    try {
      return getStringsResourceBundle().getString(resourceKey);
    } catch(Exception ex) {
      log.error("Could not get Resource for key {} from String Resource Bundle {}", resourceKey, StringsResourceBundleName);
    }

    return resourceKey;
  }

  public static String getLocalizedString(String resourceKey, Object... formatArguments) {
    return String.format(getLocalizedString(resourceKey), formatArguments);
  }


  /**
   * <p>
   *  By default .properties files only supports ISO-8859-1 (Latin-1) as encoding.
   *  To be able to load non Latin-1 characters, a custom ResourceBundle.Control has to be written which reads properties file in UTF-8 encoding.
   * </p>
   */
  public static class UTF8Control extends ResourceBundle.Control {
    public ResourceBundle newBundle
        (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
        throws IllegalAccessException, InstantiationException, IOException
    {
      // The below is a copyFile of the default implementation.
      String bundleName = toBundleName(baseName, locale);
      String resourceName = toResourceName(bundleName, "properties");
      ResourceBundle bundle = null;
      InputStream stream = null;
      if (reload) {
        URL url = loader.getResource(resourceName);
        if (url != null) {
          URLConnection connection = url.openConnection();
          if (connection != null) {
            connection.setUseCaches(false);
            stream = connection.getInputStream();
          }
        }
      } else {
        stream = Localization.class.getClassLoader().getResourceAsStream(resourceName);
      }

      if (stream != null) {
        try {
          // Only this line is changed to make it to read properties files as UTF-8.
          bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
        } finally {
          stream.close();
        }
      }
      return bundle;
    }
  }

}
