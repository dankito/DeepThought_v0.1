package net.dankito.deepthought.android.data.persistence.db.helper;

import com.j256.ormlite.dao.cda.jointable.JoinTableDao;
import com.j256.ormlite.dao.cda.jointable.JoinTableDaoRegistry;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;

import java.lang.reflect.Field;

/**
 * Created by ganymed on 10/11/14.
 */
public class TestJoinTableDaoRegistry extends JoinTableDaoRegistry {

  public JoinTableDao getCachedDaoForRelation(Field owningSideField, Field inverseSideField) {
    for(JoinTableDao dao : createdJoinTableDaos.values()) {
      ManyToManyConfig config = dao.getConfig();
      if(config.getOwningSideField().equals(owningSideField) && config.getInverseSideField().equals(inverseSideField))
        return dao;
    }

    return null;
  }
}
