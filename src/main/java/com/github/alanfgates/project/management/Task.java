package com.github.alanfgates.project.management;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class Task extends TaskOrStream {

  private List<Link> links;
  private LocalDate dueBy;
  private Priority priority;

  Task(WorkStream parent, String name) {
    super(parent, name);
  }

  List<Link> getLinks() {
    return links;
  }

  void addLink(Link link) {
    links.add(link);
    lastModified = LocalDate.now();
  }

  LocalDate getDueBy() {
    return dueBy;
  }

  void setDueBy(LocalDate dueBy) {
    this.dueBy = dueBy;
    lastModified = LocalDate.now();
  }

  Priority getPriority() {
    return priority;
  }

  void setPriority(Priority priority) {
    this.priority = priority;
    lastModified = LocalDate.now();
  }

  void markDone() {
    delete();
  }

  @Override
  void delete() {
    parent.deleteTask(this);
  }

  @Override
  Collection<WorkStream> getStreams() {
    return Collections.emptyList();
  }

  @Override
  Collection<Task> getTasks() {
    return Collections.singletonList(this);
  }

  @Override
  Collection<Task> getAllTasks() {
    return getTasks();
  }
}
