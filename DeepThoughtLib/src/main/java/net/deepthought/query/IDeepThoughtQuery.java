package net.deepthought.query;

import net.deepthought.data.model.Tag;

import java.util.List;

/**
 * Created by ganymed on 28/11/14.
 */
public interface IDeepThoughtQuery {

  public List<Tag> filterTags(String filterConstraint);

}
