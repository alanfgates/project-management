package com.github.alanfgates.project.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class ProjectManagement {

  final private WorkStream head;
  private File db;

  ProjectManagement(String filename) throws IOException, ClassNotFoundException {
    this(filename, false);

  }

  ProjectManagement(String projName, boolean create) throws IOException, ClassNotFoundException {
    String filename = "project-" + projName + ".yaml";
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
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      ObjectReader reader = mapper.readerFor(WorkStream.class);
      head = reader.readValue(db);
      head.connectChildren();
    }
  }

  WorkStream getHead() {
    return head;
  }

  void commit() throws IOException {
    if (db.exists()) {
      File backup = new File(db.getAbsolutePath() + ".bak");
      if (backup.exists()) backup.delete();
      Files.copy(db.toPath(), backup.toPath());
    }
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.writeValue(db, head);
  }




}
