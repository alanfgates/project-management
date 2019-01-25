package com.github.alanfgates.project.management;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class TaskOrStream implements Serializable {

  protected final WorkStream parent;
  protected String name;
  protected String description;
  protected LocalDate createdAt;
  protected LocalDate lastModified;

  protected TaskOrStream(WorkStream parent, String name) {
    this.parent = parent;
    this.name = name;
    createdAt = LocalDate.now();
    lastModified = createdAt;
  }

  WorkStream getParent() {
    return parent;
  }

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
    lastModified = LocalDate.now();
  }

  String getDescription() {
    return description;
  }

  void setDescription(String description) {
    this.description = description;
    lastModified = LocalDate.now();
  }

  LocalDate getCreatedAt() {
    return createdAt;
  }

  LocalDate getLastModified() {
    return lastModified;
  }

  Collection<TaskOrStream> getAllChildren() {
    List<TaskOrStream> all = new ArrayList<>();
    all.addAll(getStreams());
    all.addAll(getTasks());
    return all;
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

}
