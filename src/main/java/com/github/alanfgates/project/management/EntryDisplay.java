package com.github.alanfgates.project.management;

import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class EntryDisplay {
  protected boolean selected;
  protected boolean opened; // whether all substream/tasks are displayed

  protected EntryDisplay() {
  }

  abstract int display(Terminal terminal, int row, int col) throws IOException;

  abstract void setOpenAll(boolean open);

  abstract EntryDisplay next();

  abstract boolean markDone();

  abstract boolean delete(TextUI ui) throws IOException;

  abstract void details(TextUI ui) throws IOException;

  final protected List<String> commonDetails() {
    List<String> lines = new ArrayList<>();
    lines.add("Name: " + getTaskOrStream().getName());
    lines.add("Description: " + getTaskOrStream().getDescription());
    lines.add("Created at: " + getTaskOrStream().getCreationTime());
    return lines;
  }


  protected abstract TaskOrStream getTaskOrStream();

  final EntryDisplay prev() {
    TaskOrStream entry = getTaskOrStream();
    if (entry.getPrev() == null) {
      // This means we don't have an older sibling, so go to our parent
      TaskOrStream parent = entry.getParent();
      if (parent == null) return null;
      else return parent.getDisplay();
    } else if (entry.getPrev().getDisplay().opened) {
      return entry.getPrev().getChildren().last().getDisplay();
    } else {
      return entry.getPrev().getDisplay();
    }
  }

  void setSelected(boolean selected) {
    this.selected = selected;
  }

  void setOpened(boolean opened) {
    this.opened = opened;
  }
}
