package com.github.alanfgates.project.management;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class WorkStream extends TaskOrStream {

  private Set<WorkStream> substreams;
  private Set<Task> tasks;

  WorkStream(WorkStream parent, String name) {
    super(parent, name);
    substreams = new HashSet<>();
    tasks = new HashSet<>();
  }

  void addStream(WorkStream stream) {
    substreams.add(stream);
    lastModified = LocalDate.now();
  }

  void addTask(Task task) {
    tasks.add(task);
    lastModified = LocalDate.now();
  }

  // Only for use by Task, do not call directly.
  void deleteTask(Task task) {
    tasks.remove(task);
  }

  @Override
  void delete() throws StreamNotEmptyException {
    if (substreams.isEmpty() && tasks.isEmpty()) {
      parent.substreams.remove(this);
    }
    throw new StreamNotEmptyException("Stream " + name + " is not empty and cannot be removed.");
  }

  @Override
  Collection<WorkStream> getStreams() {
    return substreams;
  }

  @Override
  Collection<Task> getTasks() {
    return tasks;
  }

  @Override
  Collection<Task> getAllTasks() {
    List<Task> allTasks = new ArrayList<>();
    for (WorkStream stream : substreams) allTasks.addAll(getAllTasks());
    allTasks.addAll(tasks);
    return allTasks;
  }

}
