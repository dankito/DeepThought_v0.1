package net.deepthought.util.isbn;

import net.deepthought.data.html.IHtmlHelper;
import net.deepthought.util.IThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *   Knows multiple {@link IIsbnResolver} implementations and for each {@link #resolveIsbnAsync(String, IsbnResolvingListener)}
 *   requests queries all of them and returns the best result.
 * </p>
 * Created by ganymed on 06/12/15.
 */
public class MultiImplementationsIsbnResolver implements IIsbnResolver {

  protected IThreadPool threadPool;

  protected IHtmlHelper htmlHelper;

  protected List<IIsbnResolver> isbnResolverImplementations = new ArrayList<>();


  public MultiImplementationsIsbnResolver(IHtmlHelper htmlHelper, IThreadPool threadPool) {
    this.htmlHelper = htmlHelper;
    this.threadPool = threadPool;

    addIsbnResolverImplementations();
  }

  protected void addIsbnResolverImplementations() {
    isbnResolverImplementations.add(new OpenIsbnIsbnResolver(htmlHelper, threadPool));
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

  public void resolveIsbn(String isbn, final IsbnResolvingListener listener) {
    final int countResultsExpected = isbnResolverImplementations.size();
    final List<ResolveIsbnResult> receivedResults = new ArrayList<>();

    for(IIsbnResolver isbnResolver : isbnResolverImplementations) {
      isbnResolver.resolveIsbnAsync(isbn, new IsbnResolvingListener() {
        @Override
        public void isbnResolvingDone(ResolveIsbnResult result) {
          receivedResults.add(result);

          // TODO: what if not all IIsbnResolvers return a result? In this case this method would wait infinite!
          if(countResultsExpected == receivedResults.size()) {
            dispatchBestResult(receivedResults, listener);
          }
        }
      });
    }
  }


  protected void dispatchBestResult(List<ResolveIsbnResult> receivedResults, IsbnResolvingListener listener) {
    if(receivedResults.size() == 1) {
      listener.isbnResolvingDone(receivedResults.get(0));
    }

    ResolveIsbnResult bestResult = findBestResult(receivedResults);
    listener.isbnResolvingDone(bestResult);
  }

  protected ResolveIsbnResult findBestResult(List<ResolveIsbnResult> receivedResults) {
    // TODO: judge which result is best (only needed as soon as multiple implementations are available)
    if(receivedResults.size() > 0) {
      return receivedResults.get(0);
    }

    return null;
  }

}
