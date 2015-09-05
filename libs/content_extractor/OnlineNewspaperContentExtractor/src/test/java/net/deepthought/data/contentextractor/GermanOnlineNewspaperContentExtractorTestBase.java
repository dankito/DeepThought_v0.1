package net.deepthought.data.contentextractor;

import net.deepthought.util.Localization;

import org.junit.Before;

import java.util.Locale;

/**
 * Created by ganymed on 13/06/15.
 */
public abstract class GermanOnlineNewspaperContentExtractorTestBase extends OnlineNewspaperContentExtractorTestBase {


  @Before
  public void setup() {
    Localization.setLanguageLocale(Locale.GERMAN); // so that publishing dates get compared correctly
    super.setup();
  }

}
