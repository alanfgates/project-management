package com.github.alanfgates.project.management;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Repl {

  private final ProjectManagement proj;
  private final WorkStream head;
  private TaskOrStream current;
  private BufferedReader input;

  private Repl(ProjectManagement proj) {
    this.proj = proj;
    head = proj.getHead();
  }

  private void repl() throws IOException {
    input = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("Welcome to the project management system");
    current = head;
    while (true) {
      boolean changed = false;
      try {
        tree(current, 0);
        String command = input.readLine().toLowerCase();
        if (command.length() == 0) {
          continue;
        } else if (current instanceof WorkStream && command.equals("as")) {
          addStream();
          changed = true;
        } else if (current instanceof WorkStream && command.equals("at")) {
          addTask();
          changed = true;
        } else if (command.equals("d")) {
          changed = delete();
        } else if (command.equals("e")) {
          changed = edit();
        } else if (command.equals("h") || command.equals("?")) {
          help();
        } else if (command.equals("l")) {
          list();
        } else if (current instanceof Task && command.equals("m")) {
          markDone();
          changed = true;
        } else if (command.equals("p")) {
          parent();
        } else if (command.equals("q")) {
          System.out.println("Goodbye");
          return;
        } else if (current instanceof WorkStream && command.equals("s")) {
          child();
        } else if (current instanceof WorkStream && command.equals("td")) {
          allTasksByDueDate();
        } else if (current instanceof WorkStream && command.equals("tp")) {
          allTasksByPriority();
        } else if (current instanceof WorkStream && command.equals("tt")) {
          allTasksToday();
        } else {
          System.err.println("I don't understand the command " + command);
        }
      } finally {
        if (changed) proj.commit();
      }
    }
  }

  private void tree(TaskOrStream node, int level) {
    for (int i = 0; i < level; i++) System.out.print("    ");
    System.out.println(node.getName());
    if (node instanceof WorkStream) {
      for (TaskOrStream child : node.getChildren()) tree(child, level + 1);
    }

  }

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

  private void child() throws IOException {
    pickChildFromOptions(current.getChildren().asCollection());
  }

  private void pickChildFromOptions(Collection<TaskOrStream> options) throws IOException {
    System.out.print("Enter name of child to select (");
    for (TaskOrStream child : options) System.out.print(child.getName() + " ");
    System.out.print(")? ");
    String name = input.readLine();
    System.out.println();
    SortedSet<TaskOrStream> possibilities = new TreeSet<>(Comparator.comparing(TaskOrStream::getName));
    for (TaskOrStream option : options) {
      if (option.getName().startsWith(name)) possibilities.add(option);
    }
    if (possibilities.size() == 0) {
      System.err.println(name + " does not refer to any child of this node");
    } else if (possibilities.size() > 1) {
      System.out.println("Ambiguous, keep going");
      pickChildFromOptions(possibilities);
    } else {
      current = possibilities.iterator().next();
    }
  }
  private boolean delete() throws IOException {
    if (current == head) {
      System.err.println("You can't delete the head node!");
      return false;
    }
    String reponse = getInput("Delete " + current.getName() + ", are you sure? ");
    if (reponse.toLowerCase().startsWith("y")) {
      try {
        current.delete();
        parent();
        return true;
      } catch (StreamNotEmptyException e) {
        System.err.println("Node " + current.getName() + " is not empty and cannot be deleted");
      }
    }
    return false;
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
        URL link = getLink();
        if (link != null) task.setLink(link);
        return true;
      } else if (field.equals("rl")) {
        task.setLink(null);
        return true;
      }
    }

    System.err.println("Sorry, I didn't under the command " + field);
    return false;
  }

  private void help() {
    if (current instanceof WorkStream) {
      System.out.println("as: add a stream to this node");
      System.out.println("at: add a task to this node");
    }
    System.out.println("d: delete this node");
    System.out.println("e: edit this node");
    System.out.println("h or ?: print this");
    System.out.println("l: list (describe) this node");
    if (current instanceof Task) System.out.println("m: mark done");
    System.out.println("p: move to parent node");
    System.out.println("q: quit");
    if (current instanceof WorkStream) {
      System.out.println("s: select a child node");
      System.out.println("td: show all tasks under this node sorted by due date");
      System.out.println("tp: show all tasks under this node sorted by priority");
      System.out.println("tt: show all tasks under this node due today");
    }
  }

  private void list() {
    System.out.println("Name: " + current.getName());
    System.out.println("Description: " + current.getDescription());
    System.out.println("Created At: " + current.getCreatedAt().toString());
    if (current instanceof WorkStream) {
      for (TaskOrStream child : current.getChildren()) System.out.println(child.getName() + " ");
      System.out.println();
    } else if (current instanceof Task) {
      Task currentTask = (Task)current;
      LocalDate dueBy = currentTask.getDueBy();
      if (dueBy != null && !dueBy.equals(Task.END_OF_THE_WORLD)) System.out.println("Due By: " + dueBy.toString());
      System.out.println("Priority: " + currentTask.getPriority().name().toLowerCase());
      if (currentTask.getLink() != null) System.out.println("Link: " + currentTask.getLink().toString());
      System.out.println();
    }
  }

  private void markDone() {
    ((Task)current).markDone();
    parent();
  }

  private void parent() {
    if (current.getParent() != null) current = current.getParent();
  }

  private void allTasksByDueDate() {
    /*
    List<Task> tasks = new ArrayList<>(current.getAllTasks());
    tasks.sort(Comparator.comparing(Task::getDueBy));
    for (Task task : tasks) {
      System.out.print(task.buildName() + " ");
      if (!task.getDueBy().equals(Task.END_OF_THE_WORLD)) System.out.print(task.getDueBy().toString());
      System.out.println();
    }
    */
  }

  private void allTasksByPriority() {
    /*
    List<Task> tasks = new ArrayList<>(current.getAllTasks());
    tasks.sort(Comparator.comparing(Task::getPriority));
    for (Task task : tasks) {
      System.out.print(task.buildName() + " ");
      Priority p = task.getPriority();
      if (p != null) System.out.print(p.name().toLowerCase());
      System.out.println();
    }
    */
  }

  private void allTasksToday() {
    /*
    for (Task task : current.getAllTasks()) {
      if (task.getDueBy() != null && LocalDate.now().compareTo(task.getDueBy()) >= 0) {
        Priority p = task.getPriority();
        System.out.print(task.buildName() + " " + task.getDueBy().toString());
        if (p != null) System.out.print(" " + p.name().toLowerCase());
        System.out.println();
      }
    }
    */
  }

  private String getInput(String prompt) throws IOException {
    System.out.print(prompt + "? ");
    return input.readLine();
  }

  private LocalDate getDueBy() throws IOException {
    String date = getInput("Due By").toLowerCase();
    try {
      return Task.parseDateString(date);
    } catch (InvalidInputException e) {
      System.err.println(e.getMessage());
      return getDueBy();
    }
  }

  private Priority getPriority() throws IOException {
    String priority = getInput("Priority");
    try {
      return Task.parsePriorityString(priority);
    } catch (IllegalArgumentException e) {
      System.err.println("Don't know priority " + priority + ", valid values are 'high', 'low', 'medium'");
      return getPriority();
    }
  }

  private URL getLink() throws IOException {
    try {
      String urlString = getInput("URL");
      return new URL(urlString);
    } catch (MalformedURLException e) {
      System.err.println("Bad URL: " + e.getMessage());
    }
    return getLink();
  }

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

      Repl repl = new Repl(project);
      repl.repl();

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


