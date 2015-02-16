package net.deepthought.util;

/**
 * Created by ganymed on 01/01/15.
 */
public class DeepThoughtError {

  public final static DeepThoughtError Success = DeepThoughtError.errorFromLocalizationKey("success");


  protected String errorMessage;

  protected Exception exception;

  protected boolean isSevere = false;

  protected String errorMessageTitle;


  public DeepThoughtError(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public DeepThoughtError(String errorMessage, Exception exception) {
    this(errorMessage);
    this.exception = exception;
  }

  public DeepThoughtError(String errorMessage, Exception exception, boolean isSevere) {
    this(errorMessage, exception);
    this.isSevere = isSevere;
  }

  public DeepThoughtError(String errorMessage, Exception exception, boolean isSevere, String errorMessageTitle) {
    this(errorMessage, exception, isSevere);
    this.errorMessageTitle = errorMessageTitle;
  }


  public String getErrorMessage() {
    return errorMessage;
  }

  public Exception getException() {
    return exception;
  }

  public boolean isSevere() {
    return isSevere;
  }

  public String getErrorMessageTitle() {
    return errorMessageTitle;
  }


  @Override
  public String toString() {
    return errorMessage;
  }


  public static DeepThoughtError errorFromLocalizationKey(String localizationKey, Object... formatArguments) {
    return new DeepThoughtError(Localization.getLocalizedStringForResourceKey(localizationKey, formatArguments));
  }

  public static DeepThoughtError errorFromLocalizationKey(Exception exception, String localizationKey, Object... formatArguments) {
    return new DeepThoughtError(Localization.getLocalizedStringForResourceKey(localizationKey, formatArguments), exception);
  }

}
