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
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
        System.out.print(current.getName() + ": ");
        String command = input.readLine().toLowerCase();
        if (command.length() == 0) {
          continue;
        } else if (current instanceof WorkStream && command.equals("a")) {
          changed = add();
        } else if (command.equals("d")) {
          changed = delete();
        } else if (command.equals("e")) {
          changed = edit();
        } else if (command.equals("h") || command.equals("?")) {
          help();
        } else if (command.equals("l")) {
          list();
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
        } else {
          System.err.println("I don't understand the command " + command);
        }
      } finally {
        if (changed) proj.commit();
      }
    }
  }

  private boolean add() throws IOException {
    WorkStream currentStream = (WorkStream) current;
    System.out.print("Substream or task[s/t]?");
    String command = input.readLine().toLowerCase();
    System.out.println();
    if (command.equals("s")) {
      String name = getInput("Name");
      WorkStream substream = new WorkStream(currentStream, name);
      ((WorkStream) current).addStream(substream);
      String description = getInput("Description");
      substream.setDescription(description);
      return true;
    } else if (command.equals("t")) {
      String name = getInput("Name");
      Task task = new Task(currentStream, name);
      ((WorkStream) current).addTask(task);
      String description = getInput("Description");
      task.setDescription(description);
      task.setDueBy(getDateTime("Due By"));
      String priority = getInput("Priority");
      if (priority.length() > 0) task.setPriority(Priority.valueOf(priority));
      System.out.println();
      return true;
    } else {
      System.err.println("I don't know how to add a " + command);
      return false;
    }
  }

  private void child() throws IOException {
    pickChildFromOptions(current.getAllChildren());
  }

  private void pickChildFromOptions(Set<TaskOrStream> options) throws IOException {
    System.out.print("Enter name of child to select (");
    for (TaskOrStream child : options) System.out.print(child.getName() + " ");
    System.out.print(")? ");
    String name = input.readLine();
    System.out.println();
    for (TaskOrStream option : options) {
      if (!option.getName().startsWith(name)) options.remove(option);
    }
    if (options.size() == 0) {
      System.err.println(name + " does not refer to any child of this node");
    } else if (options.size() > 1) {
      System.out.println("Ambiguous, keep going");
      pickChildFromOptions(options);
    } else {
      current = options.iterator().next();
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
        String priority = getInput("Priority");
        task.setPriority(Priority.valueOf(priority));
        return true;
      } else if (field.equals("u")) {
        task.setDueBy(getDateTime("Due By"));
        return true;
      } else if (field.equals("al")) {
        String type = getInput("Link type");
        String link = getInput("URL");
        task.addLink(new Link(Link.LinkType.valueOf(type), new URL(link)));
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

  private void help() {
    if (current instanceof WorkStream) {
      System.out.println("a: add a task or stream to this node");
    }
    System.out.println("d: delete this node");
    System.out.println("e: edit this node");
    System.out.println("h or ?: print this");
    System.out.println("l: list (describe) this node");
    System.out.println("p: move to parent node");
    System.out.println("q: quit");
    if (current instanceof WorkStream) {
      System.out.println("s: select a child node");
      System.out.println("td: show all tasks under this node sorted by due date");
      System.out.println("tp: show all tasks under this node sorted by priority");
    }
  }

  private void list() {
    list(current);
  }

  private void list(TaskOrStream node) {
    System.out.println("Name: " + node.getName());
    System.out.println("Description: " + node.getDescription());
    System.out.println("Created At: " + node.getCreatedAt().toString());
    System.out.println("Last Modified: " + node.getLastModified().toString());
    if (node instanceof WorkStream) {
      System.out.print("Substreams: ");
      for (WorkStream stream : node.getStreams()) System.out.print(stream.getName() + " ");
      System.out.println();
      System.out.print("Tasks: ");
      for (Task task : node.getTasks()) System.out.print(task.getName() + " ");
      System.out.println();
    } else if (node instanceof Task) {
      Task currentTask = (Task)node;
      System.out.println("Due By: " + currentTask.getDueBy().toString());
      System.out.println("Priority: " + currentTask.getPriority());
      System.out.print("Links: ");
      for (Link link : currentTask.getLinks()) System.out.print(link + " ");
      System.out.println();
    }
  }

  private void parent() {
    if (current.getParent() != null) current = current.getParent();
  }

  private void allTasksByDueDate() {
    List<Task> tasks = new ArrayList<>(current.getAllTasks());
    tasks.sort(Comparator.comparing(Task::getDueBy));
    for (Task task : tasks) list(task);
  }

  private void allTasksByPriority() {
    List<Task> tasks = new ArrayList<>(current.getAllTasks());
    tasks.sort(Comparator.comparing(Task::getPriority));
    for (Task task : tasks) list(task);
  }

  private String getInput(String prompt) throws IOException {
    System.out.print(prompt + "? ");
    return input.readLine();
  }

  private LocalDate getDateTime(String prompt) throws IOException {
    String date = getInput(prompt).toLowerCase();
    if (date.length() == 0) return null;
    if (date.startsWith("tod")) {
      return LocalDate.now();
    } else if (date.startsWith("tom")) {
      return LocalDate.now().plusDays(1);
    } else  {
      // Assume it's of the form mm/dd
      try {
        LocalDate now = LocalDate.now();
        int month = Integer.valueOf(date.substring(0, 3));
        int day = Integer.valueOf(date.substring(3));
        int year = now.getYear();
        if (month < now.getMonthValue()) year++;
        return LocalDate.of(year, month, day);
      } catch (NumberFormatException e) {
        System.err.println("Cannot parse " + date + ", please use 'today', 'tomorrow', or mm/dd");
        return getDateTime(prompt);
      }
    }
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


