package org.obiba.opal.web.gwt.app.client.project.event;

import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ProjectCreatedEvent extends GwtEvent<ProjectCreatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onProjectCreated(ProjectCreatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final ProjectDto projectDto;

  public ProjectCreatedEvent(ProjectDto dto) {
    projectDto = dto;
  }

  public ProjectDto getProjectDto() {
    return projectDto;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProjectCreated(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
