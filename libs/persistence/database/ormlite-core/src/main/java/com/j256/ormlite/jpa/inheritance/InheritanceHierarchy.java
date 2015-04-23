package com.j256.ormlite.jpa.inheritance;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ganymed on 16/11/14.
 */
public class InheritanceHierarchy {

  protected List<EntityInheritance> inheritanceHierarchy = new LinkedList<>();


  public InheritanceHierarchy() {

  }


  public void addEntityHierarchyAtTop(EntityInheritance inheritance) {
    EntityInheritance currentTopItem = getEntityInheritanceAtTop();
    if(currentTopItem != null) {
      inheritance.setNextEntityInheritance(currentTopItem);
      currentTopItem.setPreviousEntityInheritance(inheritance);
    }

    inheritanceHierarchy.add(0, inheritance);
  }

  public void addEntityHierarchyAtEnd(EntityInheritance inheritance) {
    EntityInheritance currentBottomItem = getEntityInheritanceAtEnd();
    if(currentBottomItem != null) {
      inheritance.setPreviousEntityInheritance(currentBottomItem);
      currentBottomItem.setNextEntityInheritance(inheritance);
    }

    inheritanceHierarchy.add(inheritance);
  }

  public EntityInheritance getEntityInheritanceAtTop() {
    if(inheritanceHierarchy.size() == 0)
      return null;
    return inheritanceHierarchy.get(0);
  }

  public EntityInheritance getEntityInheritanceAtEnd() {
    if(inheritanceHierarchy.size() == 0)
      return null;
    return inheritanceHierarchy.get(inheritanceHierarchy.size() - 1);
  }

  public List<EntityInheritance> getHierarchy() {
    return inheritanceHierarchy;
  }
}
