package net.deepthought.communication.registration;

import net.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 19/08/15.
 */
public interface RegistrationRequestListener {

  void openRegistrationServerFound(HostInfo hostInfo);

}
