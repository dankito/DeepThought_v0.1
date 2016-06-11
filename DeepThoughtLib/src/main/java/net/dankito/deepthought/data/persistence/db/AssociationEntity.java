package net.dankito.deepthought.data.persistence.db;

import javax.persistence.MappedSuperclass;

/**
 * Just a dummy class to mark Association Entities. AssociationEntities contrary to all other Entities really get deleted from Database instead just setting their deleted flag to true.
 * Created by ganymed on 10/02/15.
 */
@MappedSuperclass
public class AssociationEntity extends BaseEntity {
}
