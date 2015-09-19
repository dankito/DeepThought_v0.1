package net.deepthought.util;

/**
 * Created by ganymed on 17/07/15.
 */
public class Notification {

  protected NotificationType type;

  protected String notificationMessage = "";

  protected String notificationMessageTitle = null;

  protected Object parameter = null;


  public Notification(NotificationType type) {
    this.type = type;
  }

  public Notification(NotificationType type, String notificationMessage) {
    this(type);
    this.notificationMessage = notificationMessage;
  }

  public Notification(NotificationType type, String notificationMessage, String notificationMessageTitle) {
    this(type, notificationMessage);
    this.notificationMessageTitle = notificationMessageTitle;
  }

  public Notification(NotificationType type, String notificationMessage, Object parameter) {
    this(type, notificationMessage);
    this.parameter = parameter;
  }

  public Notification(NotificationType type, String notificationMessage, String notificationMessageTitle, Object parameter) {
    this(type, notificationMessage, notificationMessageTitle);
    this.parameter = parameter;
  }

  public Notification(NotificationType type, Object parameter) {
    this(type);
    this.parameter = parameter;
  }


  public NotificationType getType() {
    return type;
  }

  public String getNotificationMessage() {
    return notificationMessage;
  }

  public boolean hasNotificationMessageTitle() {
    return notificationMessageTitle != null;
  }

  public String getNotificationMessageTitle() {
    return notificationMessageTitle;
  }

  public boolean hasParameter() {
    return parameter != null;
  }

  public Object getParameter() {
    return parameter;
  }


  @Override
  public String toString() {
    return notificationMessage;
  }

}
