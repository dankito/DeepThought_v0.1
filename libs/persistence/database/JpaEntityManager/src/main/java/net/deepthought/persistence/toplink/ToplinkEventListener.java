//package net.deepthought.persistence.toplink;
//
//import net.deepthought.Application;
//import net.deepthought.data.persistence.db.BaseEntity;
//
//import org.eclipse.persistence.sessions.SessionEvent;
//import org.eclipse.persistence.sessions.SessionEventAdapter;
//
////import oracle.toplink.essentials.sessions.SessionEvent;
////import oracle.toplink.essentials.sessions.SessionEventAdapter;
//
///**
//* Created by ganymed on 02/01/15.
//*/
//public class ToplinkEventListener extends SessionEventAdapter {
//
//  @Override
//  public void postLogin(SessionEvent event) {
//    super.postLogin(event);
//    Application.getDeepThought().lazyLoadedEntityMapped((BaseEntity)event.getResult());
//  }
//
//  @Override
//  public void postAcquireConnection(SessionEvent event) {
//    super.postAcquireConnection(event);
//  }
//}
