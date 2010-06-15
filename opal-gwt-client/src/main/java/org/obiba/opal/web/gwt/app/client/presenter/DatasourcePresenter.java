/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class DatasourcePresenter extends WidgetPresenter<DatasourcePresenter.Display> {

  public interface Display extends WidgetDisplay {

    void renderRows(JsArray<TableDto> rows);

    Label getDatasourceNameLabel();

    Label getVariableCountLabel();

    HasClickHandlers getSpreadsheetIcon();

    HasFieldUpdater<TableDto, String> getTableNameColumn();

    FlowPanel getSpreadsheetDownloadPanel();

  }

  private String datasource;

  @Inject
  public DatasourcePresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    super.registerHandler(eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionChangeEvent.Handler() {
      @Override
      public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
        event.getSelection().getUserObject();
        if(event.getSelection().getParentItem() == null) {
          datasource = ((DatasourceDto) event.getSelection().getUserObject()).getName();
          getDisplay().getDatasourceNameLabel().setText(datasource);
          updateTable(datasource);
        }
      }
    }));

    super.registerHandler(getDisplay().getSpreadsheetIcon().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        downloadMetadata(datasource);
      }
    }));

    super.getDisplay().getTableNameColumn().setFieldUpdater(new FieldUpdater<TableDto, String>() {

      @Override
      public void update(int index, TableDto tableDto, String value) {

        eventBus.fireEvent(new TableSelectionChangeEvent(tableDto));

      }

    });
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

  private void updateTable(final String datasource) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasource + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
      @Override
      public void onResource(Response response, JsArray<TableDto> resource) {
        getDisplay().getVariableCountLabel().setText(Integer.toString(resource.length()));
        getDisplay().renderRows(resource);
      }

    }).send();
  }

  private void downloadMetadata(String datasource) {
    String downloadUrl = GWT.getHostPageBaseURL().replace("org.obiba.opal.web.gwt.app.GwtApp/", "") + "ws/datasource/" + datasource + "/dictionary/excel";
    Frame frame = new Frame(downloadUrl);
    frame.setVisible(false);
    getDisplay().getSpreadsheetDownloadPanel().add(frame);
  }

}
