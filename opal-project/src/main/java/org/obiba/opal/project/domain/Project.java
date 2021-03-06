/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project.domain;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Description of a project in Opal.
 */
public class Project {

  @Nonnull
  private String name;

  @Nonnull
  private String title;

  private String description;

  private List<String> tags;

  private boolean archived;

  private String database;

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  public boolean hasTitle() {
    return !Strings.isNullOrEmpty(title);
  }

  @Nonnull
  public String getTitle() {
    return title;
  }

  public void setTitle(@Nonnull String title) {
    this.title = title;
  }

  public boolean hasDescription() {
    return !Strings.isNullOrEmpty(description);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean hasTags() {
    return getTags().size() > 0;
  }

  public List<String> getTags() {
    return tags == null ? tags = Lists.newArrayList() : tags;
  }

  public void addTag(String tag) {
    if(!getTags().contains(tag)) {
      getTags().add(tag);
    }
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private Project project;

    public static Builder create(String name) {
      Builder builder = new Builder();
      builder.project = new Project();
      builder.project.setName(name);
      return builder;
    }

    public Builder name(String name) {
      project.setName(name);
      return this;
    }

    public Builder title(String title) {
      project.setTitle(title);
      return this;
    }

    public Builder description(String description) {
      project.setDescription(description);
      return this;
    }

    public Builder tags(Iterable<String> tags) {
      if(tags == null) return this;
      for(String tag : tags) {
        project.addTag(tag);
      }
      return this;
    }

    public Builder tags(String... tags) {
      if(tags == null) return this;
      for(String tag : tags) {
        project.addTag(tag);
      }
      return this;
    }

    public Builder archived(boolean archived) {
      project.setArchived(archived);
      return this;
    }

    public Builder archived() {
      return archived(true);
    }

    public Builder database(String database) {
      project.setDatabase(database);
      return this;
    }

    public Project build() {
      return project;
    }
  }
}
