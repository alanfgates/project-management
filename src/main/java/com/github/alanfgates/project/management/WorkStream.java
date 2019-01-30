package com.github.alanfgates.project.management;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorkStream extends TaskOrStream {

  private final SortedLinkedTree<TaskOrStream> children;
  private StreamDisplay display;

  /**
   * For Jackson
   */
  public WorkStream() {
    children = new SortedLinkedTree<>(Comparator.comparing(TaskOrStream::getName));
  }

  WorkStream(WorkStream parent, String name) {
    super(parent, name);
    children = new SortedLinkedTree<>(Comparator.comparing(TaskOrStream::getName));
  }

  void addStream(WorkStream stream) {
    children.add(stream);
  }

  // Next for methods are for Jackson
  public List<WorkStream> getSubstreams() {
    List<WorkStream> substreams = new ArrayList<>();
    for (TaskOrStream child : children) if (child instanceof WorkStream) substreams.add((WorkStream)child);
    return substreams;
  }

  public void setSubstreams(List<WorkStream> substreams) {
    children.addAll(substreams);
  }

  public void setTasks(List<Task> tasks) {
    children.addAll(tasks);
  }

  public List<Task> getTasks() {
    List<Task> tasks = new ArrayList<>();
    for (TaskOrStream child : children) if (child instanceof Task) tasks.add((Task)child);
    return tasks;

  }

  void addTask(Task task) {
    children.add(task);
  }

  // Only for use by Task, do not call directly.
  void deleteTask(Task task) {
    children.remove(task);
  }

  @Override
  void delete() throws StreamNotEmptyException {
    if (children.isEmpty()) {
      parent.children.remove(this);
    } else {
      throw new StreamNotEmptyException("Stream " + name + " is not empty and cannot be removed.");
    }
  }

  @Override
  SortedLinkedTree<TaskOrStream> getChildren() {
    return children;
  }

  /**
   * After being read from Yaml the children aren't connected back to the parents.  Calling this will fix that.
   */
  @Override
  void connectChildren() {
    for (TaskOrStream child : children) {
      child.setParent(this);
      child.connectChildren();
    }
  }

  @Override
  EntryDisplay getDisplay() {
    if (display == null) display = new StreamDisplay(this);
    return display;
  }
}
