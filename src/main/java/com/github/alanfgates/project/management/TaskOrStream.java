package com.github.alanfgates.project.management;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class TaskOrStream implements Serializable {

  protected WorkStream parent;
  protected String name;
  protected String description;
  protected LocalDate createdAt;

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
  Collection<TaskOrStream> getAllChildren() {
    SortedSet<TaskOrStream> all = new TreeSet<>(Comparator.comparing(TaskOrStream::getName));
    all.addAll(getStreams());
    all.addAll(getTasks());
    return all;
  }

  String buildName() {
    return (parent == null) ? name : parent.buildName() + "." + name;
  }

  abstract Collection<WorkStream> getStreams();

  /**
   * Get the tasks directly connected to this node.
   * @return tasks
   */
  abstract Collection<Task> getTasks();

  /**
   * Get all tasks under this node.
   * @return all tasks
   */
  abstract Collection<Task> getAllTasks();

  abstract void delete() throws StreamNotEmptyException;

  abstract EntryDisplay getDisplay();

}
