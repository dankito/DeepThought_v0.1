package net.dankito.deepthought.controls;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * Created by ganymed on 04/03/15.
 */
public class Constants {

  public final static int ContextHelpBackgroundColor = -7820592;

  public final static Color AddEntityButtonTextColor = Color.rgb(6, 134, 6);

  public final static Color RemoveEntityButtonTextColor = Color.rgb(208, 16, 16);


  public final static String ContextHelpIconPath = "icons/context_help_icon_28x30.png";

  public final static String FilterIconPath = "icons/filter_22x22.png";
  public final static String FilterDeleteIconPath = "icons/filter_delete_20x20.png";

  public final static String SearchIconPath = "icons/search_icon_18x18.png";

  public final static String NewspaperIconPath = "icons/news_icon_26x26.png";

  public final static String UpdateIconPath = "icons/update_icon_32x32.png";

  public final static String WindowIconPath = "icons/window_icon_16x16.png";

  public final static String CameraIconPath = "icons/camera_import_icon.png";
  public final static String ImportFilesIconPath = "icons/photos_import_icon.png";
  public final static String OcrOnLocalImageIconPath = "icons/local_photos_ocr_icon.png";
  public final static String CaptureImageAndDoOcrIconPath = "icons/camera_ocr_icon.png";
  public final static String OcrOnRemoteImageIconPath = "icons/remote_photos_ocr_icon.png";

  public final static String LockClosedIconPath = "icons/lock_closed_icon_24x24.png";

  public final static String LockOpenedIconPath = "icons/lock_opened_icon_24x24.png";


  public final static String AndroidIconPath = "icons/os/android_icon.png";
  public final static String AndroidLogoPath = "icons/os/android_logo.png";

  public final static String LinuxIconPath = "icons/os/linux_icon.png";
  public final static String LinuxLogoPath = "icons/os/linux_logo.png";

  public final static String WindowsIconPath = "icons/os/windows_icon.png";
  public final static String WindowsLogoPath = "icons/os/windows_logo.png";

  public final static String AppleIconPath = "icons/os/apple_icon.png";
  public final static String AppleLogoPath = "icons/os/apple_logo.png";

  public final static String SolarisIconPath = "icons/os/sun-solaris_icon.png";
  public final static String SolarisLogoPath = "icons/os/sun-solaris_logo.png";


  public final static Background TagCellExactMatchBackground = new Background(new BackgroundFill(Color.GREEN.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background TagCellRelevantMatchBackground = new Background(new BackgroundFill(Color.LIGHTGREEN.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background TagCellLastSearchTermExactMatchBackground = new Background(new BackgroundFill(Color.CORNFLOWERBLUE.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background TagCellLastSearchTermSingleMatchBackground = new Background(new BackgroundFill(Color.LIGHTSKYBLUE.deriveColor(0, 1.0, 1.0, 0.5), new CornerRadii(0), new Insets(0)));

  public final static Background TagCellSelectedBackground = new Background(new BackgroundFill(Color.SKYBLUE, new CornerRadii(0), new Insets(0)));

  public final static Background TagCellDefaultBackground = new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(0), new Insets(0)));


  public final static Color ClipboardContentPopupBackgroundColor = Color.LIGHTSKYBLUE.deriveColor(0, 1.0, 1.0, 0.95);
  public final static Background ClipboardContentPopupBackground = new Background(new BackgroundFill(ClipboardContentPopupBackgroundColor, new CornerRadii(8), new Insets(0)));

  public final static Background ClipboardContentPopupOptionMouseOverBackground = new Background(new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(8), new Insets(0)));
}
