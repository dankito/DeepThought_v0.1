package net.dankito.deepthought.util.isbn;

import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.model.Reference;

import org.jsoup.nodes.Document;

/**
 * Created by ganymed on 05/12/15.
 */
public abstract class IsbnResolverBase implements IIsbnResolver {

  protected IHtmlHelper htmlHelper;

  protected IThreadPool threadPool;


  public IsbnResolverBase(IHtmlHelper htmlHelper, IThreadPool threadPool) {
    this.htmlHelper = htmlHelper;
    this.threadPool = threadPool;
  }


  @Override
  public void resolveIsbnAsync(final String isbn, final IsbnResolvingListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        resolveIsbn(isbn, listener);
      }
    });
  }

  public void resolveIsbn(String isbn, IsbnResolvingListener listener) {
    try {
      Document receivedResponse = htmlHelper.retrieveOnlineDocument(getQueryIsbnUrl(isbn));
      Reference reference = parseResponseToReference(receivedResponse);
      reference.setIsbnOrIssn(isbn);

      listener.isbnResolvingDone(new ResolveIsbnResult(reference));
    } catch(Exception ex) {
      listener.isbnResolvingDone(new ResolveIsbnResult(ex));
    }
  }

  protected abstract String getQueryIsbnUrl(String isbn);

  protected abstract Reference parseResponseToReference(Document receivedResponse);

}
