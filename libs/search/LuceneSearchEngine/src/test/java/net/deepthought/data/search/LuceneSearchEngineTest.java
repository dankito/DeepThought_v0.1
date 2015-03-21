package net.deepthought.data.search;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by ganymed on 17/03/15.
 */
public class LuceneSearchEngineTest {

  protected LuceneSearchEngine searchEngine;

  @Before
  public void setup() {
    searchEngine = new LuceneSearchEngine();
  }


  @Test
  public void test() throws IOException, ParseException {
    searchEngine.index("Jetzt aber mal ein ganz ein langer Text, dessen Sprache zu identifizieren dir hoffentlich keine Mühe bereitet, liebes Tika");
    searchEngine.index("Entweder man lebt, oder man ist konsequent");
    searchEngine.index("Du hast nichts zu verlieren, aber eine Welt zu gewinnen");
    searchEngine.index("Die Welt hat genug für jedermanns Bedürfnisse, aber nicht für jedermanns Gier");
    searchEngine.index("Shit happens");
    searchEngine.index("In the long run we're all dead");
    searchEngine.index("Voulez vous coucher avec moi? - Va te faire enculer sale fils de pute!");
    searchEngine.index("Preferisco la dolce vita italiana");

    searchEngine.search("lebt");
    searchEngine.search("leben");
    searchEngine.search("run");

    List<IndexTerm> terms = searchEngine.getAllTerms();
  }
}
