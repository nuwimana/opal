/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.configureview.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class DataTabPresenter extends PresenterWidget<DataTabPresenter.Display> {

  public interface Display extends View {
    HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler);

    void saveChangesEnabled(boolean enabled);

    void clear();

    void addTableSelections(JsArray<TableDto> tables);

    void selectTables(JsArrayString tableFullNames);

    List<TableDto> getSelectedTables();

    void setTableListListener(TableListListener listener);

  }

  public interface TableListListener {

    void onTableListUpdated();

  }

  private ViewDto viewDto;

  private Request refreshRequest;

  @Inject
  public DataTabPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  public void onReset() {
    getView().saveChangesEnabled(false);
    getView().clear();
    refreshTables();
  }

  private void refreshTables() {
    if(refreshRequest == null || !refreshRequest.isPending()) {
      refreshRequest = ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource("/datasources/tables")
          .get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
            @Override
            public void onResource(Response response, JsArray<TableDto> resource) {
              JsArray<TableDto> tables = JsArrays.toSafeArray(resource);
              TableDto viewTableDto = findViewTabledto(tables);
              getView().addTableSelections(filterTables(tables, viewTableDto));
              if(viewDto != null) {
                getView().selectTables(viewDto.getFromArray());
              }
              refreshRequest = null;
            }

            /**
             * Search for the table corresponding to the view.
             * @param tables
             * @return
             */
            private TableDto findViewTabledto(JsArray<TableDto> tables) {
              if(viewDto == null) return null;

              TableDto viewTableDto = null;
              for(TableDto table : JsArrays.toIterable(tables)) {
                if(viewDto.getDatasourceName().equals(table.getDatasourceName()) &&
                    viewDto.getName().equals(table.getName())) {
                  viewTableDto = table;
                  break;
                }
              }
              return viewTableDto;
            }

            /**
             * Remove from selection the view itself and the tables of different entity types.
             * @param tables
             * @param viewTableDto
             * @return
             */
            private JsArray<TableDto> filterTables(JsArray<TableDto> tables, TableDto viewTableDto) {
              if(viewTableDto == null) return tables;

              JsArray<TableDto> filteredTables = JsArrays.create();
              for(TableDto table : JsArrays.toIterable(tables)) {
                if(!table.equals(viewTableDto) && table.getEntityType().equals(viewTableDto.getEntityType())) {
                  filteredTables.push(table);
                }
              }
              return filteredTables;
            }

          }).send();
    }
  }

  private void addEventHandlers() {
    registerHandler(getEventBus()
        .addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    registerHandler(getView().addSaveChangesClickHandler(new SaveChangesClickHandler()));
    registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));
    getView().setTableListListener(new FormChangedHandler());
  }

  class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      setViewDto(event.getView());
    }
  }

  class SaveChangesClickHandler implements ClickHandler {

    private final Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

    SaveChangesClickHandler() {
      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        @Override
        public Collection<TableDto> getCollection() {
          return getView().getSelectedTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<TableDto>(tablesField, 1, "TableSelectionRequired"));
      validators.add(new MatchingTableEntitiesValidator(tablesField));
    }

    @Override
    public void onClick(ClickEvent event) {
      String errorMessageKey = validate();
      if(errorMessageKey != null) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorMessageKey).build());
        return;
      }
      viewDto.clearFromArray();
      viewDto.setFromArray(getSelectedTables());
      getEventBus().fireEvent(new ViewSaveRequiredEvent(viewDto));
    }

    String validate() {
      for(FieldValidator validator : validators) {
        String errorMessageKey = validator.validate();
        if(errorMessageKey != null) {
          return errorMessageKey;
        }
      }
      return null;
    }
  }

  class FormChangedHandler implements TableListListener {

    @Override
    public void onTableListUpdated() {
      List<TableDto> tables = getView().getSelectedTables();

      boolean changed = false;
      if(tables.size() == viewDto.getFromArray().length()) {
        List<String> fromTables = JsArrays.toList(viewDto.getFromArray());
        for(TableDto table : tables) {
          if(!fromTables.contains(table.getDatasourceName() + "." + table.getName())) {
            changed = true;
            break;
          }
        }
      } else {
        changed = true;
      }
      getView().saveChangesEnabled(changed);
      getEventBus().fireEvent(new ViewSavePendingEvent(changed));
    }
  }

  class ViewSavedHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      getView().saveChangesEnabled(false);
    }

  }

  private JsArrayString getSelectedTables() {
    JsArrayString tables = JavaScriptObject.createArray().cast();
    for(TableDto tableDto : getView().getSelectedTables()) {
      tables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    return tables;
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));
    getView().selectTables(viewDto.getFromArray());
  }
}