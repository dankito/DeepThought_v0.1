package net.deepthought.util.isbn;

import net.deepthought.data.html.JsoupHtmlHelper;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.util.ObjectHolder;
import net.deepthought.util.ThreadPool;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 06/12/15.
 */
public class OpenIsbnIsbnResolverTest {

  protected OpenIsbnIsbnResolver resolver;

  protected ObjectHolder<ResolveIsbnResult> resultHolder;
  protected CountDownLatch waitLatch;

  protected DateFormat dateFormat;


  @Before
  public void setup() {
    resolver = new OpenIsbnIsbnResolver(new JsoupHtmlHelper(), new ThreadPool());

    resultHolder = new ObjectHolder<>();
    waitLatch = new CountDownLatch(1);

    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  }


  @Test
  public void resolveOnTheRoadToMandalay() throws ParseException {
    resolver.resolveIsbn("9789748299259", new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        resultHolder.set(result);
        waitLatch.countDown();
      }
    });

    waitForResult();

    assertResolvingWasSuccessful(resultHolder);

    Reference reference = resultHolder.get().getResolvedReference();
    Assert.assertEquals("On The Road To Mandalay (Asian Portraits)", reference.getTitle());
    Assert.assertEquals("284", reference.getLength());
    Assert.assertEquals("9789748299259", reference.getIsbnOrIssn());
    Assert.assertEquals(dateFormat.parse("2006-08-02"), reference.getPublishingDate());

    Assert.assertEquals(1, reference.getPersons().size());
    Person author = new ArrayList<Person>(reference.getPersons()).get(0);
    Assert.assertEquals("Mya Than", author.getFirstName());
    Assert.assertEquals("Tint", author.getLastName());

    Assert.assertNotNull(reference.getPreviewImage());
    Assert.assertEquals("http://ecx.images-amazon.com/images/I/5164XEW04VL._SX220_.jpg", reference.getPreviewImage().getUriString());
    Assert.assertEquals(reference.getTitle(), reference.getPreviewImage().getName());
  }


  @Test
  public void resolveLatinGrammarBook() throws ParseException {
    resolver.resolveIsbn("9783140103305", new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        resultHolder.set(result);
        waitLatch.countDown();
      }
    });

    waitForResult();

    assertResolvingWasSuccessful(resultHolder);

    Reference reference = resultHolder.get().getResolvedReference();
    Assert.assertEquals("Orbis Romanus. Lateinische Elementargrammatik", reference.getTitle());
    Assert.assertEquals("125", reference.getLength());
    Assert.assertEquals("9783140103305", reference.getIsbnOrIssn());
    Assert.assertEquals(dateFormat.parse("1975-01-01"), reference.getPublishingDate());

    Assert.assertEquals(1, reference.getPersons().size());
    Person author = new ArrayList<Person>(reference.getPersons()).get(0);
    Assert.assertEquals("Heinrich", author.getFirstName());
    Assert.assertEquals("Schmeken", author.getLastName());

    Assert.assertNotNull(reference.getPreviewImage());
    Assert.assertEquals("http://ecx.images-amazon.com/images/I/51jiByrc3uL._SX220_.jpg", reference.getPreviewImage().getUriString());
    Assert.assertEquals(reference.getTitle(), reference.getPreviewImage().getName());
  }


  @Test
  public void resolveHeadFirstDesignPatterns() throws ParseException {
    resolver.resolveIsbn("9780596007126", new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        resultHolder.set(result);
        waitLatch.countDown();
      }
    });

    waitForResult();

    assertResolvingWasSuccessful(resultHolder);

    Reference reference = resultHolder.get().getResolvedReference();
    Assert.assertEquals("Head First Design Patterns", reference.getTitle());
    Assert.assertEquals("688", reference.getLength());
    Assert.assertEquals("9780596007126", reference.getIsbnOrIssn());
    Assert.assertEquals(dateFormat.parse("2004-11-01"), reference.getPublishingDate());

    Assert.assertEquals(4, reference.getPersons().size());

    Assert.assertNotNull(reference.getPreviewImage());
    Assert.assertEquals("http://ecx.images-amazon.com/images/I/515dhQ3T0PL._SX220_.jpg", reference.getPreviewImage().getUriString());
    Assert.assertEquals(reference.getTitle(), reference.getPreviewImage().getName());
  }


  @Test
  public void resolveVaadin() throws ParseException {
    resolver.resolveIsbn("9783864902062", new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        resultHolder.set(result);
        waitLatch.countDown();
      }
    });

    waitForResult();

    assertResolvingWasSuccessful(resultHolder);

    Reference reference = resultHolder.get().getResolvedReference();
    Assert.assertEquals("Vaadin: Der Kompakte Einstieg Für Java-Entwickler (Mit Einem Geleitwort Von Ville Ingmann, Vaadin Advocate)", reference.getTitle());
    Assert.assertEquals("280", reference.getLength());
    Assert.assertEquals("9783864902062", reference.getIsbnOrIssn());
    Assert.assertEquals(dateFormat.parse("2014-11-27"), reference.getPublishingDate());

    // OpenIsbn has parsed this badly: this book has 5 authors, OpenIsbn only shows 3, two of them concatenated and then cut off
    Assert.assertEquals(3, reference.getPersons().size());

    Assert.assertNotNull(reference.getPreviewImage());
    Assert.assertEquals("http://ecx.images-amazon.com/images/I/41kjYkNTzGL._SX220_.jpg", reference.getPreviewImage().getUriString());
    Assert.assertEquals(reference.getTitle(), reference.getPreviewImage().getName());
  }


  protected void assertResolvingWasSuccessful(ObjectHolder<ResolveIsbnResult> resultHolder) {
    ResolveIsbnResult result = resultHolder.get();

    Assert.assertNotNull(result);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertNotNull(result.getResolvedReference());
    Assert.assertNull(result.getError());
  }


  protected void waitForResult() {
    waitForResult(3000);
  }

  protected void waitForResult(int milliseconds) {
    try { waitLatch.await(milliseconds, TimeUnit.MILLISECONDS); } catch(Exception ex) { }
  }
}
