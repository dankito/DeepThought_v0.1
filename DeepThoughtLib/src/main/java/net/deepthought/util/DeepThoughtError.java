package net.deepthought.util;

/**
 * Created by ganymed on 01/01/15.
 */
public class DeepThoughtError extends Notification {

  public final static DeepThoughtError Success = DeepThoughtError.errorFromLocalizationKey("success");


  protected Exception exception;

  protected boolean isSevere = false;


  public DeepThoughtError(String notificationMessage) {
    super(NotificationType.Error, notificationMessage);
  }

  public DeepThoughtError(String notificationMessage, Exception exception) {
    this(notificationMessage);
    this.exception = exception;
  }

  public DeepThoughtError(String notificationMessage, Exception exception, boolean isSevere) {
    this(notificationMessage, exception);
    this.isSevere = isSevere;
  }

  public DeepThoughtError(String notificationMessage, Exception exception, boolean isSevere, String notificationMessageTitle) {
    this(notificationMessage, exception, isSevere);
    this.notificationMessageTitle = notificationMessageTitle;
  }


  public Exception getException() {
    return exception;
  }

  public boolean isSevere() {
    return isSevere;
  }



  public static DeepThoughtError errorFromLocalizationKey(String localizationKey, Object... formatArguments) {
    return new DeepThoughtError(Localization.getLocalizedStringForResourceKey(localizationKey, formatArguments));
  }

  public static DeepThoughtError errorFromLocalizationKey(Exception exception, String localizationKey, Object... formatArguments) {
    return new DeepThoughtError(Localization.getLocalizedStringForResourceKey(localizationKey, formatArguments), exception);
  }

}
