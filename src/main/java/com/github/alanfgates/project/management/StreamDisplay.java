package com.github.alanfgates.project.management;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class StreamDisplay extends EntryDisplay {

  private final WorkStream stream;

  StreamDisplay(WorkStream stream) {
    super();
    this.stream = stream;
  }

  @Override
  int display(Terminal term, int row, int col) throws IOException {
    List<SGR> mods = new ArrayList<>();
    if (selected) mods.add(SGR.BOLD);
    TextGraphics line = term.newTextGraphics();
    if (opened) {
      line.putString(col, row, "-" + stream.getName(), mods);
      for (TaskOrStream child : stream.getChildren()) {
        row = child.getDisplay().display(term, row + 1, col + 2);
      }
    } else {
      line.putString(col, row, "+" + stream.getName(), mods);
    }
    return row;
  }

  @Override
  void setOpenAll(boolean open) {
    opened = open;
    for (TaskOrStream child : stream.getChildren()) child.getDisplay().setOpenAll(open);
  }

  @Override
  EntryDisplay next() {
    if (opened) {
      Iterator<TaskOrStream> iter = stream.getChildren().iterator();
      if (iter.hasNext()) {
        return iter.next().getDisplay();
        // If not, fall through here
      }
    }
    return stream.getNext() == null ? null : stream.getNext().getDisplay();
  }

  @Override
  protected TaskOrStream getTaskOrStream() {
    return stream;
  }

  @Override
  boolean markDone(TextUI ui) throws IOException {
    help(ui);
    return false;
  }

  @Override
  boolean delete(TextUI ui) throws IOException {
    try {
      stream.delete();
      return true;
    } catch (StreamNotEmptyException e) {
      ui.showError("Failed to delete non-empty stream");
      e.printStackTrace();
      return false;
    }
  }

  @Override
  void details(TextUI ui) throws IOException {
    List<String> lines = commonDetails();
    ui.displayStrings(lines);
  }

  @Override
  void help(TextUI ui) throws IOException {
    ui.displayStrings(Arrays.asList(
        "d: delete (stream must be empty)",
        "e: edit details of this stream",
        "H: close all children",
        "h: close children",
        "j: move to next entry",
        "k: move to previous entry",
        "L: open all children",
        "q: quit",
        "s: add new stream",
        "t: add new task",
        "?: get help",
        "return: see details of this stream"
    ));
  }

  @Override
  void addStream(TextUI ui) throws IOException {
    NameCollector name = new NameCollector();
    DescriptionCollector description = new DescriptionCollector();
    ui.getInput("Add Stream", Arrays.asList(name, description));
    WorkStream newStream = new WorkStream(stream, name.getInput());
    stream.addStream(newStream);
    newStream.setDescription(description.getInput());
  }

  @Override
  void addTask(TextUI ui) throws IOException {
    NameCollector name = new NameCollector();
    DescriptionCollector description = new DescriptionCollector();
    DueByCollector dueBy = new DueByCollector();
    PriorityCollector priority = new PriorityCollector();
    UrlCollector link = new UrlCollector();
    ui.getInput("Add Task", Arrays.asList(name, description, dueBy, priority, link));
    Task task = new Task(stream, name.getInput());
    stream.addTask(task);
    task.setDescription(description.getInput());
    task.setDueBy(dueBy.getInput());
    task.setPriority(priority.getInput());
    task.setLink(link.getInput());
  }

  private class NameCollector extends InputCollector<String> {
    NameCollector() {
      super("Name", input -> {
        if (input == null || input.length() == 0) {
          throw new InvalidInputException("Name cannot be null");
        }
        return input;
      });
    }
  }

  private class DescriptionCollector extends InputCollector<String> {
    DescriptionCollector() {
      super("Description", input -> input);
    }
  }

  private class DueByCollector extends InputCollector<LocalDate> {
    DueByCollector() {
      super("Due by", input -> input == null || input.length() == 0 ? null : Task.parseDateString(input));
    }
  }

  private class PriorityCollector extends InputCollector<Priority> {
    PriorityCollector() {
      super("Priority", input -> {
        try {
          return input == null || input.length() == 0 ? null : Priority.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
          throw new InvalidInputException(input + " is not a known Priority");
        }
      });
    }
  }

  private class UrlCollector extends InputCollector<URL> {
    UrlCollector() {
      super("URL", input -> {
        try {
          return input == null || input.length() == 0 ? null : new URL(input);
        } catch (MalformedURLException e) {
          throw new InvalidInputException(e.getMessage());
        }
      });
    }
  }

  // TODO - three problems, one prompts don't come out in order.  Need to move this to a list and pass a struct that
  // includes the return type that is, change validator to have just one transform call.  Two, we don't retry when
  // the transform fails.  three, we don't seem to be handling nulls.
  //
}
