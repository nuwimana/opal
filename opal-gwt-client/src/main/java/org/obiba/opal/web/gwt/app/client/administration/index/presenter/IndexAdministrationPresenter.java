/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.index.event.TableIndicesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableIndexStatusRefreshEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ServiceDto;
import org.obiba.opal.web.model.client.opal.ServiceStatus;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class IndexAdministrationPresenter
    extends ItemAdministrationPresenter<IndexAdministrationPresenter.Display, IndexAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.INDEX)
  public interface Proxy extends ProxyPlace<IndexAdministrationPresenter> {}

  private static final int DELAY_MILLIS = 1500;

  public interface Display extends View, HasBreadcrumbs {

    String INDEX_ACTION = "Index now";

    String CLEAR_ACTION = "Clear";

    String SCHEDULE = "Schedule indexing";

    void serviceStartable();

    void serviceStoppable();

    void serviceExecutionPending();

    void unselectIndex(TableIndexStatusDto object);

    enum Slots {
      Drivers, Permissions
    }

    HasClickHandlers getStartButton();

    HasClickHandlers getStopButton();

    Button getConfigureButton();

    HasClickHandlers getRefreshButton();

    void renderRows(JsArray<TableIndexStatusDto> rows);

    void clear();

    List<TableIndexStatusDto> getSelectedIndices();

    DropdownButton getActionsDropdown();

    HasActionHandler<TableIndexStatusDto> getActions();

    HasData<TableIndexStatusDto> getIndexTable();

  }

  private final ModalProvider<IndexPresenter> indexModalProvider;

  private final ModalProvider<IndexConfigurationPresenter> indexConfigurationProvider;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @SuppressWarnings("FieldCanBeLocal")
  private final AuthorizationPresenter authorizationPresenter;

  @SuppressWarnings("UnusedDeclaration")
  private Command confirmedCommand;

  @Inject
  public IndexAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      Provider<AuthorizationPresenter> authorizationPresenter, ModalProvider<IndexPresenter> indexModalProvider,
      ModalProvider<IndexConfigurationPresenter> indexConfigurationProvider,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.indexModalProvider = indexModalProvider.setContainer(this);
    this.authorizationPresenter = authorizationPresenter.get();
    this.indexConfigurationProvider = indexConfigurationProvider.setContainer(this);
    this.breadcrumbsHelper = breadcrumbsHelper;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.indices()).get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListIndicesAuthorization())).send();
  }

  @Override
  public String getName() {
    return translations.indicesLabel();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    // stop start search service
    ResourceRequestBuilderFactory.<ServiceDto>newBuilder().forResource(Resources.searchService()).get()
        .withCallback(new ResourceCallback<ServiceDto>() {
          @Override
          public void onResource(Response response, ServiceDto resource) {
            if(response.getStatusCode() == Response.SC_OK) {
              if(resource.getStatus().isServiceStatus(ServiceStatus.RUNNING)) {
                getView().serviceStoppable();
              } else {
                getView().serviceStartable();
              }
            }
          }
        }).send();

    getView().getIndexTable().setVisibleRange(0, 10);
    refresh();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.indices()).get().authorize(authorizer)
        .send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageSearchIndexTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {

      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if(event.getSource() == confirmedCommand && event.isConfirmed()) {
          confirmedCommand.execute();
        }
      }
    }));

    // Dropdown Actions
    getView().getActionsDropdown().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        getView();
        if(getView().getActionsDropdown().getLastSelectedNavLink().getText().trim().equals(Display.CLEAR_ACTION)) {
          doClear();
        } else if(getView().getActionsDropdown().getLastSelectedNavLink().getText().trim().equals(Display.SCHEDULE)) {
          doSchedule();
        }
      }

      private void doClear() {
        if(getView().getSelectedIndices().isEmpty()) {
          getEventBus()
              .fireEvent(NotificationEvent.Builder.newNotification().error("IndexClearSelectAtLeastOne").build());
        } else {

          for(TableIndexStatusDto object : getView().getSelectedIndices()) {
            ResponseCodeCallback callback = new ResponseCodeCallback() {

              @Override
              public void onResponseCode(Request request, Response response) {
                refresh();
              }

            };
            ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
                .forResource(Resources.index(object.getDatasource(), object.getTable())).accept("application/json")//
                .withCallback(Response.SC_OK, callback)//
                .withCallback(Response.SC_SERVICE_UNAVAILABLE, callback).delete().send();

            getView().unselectIndex(object);
          }
        }
      }

      private void doSchedule() {
        if(getView().getSelectedIndices().isEmpty()) {
          getEventBus()
              .fireEvent(NotificationEvent.Builder.newNotification().error("IndexScheduleSelectAtLeastOne").build());
        } else {

          List<TableIndexStatusDto> objects = new ArrayList<TableIndexStatusDto>();
          for(TableIndexStatusDto object : getView().getSelectedIndices()) {
            objects.add(object);
          }

          IndexPresenter dialog = indexModalProvider.get();
          dialog.updateSchedules(objects);
        }
      }
    });

    getView().getActions().setActionHandler(new ActionHandler<TableIndexStatusDto>() {

      @SuppressWarnings("UnnecessaryFinalOnLocalVariableOrParameter")
      @Override
      public void doAction(final TableIndexStatusDto object, String actionName) {
        if(actionName.trim().equalsIgnoreCase(Display.CLEAR_ACTION)) {
          ResponseCodeCallback callback = new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == Response.SC_OK) {
                refresh();
              } else {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getEventBus().fireEvent(
                    NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                        .build());
              }
            }

          };
          ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
              .forResource(Resources.index(object.getDatasource(), object.getTable())).accept("application/json")//
              .withCallback(Response.SC_OK, callback)//
              .withCallback(Response.SC_SERVICE_UNAVAILABLE, callback).delete().send();
        } else if(actionName.trim().equalsIgnoreCase(Display.INDEX_ACTION)) {
          ResponseCodeCallback callback = new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == Response.SC_OK) {
                // Wait a few seconds for the task to launch before checking its status
                Timer t = new Timer() {
                  @Override
                  public void run() {
                    refresh(false);
                  }
                };
                // Schedule the timer to run once in X seconds.
                t.schedule(DELAY_MILLIS);
              } else {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getEventBus().fireEvent(
                    NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                        .build());
              }
            }

          };
          ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
              .forResource(Resources.index(object.getDatasource(), object.getTable())).accept("application/json")//
              .withCallback(Response.SC_OK, callback) //
              .withCallback(Response.SC_SERVICE_UNAVAILABLE, callback).put().send();
        }
      }

    });

    // REFRESH
    registerHandler(getView().getRefreshButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        refresh();
      }
    }));

    registerHandler(
        getEventBus().addHandler(TableIndicesRefreshEvent.getType(), new TableIndicesRefreshEvent.Handler() {
          @Override
          public void onRefresh(TableIndicesRefreshEvent event) {
            refresh();
          }
        }));

    // STOP
    registerHandler(getView().getStopButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResponseCodeCallback callback = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_OK) {
              getView().serviceStartable();
              getView().clear();
              getEventBus().fireEvent(new TableIndexStatusRefreshEvent());
            } else {
              getView().clear();
              getView().serviceStoppable();
              ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
              getEventBus().fireEvent(
                  NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                      .build());
            }
          }

        };

        // Stop service
        getView().serviceExecutionPending();
        ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
            .forResource(Resources.searchServiceEnabled()).accept("application/json")//
            .withCallback(Response.SC_OK, callback)//
            .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback).delete().send();
      }
    }));

    // START
    registerHandler(getView().getStartButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResponseCodeCallback callback = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_OK) {
              getView().serviceStoppable();
              refresh();
              getEventBus().fireEvent(new TableIndexStatusRefreshEvent());
            } else {
              getView().serviceStartable();
              getEventBus().fireEvent(NotificationEvent.Builder.newNotification().error(response.getText()).build());
            }
          }

        };

        // Start service
        getView().serviceExecutionPending();
        ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
            .forResource(Resources.searchServiceEnabled()).accept("application/json")//
            .withCallback(callback, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR).put().send();
      }
    }));

    // CONFIGURE
    getView().getConfigureButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        indexConfigurationProvider.get();
      }
    });
  }

  private void refresh() {
    refresh(true);
  }

  private void refresh(boolean clearIndices) {
    // Fetch all indices
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
        .forResource(Resources.indices())//
        .withCallback(new TableIndexStatusResourceCallback(getView().getIndexTable().getVisibleRange(), clearIndices))//
        .withCallback(Response.SC_SERVICE_UNAVAILABLE, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // nothing
          }
        })//
        .get().send();
  }

  private class TableIndexStatusResourceCallback implements ResourceCallback<JsArray<TableIndexStatusDto>> {
    private final Range r;

    private boolean clearIndices;

    private TableIndexStatusResourceCallback(Range r, boolean clearIndices) {
      this.r = r;
      this.clearIndices = clearIndices;
    }

    @Override
    public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {
      getView().renderRows(resource);
      getView().getIndexTable().setVisibleRangeAndClearData(r, true);

      if(clearIndices) getView().getSelectedIndices().clear();
    }
  }

  private final class ListIndicesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {

      // Fetch all indices
      ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
          .forResource(Resources.indices()).withCallback(new ResourceCallback<JsArray<TableIndexStatusDto>>() {
        @Override
        public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {
          getView().renderRows(resource);
        }
      })//
          .withCallback(Response.SC_SERVICE_UNAVAILABLE, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              // nothing
            }
          }).get().send();

    }

    @Override
    public void unauthorized() {
    }

  }

}
