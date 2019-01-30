package com.github.alanfgates.project.management;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
  boolean markDone() {
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
}
