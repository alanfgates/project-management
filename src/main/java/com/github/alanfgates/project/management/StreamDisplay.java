package com.github.alanfgates.project.management;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class StreamDisplay extends EntryDisplay {

  private final WorkStream stream;

  StreamDisplay(WorkStream stream) {
    super();
    this.stream = stream;
    for (TaskOrStream entryChild : stream.getAllChildren()) {
      children.add(entryChild.getDisplay());
    }
  }

  @Override
  int display(Terminal term, int row, int col) throws IOException {
    List<SGR> mods = new ArrayList<>();
    if (selected) mods.add(SGR.BOLD);
    TextGraphics line = term.newTextGraphics();
    if (opened) {
      line.putString(col, row, "-" + stream.getName(), mods);
      for (EntryDisplay child : children) {
        row = child.display(term, row + 1, col + 2);
      }
    } else {
      line.putString(col, row, "+" + stream.getName(), mods);
    }
    return row;
  }
}
