/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter.Display.ComparisonResult;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceCompareDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ComparedDatasourcesReportStepPresenter extends WidgetPresenter<ComparedDatasourcesReportStepPresenter.Display> {

  @Inject
  private UploadVariablesStepPresenter uploadVariablesStepPresenter;

  @Inject
  private ImportVariablesStepPresenter importVariablesStepPresenter;

  private String sourceDatasourceName;

  private String targetDatasourceName;

  private JsArray<TableCompareDto> comparedTables;

  @Inject
  public ComparedDatasourcesReportStepPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {

    enum ComparisonResult {
      CREATION, MODIFICATION, CONFLICT
    }

    HandlerRegistration addSaveClickHandler(ClickHandler handler);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    void addTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult);

    void clearDisplay();

    void setEnabledSaveButton(boolean enabled);

  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    initComparedDatasourceReport();
  }

  private void initComparedDatasourceReport() {
    getDisplay().clearDisplay();
    ResourceRequestBuilderFactory.<DatasourceCompareDto> newBuilder().forResource("/datasource/" + sourceDatasourceName + "/compare/" + targetDatasourceName).get().withCallback(new ResourceCallback<DatasourceCompareDto>() {

      @Override
      public void onResource(Response response, DatasourceCompareDto resource) {
        comparedTables = resource.getTableComparisonsArray();
        boolean conflictsExist = false;
        for(int i = 0; i < comparedTables.length(); i++) {
          TableCompareDto tableComparison = comparedTables.get(i);
          ComparisonResult comparisonResult = getTableComparisonResult(tableComparison);
          getDisplay().addTableCompareTab(tableComparison, comparisonResult);
          if(comparisonResult == ComparisonResult.CONFLICT) {
            conflictsExist = true;
          }
        }
        getDisplay().setEnabledSaveButton(!conflictsExist);
      }

    }).send();

  }

  private void addEventHandlers() {
    registerHandler(getDisplay().addSaveClickHandler(new SaveClickHandler()));
    registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  private ComparisonResult getTableComparisonResult(TableCompareDto tableComparison) {
    if(!tableComparison.hasWithTable()) {
      return ComparisonResult.CREATION;
    } else if(tableComparison.getConflictsArray() != null && tableComparison.getConflictsArray().length() > 0) {
      return ComparisonResult.CONFLICT;
    } else {
      return ComparisonResult.MODIFICATION;
    }
  }

  public void setSourceDatasourceName(String sourceDatasourceName) {
    this.sourceDatasourceName = sourceDatasourceName;
  }

  public void setTargetDatasourceName(String targetDatasourceName) {
    this.targetDatasourceName = targetDatasourceName;
  }

  class SaveClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      importVariablesStepPresenter.clearResourceRequests();

      List<VariableDto> variableToUpdateList = new ArrayList<VariableDto>();
      List<TableCompareDto> comparedTablesList = JsArrays.toList(comparedTables);
      for(TableCompareDto tableCompareDto : comparedTablesList) {
        variableToUpdateList.addAll(JsArrays.toList(tableCompareDto.getNewVariablesArray()));
        variableToUpdateList.addAll(JsArrays.toList(tableCompareDto.getExistingVariablesArray()));

        JsArray<VariableDto> variablesToStringify = getVariableToStringify(variableToUpdateList);

        importVariablesStepPresenter.addResourceRequest(tableCompareDto.getCompared().getName(), ResourceRequestBuilderFactory.newBuilder().post().forResource("/datasource/" + targetDatasourceName + "/table/" + tableCompareDto.getCompared().getName() + "/variables").accept("application/x-protobuf+json").withResourceBody(stringify(variablesToStringify)));
        variableToUpdateList.clear();
      }

      importVariablesStepPresenter.sendResourceRequests();

      eventBus.fireEvent(new WorkbenchChangeEvent(importVariablesStepPresenter));

    }

    private JsArray<VariableDto> getVariableToStringify(List<VariableDto> variableToUpdateList) {
      JsArray<VariableDto> variablesToStringify = JsArray.createArray().cast();
      for(VariableDto variableDto : variableToUpdateList) {
        variablesToStringify.push(variableDto);
      }
      return variablesToStringify;
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(uploadVariablesStepPresenter));
    }
  }

  public static native String stringify(JavaScriptObject obj) /*-{
                                                               return $wnd.JSON.stringify(obj);
                                                               }-*/;

}
