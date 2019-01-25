package com.github.alanfgates.project.management;

import java.io.Serializable;
import java.net.URL;

public class Link implements Serializable {

  public enum LinkType { EMAIL, GOOGLE_DOC, WEBPAGE }

  private LinkType type;
  private URL url;

  /**
   * For jackson
   */
  public Link() {

  }

  Link(LinkType type, URL url) {
    this.type = type;
    this.url = url;
  }

  public LinkType getType() {
    return type;
  }

  public URL getUrl() {
    return url;
  }

  public void setType(LinkType type) {
    this.type = type;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  static LinkType parseLinkType(String linkType) throws IllegalArgumentException {
    return LinkType.valueOf(linkType.toUpperCase());
  }

  @Override
  public String toString() {
    return "type: " + type.name().toLowerCase().replace('_', ' ') + " url: " + url;
  }
}
