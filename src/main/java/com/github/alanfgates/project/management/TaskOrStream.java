package com.github.alanfgates.project.management;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.SortedSet;

public abstract class TaskOrStream implements Serializable {

  protected WorkStream parent;
  protected String name;
  protected String description;
  protected LocalDate createdAt;
  protected TaskOrStream nextSibling; // points to the next TaskOrStream of my parent
  protected TaskOrStream prevSibling; // points to the previous TaskOrStream of my parent

  // Levels of methods in this class and its children are chosen carefully to control what Jackson does and
  // doesn't serialize.  parent serialized to avoid circular references.  createdAt (and dueBy in Task) are
  // serialized as strings since LocalDate isn't easily serializable via Jackson.

  /**
   * For jackson
   */
  public  TaskOrStream() {

  }

  protected TaskOrStream(WorkStream parent, String name) {
    this.parent = parent;
    this.name = name;
    createdAt = LocalDate.now();
  }

  WorkStream getParent() {
    return parent;
  }

  void setParent(WorkStream parent) {
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  LocalDate getCreatedAt() {
    return createdAt;
  }

  /**
   * Next entry in the tree of the same level.
   * @return next entry or null if there is no next entry.
   */
  TaskOrStream getNextSibling() {
    return nextSibling;
  }

  TaskOrStream getPrevSibling() {
    return prevSibling;
  }

  /**
   * For Jackson
   */
  public String getCreationTime() {
    return createdAt.toString();
  }

  /**
   * For Jackson
   */
  public void setCreationTime(String text) {
    createdAt = LocalDate.parse(text);
  }

  /**
   * Get all the children of this node, sorted by name
   * @return all children of hte node
   */
  abstract SortedSet<TaskOrStream> getAllChildren();

  String buildName() {
    return (parent == null) ? name : parent.buildName() + "." + name;
  }

  /**
   * Walk through the all children and set the next and prev pointers properly.
   */
  abstract void fixSiblings();

  abstract void delete() throws StreamNotEmptyException;

  abstract EntryDisplay getDisplay();

}
