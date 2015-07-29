package net.deepthought.util;

import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

/**
 * Created by ganymed on 22/12/14.
 */
public class JavaFxLocalization {

  private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.getDefault());

  public static ObjectProperty<Locale> localeProperty() {
    return locale ;
  }

  public static Locale getLocale() {
    return locale.get();
  }

  public static void setLocale(Locale locale) {
    localeProperty().set(locale);
    Localization.setLanguageLocale(locale);
  }


  public static void bindLabeledText(Labeled labeled, final String key, final Object... formatArguments) {
    labeled.textProperty().bind(Bindings.createStringBinding(
        () -> Localization.getLocalizedStringForResourceKey(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindMenuItemText(MenuItem menuItem, final String key, final Object... formatArguments) {
    menuItem.textProperty().bind(Bindings.createStringBinding(
        () -> Localization.getLocalizedStringForResourceKey(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindControlToolTip(Control control, String key, Object... formatArguments) {
    control.setTooltip(createBoundTooltip(key, formatArguments));
  }

  // usage: myButton.setTooltip(createBoundTooltip("mybutton"));
  public static Tooltip createBoundTooltip(final String key, final Object[] formatArguments) {
    Tooltip tooltip = new Tooltip();
    tooltip.textProperty().bind(Bindings.createStringBinding(
        () -> Localization.getLocalizedStringForResourceKey(key, formatArguments), JavaFxLocalization.localeProperty()));
    return tooltip ;
  }

  public static void bindTextInputControlPromptText(TextInputControl control, final String key, final Object... formatArguments) {
    control.promptTextProperty().bind(Bindings.createStringBinding(
        () -> Localization.getLocalizedStringForResourceKey(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindStageTitle(Stage stage, final String key, final Object... formatArguments) {
    stage.titleProperty().bind(Bindings.createStringBinding(
        () -> Localization.getLocalizedStringForResourceKey(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

}
