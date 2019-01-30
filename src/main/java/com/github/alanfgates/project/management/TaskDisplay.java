package com.github.alanfgates.project.management;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class TaskDisplay extends EntryDisplay {

  private final Task task;

  TaskDisplay(Task task) {
    super();
    this.task = task;
  }

  @Override
  int display(Terminal term, int row, int col) throws IOException {
    TextGraphics line = term.newTextGraphics();
    List<SGR> mods = new ArrayList<>();
    if (selected) mods.add(SGR.BOLD);
    if (task.getDueBy() != null && LocalDate.now().compareTo(task.getDueBy()) >= 0) {
      mods.add(SGR.ITALIC);
    }
    line.putString(col, row, task.getName(), mods);
    return row;
  }

  @Override
  void setOpenAll(boolean open) {
    // NOP
  }

  @Override
  EntryDisplay next() {
    TaskOrStream next = task.getNext();
    if (next != null) {
      return next.getDisplay();
    } else {
      next = task.getParent().getNext();
      if (next != null) return next.getDisplay();
      else return null;
    }
  }

  @Override
  protected TaskOrStream getTaskOrStream() {
    return task;
  }

  @Override
  boolean markDone() {
    task.markDone();
    return true;
  }

  @Override
  boolean delete(TextUI ui) {
    task.delete();
    return true;
  }

  @Override
  void details(TextUI ui) throws IOException {
    List<String> lines = commonDetails();
    if (task.getDueBy() != null) lines.add("Due by: " + task.getDueBy().toString());
    if (task.getPriority() != null) lines.add("Priority: " + task.getPriority().name().toLowerCase());
    for (Link link : task.getLinks()) {
      lines.add("Link: " + link.getType().name().toLowerCase().replace('_', ' ') + " " + link.getUrl().toString());
    }
    ui.displayStrings(lines);
  }
}
