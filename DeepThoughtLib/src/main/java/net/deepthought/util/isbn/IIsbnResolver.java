package net.deepthought.util.isbn;

/**
 * Created by ganymed on 05/12/15.
 */
public interface IIsbnResolver {

  void resolveIsbnAsync(String isbn, IsbnResolvingListener listener);

}
