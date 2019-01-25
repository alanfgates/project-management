package com.github.alanfgates.project.management;

import java.io.Serializable;
import java.net.URL;

class Link implements Serializable {

  enum LinkType { EMAIL, GOOGLE_DOC, WEBPAGE }

  private final LinkType type;
  private final URL url;

  Link(LinkType type, URL url) {
    this.type = type;
    this.url = url;
  }

  LinkType getType() {
    return type;
  }

  URL getUrl() {
    return url;
  }

  static LinkType parseLinkType(String linkType) throws IllegalArgumentException {
    return LinkType.valueOf(linkType.toUpperCase());
  }

  @Override
  public String toString() {
    return "type: " + type.name().toLowerCase().replace('_', ' ') + " url: " + url;
  }
}
