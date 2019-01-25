package com.github.alanfgates.project.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class ProjectManagement {

  final private WorkStream head;
  private File db;

  ProjectManagement(String filename) throws IOException, ClassNotFoundException {
    this(filename, false);

  }

  ProjectManagement(String projName, boolean create) throws IOException, ClassNotFoundException {
    String filename = projName + ".proj";
    db = new File(filename);
    if (create) {
      if (db.exists()) throw new IOException("Project " + projName + " already exists, won't overwrite.");
      head = new WorkStream(null, projName);
      head.setDescription("Head stream for project " + projName);
      commit();
    } else {
      if (!db.exists()) {
        throw new IOException("No project " + projName +
            ", please check you have the correct name or explicitly create a new project management instance.");
      }
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(db));
      head = (WorkStream)in.readObject();
    }
  }

  WorkStream getHead() {
    return head;
  }

  void commit() throws IOException {
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(db));
    out.writeObject(head);
    out.close();
  }




}
