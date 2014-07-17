package com.continuuity.api.metadata;

import java.util.Set;

/**
 * Metadata of a service.
 */
public class ServiceMeta {

  private final String id;
  private final String name;
  private final String description;
  private final Set<String> runnables;

  public ServiceMeta(String id, String name, String description, Set<String> runnables) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.runnables = runnables;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Set<String> getRunnables() {
    return runnables;
  }
}
