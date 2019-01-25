package com.github.alanfgates.project.management;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class Task extends TaskOrStream {

  private List<Link> links;
  private LocalDate dueBy;
  private Priority priority;

  Task(WorkStream parent, String name) {
    super(parent, name);
    links = new ArrayList<>();
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

  static LocalDate parseDateString(String date) throws IllegalArgumentException {
    // If we are passed a zero length or null date just ignore it
    if (date != null && date.length() > 0) {
      if (date.startsWith("tod")) {
        return LocalDate.now();
      } else if (date.startsWith("tom")) {
        return LocalDate.now().plusDays(1);
      } else  {
        // Assume it's of the form mm/dd
        try {
          LocalDate now = LocalDate.now();
          int month = Integer.valueOf(date.substring(0, 2));
          int day = Integer.valueOf(date.substring(3));
          int year = now.getYear();
          if (month < now.getMonthValue()) year++;
          return LocalDate.of(year, month, day);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Cannot parse " + date + ", please use 'today', 'tomorrow', or mm/dd");
        }
      }
    }
    return null;
  }

  Priority getPriority() {
    return priority;
  }

  static Priority parsePriorityString(String priority) throws IllegalArgumentException {
    if (priority.length() == 0) return null;
    return Priority.valueOf(priority.toUpperCase());
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
