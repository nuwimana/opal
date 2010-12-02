/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.DisallowedCharactersValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredOptionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter.DatasourcesRefreshedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class CreateViewStepPresenter extends WidgetPresenter<CreateViewStepPresenter.Display> implements Wizard {
  //
  // Instance Variables
  //

  @Inject
  private DatasourceSelectorPresenter datasourceSelectorPresenter;

  @Inject
  private TableListPresenter tableListPresenter;

  private String datasourceName;

  //
  // Constructors
  //

  @Inject
  public CreateViewStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    datasourceSelectorPresenter.bind();
    datasourceSelectorPresenter.setDatasourcesRefreshedCallback(new DatasourcesRefreshedCallback() {

      public void onDatasourcesRefreshed() {
        if(datasourceName != null) {
          getDisplay().getDatasourceName().setText(datasourceName);
        }
      }
    });
    getDisplay().setDatasourceSelector(datasourceSelectorPresenter.getDisplay());

    tableListPresenter.bind();
    getDisplay().setTableSelector(tableListPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    datasourceSelectorPresenter.unbind();
    datasourceSelectorPresenter.setDatasourcesRefreshedCallback(null);

    tableListPresenter.unbind();
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addCancelHandler(new CancelHandler()));
    super.registerHandler(getDisplay().addCreateHandler(new CreateHandler()));
    super.registerHandler(getDisplay().addCloseHandler(new CloseHandler()));
    super.registerHandler(getDisplay().addConfigureHandler(new ConfigureHandler()));
    getDisplay().setTablesValidator(new TablesValidator());
    getDisplay().setSelectTypeValidator(new SelectTypeValidator());
  }

  @Override
  public void revealDisplay() {
    datasourceSelectorPresenter.refreshDisplay();
    tableListPresenter.getTables().clear();

    getDisplay().clear();
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
    datasourceSelectorPresenter.refreshDisplay();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Wizard Methods
  //

  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
    }
  }

  //
  // Methods
  //

  private void createViewIfDoesNotExist() {
    // Get the view name and datasource name.
    String viewName = getDisplay().getViewName().getText();
    String datasourceName = getDisplay().getDatasourceName().getText();

    ViewFoundDoNotCreateCallback doNotCreate = new ViewFoundDoNotCreateCallback();
    ViewNotFoundCreateCallback create = new ViewNotFoundCreateCallback();

    // Create the resource request (the builder).
    getViewRequest(datasourceName, viewName).withCallback(Response.SC_OK, doNotCreate)//
    .withCallback(Response.SC_NOT_FOUND, create).send();
  }

  private ResourceRequestBuilder<ViewDto> getViewRequest(String datasourceName, String viewName) {
    return ResourceRequestBuilderFactory.<ViewDto> newBuilder()//
    .get()//
    .forResource("/datasource/" + datasourceName + "/view/" + viewName)//
    .accept("application/x-protobuf+json");
  }

  private void createView() {
    getDisplay().renderPendingConclusion();
    // Get the view name and datasource name.
    String viewName = getDisplay().getViewName().getText();
    String datasourceName = getDisplay().getDatasourceName().getText();

    // Build the ViewDto for the request.
    ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder().fromTables(tableListPresenter.getTables());
    if(getDisplay().getApplyGlobalVariableFilterOption().getValue()) {
      viewDtoBuilder.defaultJavaScriptView();
    } else if(getDisplay().getAddVariablesOneByOneOption().getValue()) {
      viewDtoBuilder.defaultVariableListView();
    }
    ViewDto viewDto = viewDtoBuilder.build();

    CompletedCallback completed = new CompletedCallback();
    FailedCallback failed = new FailedCallback();

    // Create the resource request (the builder).
    ResourceRequestBuilder<JavaScriptObject> resourceRequestBuilder = ResourceRequestBuilderFactory.newBuilder()//
    .put()//
    .forResource("/datasource/" + datasourceName + "/view/" + viewName)//
    .accept("application/x-protobuf+json").withResourceBody(ViewDto.stringify(viewDto))//
    .withCallback(Response.SC_CREATED, completed)//
    .withCallback(Response.SC_OK, completed)//
    .withCallback(Response.SC_BAD_REQUEST, failed)//
    .withCallback(Response.SC_NOT_FOUND, failed)//
    .withCallback(Response.SC_METHOD_NOT_ALLOWED, failed)//
    .withCallback(Response.SC_INTERNAL_SERVER_ERROR, failed);

    resourceRequestBuilder.send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void clear();

    void setDatasourceSelector(DatasourceSelectorPresenter.Display datasourceSelector);

    void setTableSelector(TableListPresenter.Display tableSelector);

    HasText getDatasourceName();

    HasText getViewName();

    HasValue<Boolean> getApplyGlobalVariableFilterOption();

    HasValue<Boolean> getAddVariablesOneByOneOption();

    void setSelectTypeValidator(ValidationHandler validator);

    void setTablesValidator(ValidationHandler validator);

    HandlerRegistration addCancelHandler(ClickHandler handler);

    HandlerRegistration addCreateHandler(ClickHandler handler);

    HandlerRegistration addCloseHandler(ClickHandler handler);

    void showDialog();

    void hideDialog();

    void renderPendingConclusion();

    void renderFailedConclusion(String msg);

    void renderCompletedConclusion();

    HandlerRegistration addConfigureHandler(ClickHandler handler);

  }

  class CancelHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().hideDialog();
    }
  }

  final class CloseHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent evt) {
      eventBus.fireEvent(new DatasourceUpdatedEvent(datasourceSelectorPresenter.getSelectionDto()));
      getDisplay().hideDialog();
    }
  }

  final class ConfigureHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent evt) {
      eventBus.fireEvent(new DatasourceUpdatedEvent(datasourceSelectorPresenter.getSelectionDto()));

      // Get the new view dto
      getViewRequest(getDisplay().getDatasourceName().getText(), getDisplay().getViewName().getText())//
      .withCallback(new ResourceCallback<ViewDto>() {

        @Override
        public void onResource(Response response, ViewDto resource) {
          getDisplay().hideDialog();
          eventBus.fireEvent(new ViewConfigurationRequiredEvent(resource));
        }
      }).send();

    }
  }

  private class FailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String msg = "UnknownError";
      if(response.getText() != null && response.getText().length() != 0) {
        try {
          ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          msg = errorDto.getStatus();
        } catch(Exception e) {

        }
      }
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, msg, null));
      getDisplay().renderFailedConclusion(msg);
    }
  }

  private class CompletedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().renderCompletedConclusion();
    }
  }

  class SelectTypeValidator extends AbstractValidationHandler {

    public SelectTypeValidator() {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new RequiredTextValidator(getDisplay().getViewName(), "ViewNameRequired"));
      validators.add(new DisallowedCharactersValidator(getDisplay().getViewName(), new char[] { '.', ':' }, "ViewNameDisallowedChars"));
      validators.add(new RequiredOptionValidator(RequiredOptionValidator.asSet(getDisplay().getApplyGlobalVariableFilterOption(), getDisplay().getAddVariablesOneByOneOption()), "VariableDefinitionMethodRequired"));

      return validators;
    }

  }

  class TablesValidator extends AbstractValidationHandler {

    public TablesValidator() {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        public Collection<TableDto> getCollection() {
          return tableListPresenter.getTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<TableDto>(tablesField, 1, "TableSelectionRequired"));
      validators.add(new MatchingTableEntitiesValidator(tablesField));
      return validators;
    }

  }

  class CreateHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      createViewIfDoesNotExist();
    }
  }

  class ViewFoundDoNotCreateCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "ViewAlreadyExists", null));
    }
  }

  class ViewNotFoundCreateCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      createView();
    }
  }
}
