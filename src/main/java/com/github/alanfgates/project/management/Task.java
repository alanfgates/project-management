package com.github.alanfgates.project.management;

import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class Task extends TaskOrStream {

  final static LocalDate END_OF_THE_WORLD = LocalDate.of(9999, 12, 31);
  private final static SortedLinkedTree<TaskOrStream> EMPTY_TREE = new SortedLinkedTree<>(Comparator.comparing(TaskOrStream::getName));

  private LocalDate dueBy;
  private Priority priority;
  private TaskDisplay display;
  private URL link;

  /**
   * For Jackson
   */
  public Task() {
    priority = Priority.NONE;
    dueBy = END_OF_THE_WORLD;

  }

  Task(WorkStream parent, String name) {
    super(parent, name);
    priority = Priority.NONE;
    dueBy = END_OF_THE_WORLD;
  }

  // For backwards compatibility when JSON reads older stored data
  @Deprecated
  public void setLinks(List<Link> links) {
    if (links.size() > 1) link = links.get(0).getUrl();
  }

  public URL getLink() {
    return link;
  }

  public void setLink(URL link) {
    this.link = link;
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

  static LocalDate parseDateString(String date) throws InvalidInputException {
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
          throw new InvalidInputException("Cannot parse " + date + ", please use 'today', 'tomorrow', or mm/dd");
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
