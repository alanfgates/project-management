package com.github.alanfgates.project.management;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkStream extends TaskOrStream {

  private Set<WorkStream> substreams;
  private Set<Task> tasks;

  /**
   * For Jackson
   */
  public WorkStream() {

  }

  WorkStream(WorkStream parent, String name) {
    super(parent, name);
    substreams = new HashSet<>();
    tasks = new HashSet<>();
  }

  void addStream(WorkStream stream) {
    substreams.add(stream);
  }

  public Set<WorkStream> getSubstreams() {
    return substreams;
  }

  public void setSubstreams(Set<WorkStream> substreams) {
    this.substreams = substreams;
  }

  public void setTasks(Set<Task> tasks) {
    this.tasks = tasks;
  }

  void addTask(Task task) {
    tasks.add(task);
  }

  // Only for use by Task, do not call directly.
  void deleteTask(Task task) {
    tasks.remove(task);
  }

  @Override
  void delete() throws StreamNotEmptyException {
    if (substreams.isEmpty() && tasks.isEmpty()) {
      parent.substreams.remove(this);
    } else {
      throw new StreamNotEmptyException("Stream " + name + " is not empty and cannot be removed.");
    }
  }

  @Override
  Collection<WorkStream> getStreams() {
    return substreams;
  }

  @Override
  public Collection<Task> getTasks() {
    return tasks;
  }

  @Override
  Collection<Task> getAllTasks() {
    List<Task> allTasks = new ArrayList<>();
    for (WorkStream stream : substreams) allTasks.addAll(stream.getAllTasks());
    allTasks.addAll(tasks);
    return allTasks;
  }

  /**
   * After being read from Yaml the children aren't connected back to the parents.  Calling this will fix that.
   */
  void connectChildren() {
    for (WorkStream stream : substreams) {
      stream.setParent(this);
      stream.connectChildren();
    }
    for (Task task : tasks) task.setParent(this);
  }

  @Override
  EntryDisplay getDisplay() {
    return new StreamDisplay(this);
  }
}
