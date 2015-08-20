package net.deepthought.communication.model;

/**
 * Created by ganymed on 20/08/15.
 */
public class AllowDeviceToRegisterResult {

  protected boolean allowDeviceToRegister = false;

  protected boolean useServersUserInformation = false;


  public AllowDeviceToRegisterResult(boolean allowDeviceToRegister) {
    this.allowDeviceToRegister = allowDeviceToRegister;
  }

  public AllowDeviceToRegisterResult(boolean allowDeviceToRegister, boolean useServersUserInformation) {
    this(allowDeviceToRegister);
    this.useServersUserInformation = useServersUserInformation;
  }


  public boolean allowsDeviceToRegister() {
    return allowDeviceToRegister;
  }

  public boolean useServersUserInformation() {
    return useServersUserInformation;
  }


  public static AllowDeviceToRegisterResult createDenyRegistrationResult() {
    return new AllowDeviceToRegisterResult(false);
  }

  public static AllowDeviceToRegisterResult createAllowRegistrationResult(boolean useServersUserInformation) {
    return new AllowDeviceToRegisterResult(true, useServersUserInformation);
  }

}
