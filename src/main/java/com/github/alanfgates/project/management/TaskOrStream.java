package com.github.alanfgates.project.management;

import java.time.LocalDate;

public abstract class TaskOrStream implements Comparable<TaskOrStream> {

  protected WorkStream parent;
  protected String name;
  protected String description;
  protected LocalDate createdAt;

  // Levels of methods in this class and its children are chosen carefully to control what Jackson does and
  // doesn't serialize.  parent serialized to avoid circular references.  createdAt (and dueBy in Task) are
  // serialized as strings since LocalDate isn't easily serializable via Jackson.

  protected TaskOrStream() {

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
  TaskOrStream getNext() {
    if (parent == null) return null;
    return parent.getChildren().next(this);
  }

  TaskOrStream getPrev() {
    if (parent == null) return null;
    return parent.getChildren().prev(this);
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
  abstract SortedLinkedTree<TaskOrStream> getChildren();

  abstract void delete() throws StreamNotEmptyException;

  abstract EntryDisplay getDisplay();

  /**
   * After the tree has been read back from YAML we need to wire up all the parent/child relationships
   */
  abstract void connectChildren();

  @Override
  public int compareTo(TaskOrStream o) {
    if (o == null) return 1;
    else return getName().compareTo(o.getName());
  }
}
