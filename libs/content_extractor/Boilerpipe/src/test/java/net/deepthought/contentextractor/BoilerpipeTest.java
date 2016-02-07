package net.deepthought.contentextractor;

import com.kohlschutter.boilerpipe.extractors.CanolaExtractor;
import com.kohlschutter.boilerpipe.extractors.ExtractorBase;
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter;

import org.junit.Test;

import java.net.URL;

/**
 * Created by ganymed on 22/12/15.
 */
public class BoilerpipeTest {

  @Test
  public void testBoilerpipe() throws Exception {
//    ExtractorBase extractor = ArticleExtractor.INSTANCE;
//    ExtractorBase extractor = DefaultExtractor.INSTANCE;
//    ExtractorBase extractor = LargestContentExtractor.INSTANCE;
    ExtractorBase extractor = CanolaExtractor.INSTANCE;

    String tagesschau = extractor.getText(new URL("https://www.tagesschau.de/ausland/bnd-syrien-101.html"));
    String machWasZaehlt = extractor.getText(new URL("http://www.machwaszaehlt.de/"));
    String locus = extractor.getText(new URL("http://www.focus.de/politik/ausland/terror-attacke-in-paris-das-bekennerschreiben-des-is-im-wortlaut_id_5088281" +
        ".html"));

    final HTMLHighlighter htmlHighlighter = HTMLHighlighter.newHighlightingInstance();
     final HTMLHighlighter htmlExtractor = HTMLHighlighter.newExtractingInstance();

    String tagesschau2 = htmlHighlighter.process(new URL("https://www.tagesschau.de/ausland/bnd-syrien-101.html"), extractor);
    String machWasZaehlt2 = htmlHighlighter.process(new URL("http://www.machwaszaehlt.de/"), extractor);
    String locus2 = htmlHighlighter.process(new URL("http://www.focus.de/politik/ausland/terror-attacke-in-paris-das-bekennerschreiben-des-is-im-wortlaut_id_5088281" +
        ".html"), extractor);

    String tagesschau3 = htmlExtractor.process(new URL("https://www.tagesschau.de/ausland/bnd-syrien-101.html"), extractor);
    String machWasZaehlt3 = htmlExtractor.process(new URL("http://www.machwaszaehlt.de/"), extractor);
    String locus3 = htmlExtractor.process(new URL("http://www.focus.de/politik/ausland/terror-attacke-in-paris-das-bekennerschreiben-des-is-im-wortlaut_id_5088281" +
        ".html"), extractor);

    if(locus == tagesschau) {

    }
  }
}
