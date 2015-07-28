package net.deepthought.controls;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * Created by ganymed on 04/03/15.
 */
public class Constants {

//  public final static int ContextHelpBackgroundColor = -12424802;
  public final static int ContextHelpBackgroundColor = -7820592;

  public final static String ContextHelpIconPath = "icons/context_help_icon_28x30.png";

  public final static String NewspaperIconPath = "icons/news_icon_26x26.png";

  public final static String UpdateIconPath = "icons/update_icon_32x32.png";


  public final static Background FilteredTagsExactMatchBackground = new Background(new BackgroundFill(Color.GREEN.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background FilteredTagsRelevantMatchBackground = new Background(new BackgroundFill(Color.LIGHTGREEN.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background FilteredTagsLastSearchTermExactMatchBackground = new Background(new BackgroundFill(Color.CORNFLOWERBLUE.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background FilteredTagsLastSearchTermRelevantMatchBackground = new Background(new BackgroundFill(Color.DEEPSKYBLUE.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background FilteredTagsDefaultBackground = new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(0), new Insets(0)));


  public final static Color ClipboardContentPopupBackgroundColor = Color.LIGHTSKYBLUE.deriveColor(0, 1.0, 1.0, 0.95);
  protected final static Background ClipboardContentPopupBackground = new Background(new BackgroundFill(ClipboardContentPopupBackgroundColor, new CornerRadii(8), new Insets(0)));

  protected final static Background ClipboardContentPopupOptionMouseOverBackground = new Background(new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(8), new Insets(0)));
}
