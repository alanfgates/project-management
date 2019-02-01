package com.github.alanfgates.project.management;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextUI {

  private final ProjectManagement proj;
  private final EntryDisplay head;
  private Terminal terminal;

  private TextUI(ProjectManagement proj) {
    this.proj = proj;
    head = proj.getHead().getDisplay();
  }

  private void run() throws IOException {
    terminal = new DefaultTerminalFactory().createTerminal();
    try {

      boolean done = false;
      EntryDisplay current = head;
      current.setSelected(true);
      while (!done) {
        terminal.clearScreen();
        head.display(terminal, 0, 0);
        terminal.setCursorVisible(false);
        terminal.flush();
        com.googlecode.lanterna.input.KeyStroke key = terminal.readInput();
        switch (key.getCharacter()) {
          case 'd':
            if (current == head) {
              showError("You cannot delete the head node!");
              break;
            }
            // TODO need pop up that asks if you're sure
            if (current.delete(this)) {
              current = prevOrNext(current);
              proj.commit();
            }
            break;

          // TODO - e for edit

          case 'H':
            current.setOpenAll(false);
            break;

          case 'h':
            current.setOpened(false);
            break;

          case 'j':
            EntryDisplay tmp = current.next();
            if (tmp != null) {
              current.setSelected(false);
              current = tmp;
              current.setSelected(true);
            }
            break;

          case 'k':
            tmp = current.prev();
            if (tmp != null) {
              current.setSelected(false);
              current = tmp;
              current.setSelected(true);
            }
            break;

          case 'L':
            current.setOpenAll(true);
            break;

          case 'l':
            current.setOpened(true);
            break;

          case 'm':
            if (current.markDone(this)) {
              current = prevOrNext(current);
              proj.commit();
            }
            break;

          case 'q':
            done = true;
            break;

          case 's':
            current.addStream(this);
            proj.commit();
            break;

          case 't':
            current.addTask(this);
            proj.commit();
            break;

          case '\n':
            current.details(this);
            break;

          default:
            current.help(this);
            break;
        }
      }
    } finally {
      terminal.setCursorVisible(true);
      terminal.clearScreen();
      TextGraphics goodbye = terminal.newTextGraphics();
      goodbye.putString(0, 0, "Goodbye");
      terminal.close();

      System.out.println();
    }
  }

  private EntryDisplay prevOrNext(EntryDisplay current) {
    EntryDisplay tmp = current.prev();
    if (tmp != null) {
      current.setSelected(false);
      current = tmp;
      current.setSelected(true);
      return current;
    }
    tmp = current.next();
    if (tmp != null) {
      current.setSelected(false);
      current = tmp;
      current.setSelected(true);
      return current;
    }
    throw new RuntimeException("I've deleted the last node, that's really bad!");
  }

  void showError(String errorMsg) throws IOException {
    displayStrings(Collections.singletonList(errorMsg));
  }

  void displayStrings(List<String> lines) throws IOException {
    lines = fitStrings(lines, terminal.getTerminalSize().getColumns() - 10);
    int maxLineLen = 0;
    for (String line : lines) if (line.length() > maxLineLen) maxLineLen = line.length();
    TerminalPosition errorWindow = centerWindow(lines.size() + 2, maxLineLen);
    TextGraphics display = terminal.newTextGraphics();
    int nextRow = 2;
    for (String line : lines) {
      display.putString(errorWindow.withRelative(new TerminalPosition(2, nextRow++)),
          line);

    }
    // I don't care what the character is
    terminal.readInput();
  }

  /**
   * Given a set of prompts, return a set of answers
   * @param title Title of this input
   * @param inputs list of inputs to collect
   * @return map of prompts to answers
   */
  void getInput(String title, List<InputCollector> inputs) throws IOException {
    terminal.clearScreen();
    int currentRow = 1;
    TextGraphics display = terminal.newTextGraphics();
    display.putString(1, currentRow++, title);
    terminal.setCursorVisible(true);
    int maxLineLen = terminal.getTerminalSize().getColumns();
    try {
      for (InputCollector input : inputs) {
        while (true) {
          display.putString(1, currentRow++, input.getPrompt() + ": ");
          Character c = 'a';
          StringBuilder answer = new StringBuilder();
          while (c != '\t' && c != '\n') {
            c = terminal.readInput().getCharacter();
            if (c != '\t' && c != '\n') {
              answer.append(c);
              terminal.putCharacter(c);
            }
          }
          currentRow += (answer.length() + input.getPrompt().length() + 4) / maxLineLen;
          try {
            input.collect(answer.toString());
            break;
          } catch (InvalidInputException e) {
            display.putString(1, currentRow++, e.getMessage());
          }
        }
      }
    } finally {
      terminal.setCursorVisible(false);
    }
  }

  private TerminalPosition centerWindow(int rows, int cols) throws IOException {
    // Give some borders
    rows += 2;
    cols += 4;
    TerminalSize termSize = terminal.getTerminalSize();
    // TODO handle this better
    if (rows > termSize.getRows()) throw new RuntimeException("window too small");
    if (cols > termSize.getColumns()) throw new RuntimeException("window too small");

    int colMidPt = termSize.getColumns() / 2;
    int rowMidPt = termSize.getRows() / 2;

    int colStartPt = colMidPt - (cols / 2);
    int rowStartPt = rowMidPt - (rows / 2);

    TextGraphics box = terminal.newTextGraphics();
    TerminalPosition boxPos = new TerminalPosition(colStartPt, rowStartPt);
    box.drawRectangle(boxPos, new TerminalSize(cols, rows), '*');
    box.fillRectangle(new TerminalPosition(colStartPt + 1, rowStartPt + 1),
        new TerminalSize(cols - 2, rows - 2), ' ');
    return boxPos;
  }

  private List<String> fitStrings(List<String> input, int maxLen) {
    List<String> output = new ArrayList<>();
    for (String s : input) {
      String oneLine = s.replace('\n', ' ').replace('\t', ' ');
      while (oneLine.length() > maxLen) {
        int breakPoint = maxLen;
        for (int i = maxLen; i > 0; i--) {
          if (oneLine.charAt(i) == ' ') {
            breakPoint = i;
            break;
          }
        }
        output.add(oneLine.substring(0, breakPoint));
        oneLine = oneLine.substring(breakPoint);
      }
      output.add(oneLine);
    }
    return output;
  }

  /*
  private void addStream() throws IOException {
    WorkStream currentStream = (WorkStream) current;
    String name = getInput("Name");
    WorkStream substream = new WorkStream(currentStream, name);
    currentStream.addStream(substream);
    String description = getInput("Description");
    substream.setDescription(description);
  }

  private void addTask() throws IOException {
    WorkStream currentStream = (WorkStream) current;
    String name = getInput("Name");
    Task task = new Task(currentStream, name);
    currentStream.addTask(task);
    String description = getInput("Description");
    task.setDescription(description);
    task.setDueBy(getDueBy());
    task.setPriority(getPriority());
  }

  private boolean edit() throws IOException {
    list();
    System.out.println("Field to edit?");
    System.out.println("n: name");
    System.out.println("d: description");
    if (current instanceof Task) {
      System.out.println("p: priority");
      System.out.println("u: due by");
      System.out.println("al: add link");
      System.out.println("rl: remove link");
    }
    String field = input.readLine();
    if (field.equals("n")) {
      String name = getInput("Name");
      current.setName(name);
      return true;
    } else if (field.equals("d")) {
      String description = getInput("Description");
      current.setDescription(description);
      return true;
    } else if (current instanceof Task) {
      Task task = (Task)current;
      if (field.equals("p")) {
        task.setPriority(getPriority());
        return true;
      } else if (field.equals("u")) {
        task.setDueBy(getDueBy());
        return true;
      } else if (field.equals("al")) {
        Link link = getLink();
        if (link != null) task.addLink(link);
        return true;
      } else if (field.equals("rl")) {
        String num = getInput("Link number to remove");
        int n = Integer.valueOf(num);
        if (n < task.getLinks().size()) {
          task.getLinks().remove(n);
        }
        return true;
      }
    }

    System.err.println("Sorry, I didn't under the command " + field);
    return false;
  }

  */

  public static void main(String[] args) {
    Options options = new Options();

    options.addOption(Option.builder("h")
        .longOpt("help")
        .desc("You're looking at it.")
        .build());
    options.addOption(Option.builder("n")
        .longOpt("new-project")
        .desc("Create a new project")
        .build());
    options.addOption(Option.builder("p")
        .longOpt("project-name")
        .hasArg()
        .desc("Name of this project")
        .build());

    try {
      CommandLine cli = new DefaultParser().parse(options, args);
      String projname = cli.getOptionValue("p");
      ProjectManagement project = null;

      if (cli.hasOption("h") || projname == null) {
        usage(options);
      } else if (cli.hasOption("n")) {
        project = new ProjectManagement(projname, true);
      } else {
        project = new ProjectManagement(projname);
      }

      TextUI gui = new TextUI(project);
      gui.run();

    } catch (ParseException e) {
      System.err.println("Failed to parse the command line: " + e.getMessage());
    } catch (IOException e) {
      System.err.println("Got an IO exception, this is generally bad: " + e.getMessage());
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.err.println("I don't think this should ever really happen: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("project-manager", options);
  }

}


