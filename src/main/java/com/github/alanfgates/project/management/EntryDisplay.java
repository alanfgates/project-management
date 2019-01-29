package com.github.alanfgates.project.management;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

abstract class EntryDisplay {
  protected final List<EntryDisplay> children;
  protected boolean selected;
  protected boolean opened; // whether all substream/tasks are displayed

  // TODO figure out how to handle deletes and mark dones here, as we need to update this tree

  protected EntryDisplay() {
    children = new ArrayList<>();
  }

  abstract int display(Terminal terminal, int row, int col) throws IOException;

  /*
  int display(Terminal term, int row, int col) throws IOException {
    String prefix = "";
    if (entry instanceof WorkStream) {
      if (opened) {
        prefix = "-";
        for (EntryDisplay child : children) {
          row = child.display(term, row + 1, col + 2);
        }
      } else {
        prefix = "+";
      }
    }
    TextGraphics line = term.newTextGraphics();
    List<SGR> mods = new ArrayList<>();
    if (selected) mods.add(SGR.BOLD);
    if (entry instanceof Task && ((Task)entry).getDueBy() != null &&
        LocalDate.now().compareTo(((Task)entry).getDueBy()) >= 0) {
      mods.add(SGR.ITALIC);
    }
    line.putString(col, row, prefix + entry.getName(), mods);
    return row;
  }
  */

  void setSelected(boolean selected) {
    this.selected = selected;
  }

  void setOpened(boolean opened) {
    this.opened = opened;
  }
}
