package net.deepthought.util;

import net.deepthought.data.model.Publisher;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.NoteType;
import net.deepthought.data.model.enums.ReferenceCategory;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
import net.deepthought.data.model.enums.ReferenceSubDivisionCategory;
import net.deepthought.data.model.enums.SeriesTitleCategory;

/**
 * Created by ganymed on 11/02/15.
 */
public class Empty {

  public final static SeriesTitle Series = new SeriesTitle("");

  public final static SeriesTitleCategory SeriesCategory = new SeriesTitleCategory("");

  public final static Reference Reference = new Reference("");

  public final static ReferenceCategory ReferenceCategory = new ReferenceCategory("");

  public final static ReferenceSubDivisionCategory SubDivisionCategory = new ReferenceSubDivisionCategory("");

  public final static ReferenceIndicationUnit ReferenceIndicationUnit = new ReferenceIndicationUnit("");

  public final static Publisher Publisher = new Publisher("");

  public final static Language Language = new Language("");

  public final static NoteType NoteType = new NoteType("");

}
