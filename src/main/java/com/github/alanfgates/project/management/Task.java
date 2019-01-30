package com.github.alanfgates.project.management;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Task extends TaskOrStream {

  private final static LocalDate END_OF_THE_WORLD = LocalDate.of(9999, 12, 31);
  private final static SortedLinkedTree<TaskOrStream> EMPTY_TREE = new SortedLinkedTree<>(Comparator.comparing(TaskOrStream::getName));

  private List<Link> links;
  private LocalDate dueBy;
  private Priority priority;
  private TaskDisplay display;

  /**
   * For Jackson
   */
  public Task() {
    priority = Priority.NONE;
    dueBy = END_OF_THE_WORLD;

  }

  Task(WorkStream parent, String name) {
    super(parent, name);
    links = new ArrayList<>();
    priority = Priority.NONE;
    dueBy = END_OF_THE_WORLD;
  }

  public List<Link> getLinks() {
    return links;
  }

  void addLink(Link link) {
    links.add(link);
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  LocalDate getDueBy() {
    return dueBy;
  }

  void setDueBy(LocalDate dueBy) {
    this.dueBy = dueBy;
  }

  /**
   * For Jackson
   */
  public String getDueDate() {
    if (dueBy == null) return END_OF_THE_WORLD.toString();
    return dueBy.toString();
  }

  /**
   * For Jackson
   */
  public void setDueDate(String text) {
    dueBy = (text == null) ? null : LocalDate.parse(text);
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

  public Priority getPriority() {
    return priority;
  }

  static Priority parsePriorityString(String priority) throws IllegalArgumentException {
    if (priority.length() == 0) return null;
    return Priority.valueOf(priority.toUpperCase());
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  void markDone() {
    delete();
  }

  @Override
  void delete() {
    parent.deleteTask(this);
  }

  @Override
  SortedLinkedTree<TaskOrStream> getChildren() {
    return EMPTY_TREE;
  }

  @Override
  EntryDisplay getDisplay() {
    if (display == null) display = new TaskDisplay(this);
    return display;
  }

  @Override
  void connectChildren() {
    // NOP

  }
}
