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
      List<TaskOrStream> children = new ArrayList<>(node.getAllChildren());
      children.sort(Comparator.comparing(TaskOrStream::getName));
      for (TaskOrStream child : children) tree(child, level + 1);
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
    pickChildFromOptions(current.getAllChildren());
  }

  private void pickChildFromOptions(Collection<TaskOrStream> options) throws IOException {
    System.out.print("Enter name of child to select (");
    for (TaskOrStream child : options) System.out.print(child.getName() + " ");
    System.out.print(")? ");
    String name = input.readLine();
    System.out.println();
    List<TaskOrStream> possibilities = new ArrayList<>();
    for (TaskOrStream option : options) {
      if (option.getName().startsWith(name)) possibilities.add(option);
    }
    if (possibilities.size() == 0) {
      System.err.println(name + " does not refer to any child of this node");
    } else if (possibilities.size() > 1) {
      System.out.println("Ambiguous, keep going");
      pickChildFromOptions(possibilities);
    } else {
      current = possibilities.get(0);
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
    }
  }

  private void list() {
    System.out.println("Name: " + current.getName());
    System.out.println("Description: " + current.getDescription());
    System.out.println("Created At: " + current.getCreatedAt().toString());
    if (current instanceof WorkStream) {
      System.out.print("Substreams: ");
      for (WorkStream stream : current.getStreams()) System.out.print(stream.getName() + " ");
      System.out.println();
      System.out.print("Tasks: ");
      for (Task task : current.getTasks()) System.out.print(task.getName() + " ");
      System.out.println();
    } else if (current instanceof Task) {
      Task currentTask = (Task)current;
      LocalDate dueBy = currentTask.getDueBy();
      if (dueBy != null) System.out.println("Due By: " + dueBy.toString());
      Priority priority = currentTask.getPriority();
      if (priority != null) System.out.println("Priority: " + priority.name().toLowerCase());
      List<Link> links = currentTask.getLinks();
      if (links.size() > 0) {
        System.out.print("Links: ");
        for (Link link : currentTask.getLinks()) System.out.print(link + " ");
      }
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
    List<Task> tasks = new ArrayList<>(current.getAllTasks());
    tasks.sort(Comparator.comparing(Task::getDueBy));
    for (Task task : tasks) System.out.println(task.getName() + " " + task.getDueBy());
  }

  private void allTasksByPriority() {
    List<Task> tasks = new ArrayList<>(current.getAllTasks());
    tasks.sort(Comparator.comparing(Task::getPriority));
    for (Task task : tasks) {
      System.out.print(task.getName() + " ");
      Priority p = task.getPriority();
      if (p != null) System.out.print(p.name().toLowerCase());
      System.out.println();
    }
  }

  private String getInput(String prompt) throws IOException {
    System.out.print(prompt + "? ");
    return input.readLine();
  }

  private LocalDate getDueBy() throws IOException {
    String date = getInput("Due By").toLowerCase();
    try {
      return Task.parseDateString(date);
    } catch (IllegalArgumentException e) {
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

  private Link getLink() throws IOException {
    String linkTypeStr = getInput("Link Type");
    if (linkTypeStr.length() == 0) return null;
    try {
      Link.LinkType linkType = Link.parseLinkType(linkTypeStr);
      String urlString = getInput("URL");
      URL url = new URL(urlString);
      return new Link(linkType, url);

    } catch (IllegalArgumentException e) {
      System.err.print("Unknown Link Type " + linkTypeStr + ", valid values are: ");
      for (Link.LinkType type : Link.LinkType.values()) System.err.print(type.name().toLowerCase() + " ");
      System.err.println();
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


