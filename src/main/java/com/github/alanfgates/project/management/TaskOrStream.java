package com.github.alanfgates.project.management;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class TaskOrStream implements Serializable {

  protected WorkStream parent;
  protected String name;
  protected String description;
  protected LocalDate createdAt;

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
