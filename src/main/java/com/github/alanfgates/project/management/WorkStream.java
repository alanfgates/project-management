package com.github.alanfgates.project.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class WorkStream extends TaskOrStream {

  private Set<WorkStream> substreams;
  private Set<Task> tasks;
  private StreamDisplay display;
  private SortedSet<TaskOrStream> allChildren;

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
    getAllChildren(); // make sure we've put the existing streams in there, we might not have after reading yaml
    substreams.add(stream);
    allChildren.add(stream);
    fixSiblings();
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
    getAllChildren(); // make sure we've put the existing tasks in there, we might not have after reading yaml
    tasks.add(task);
    allChildren.add(task);
    fixSiblings();
  }

  // Only for use by Task, do not call directly.
  void deleteTask(Task task) {
    tasks.remove(task);
    allChildren.remove(task);
    fixSiblings();
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

  @Override
  SortedSet<TaskOrStream> getAllChildren() {
    if (allChildren == null) {
      allChildren = new TreeSet<>(Comparator.comparing(TaskOrStream::getName));
      allChildren.addAll(getStreams());
      allChildren.addAll(getTasks());
    }
    return allChildren;
  }

  @Override
  void fixSiblings() {
    TaskOrStream prev = null;
    for (TaskOrStream child : getAllChildren()) {
      child.nextSibling = null; // necessary to make sure last entry has a null
      child.prevSibling = prev;
      if (prev != null) prev.nextSibling = child;
      prev = child;
    }
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
    if (display == null) display = new StreamDisplay(this);
    return display;
  }
}
