/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter.EnterKeyHandler;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter.ItemInputDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class ItemSelectorView extends ViewImpl implements ItemSelectorPresenter.Display {

  private Widget widget;

  private final Grid itemGrid;

  private ItemInputDisplay itemInputDisplay;

  public ItemSelectorView() {
    itemGrid = new Grid(0, 2);
    itemGrid.addStyleName("itemSelector");
  }

  @Override
  public void setItems(Iterable<String> items) {
    clear();
    for(String s : items) {
      addItem(s);
    }
  }

  @Override
  public void setItemInputDisplay(ItemInputDisplay itemInputDisplay) {
    this.itemInputDisplay = itemInputDisplay;

    // Add a row for the input widget.
    itemGrid.resize(1, 2);

    // Add the input widget in the first column.
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 0, itemInputDisplay.asWidget());

    // Put an "add" button in the second column.
    Image addWidget = createAddWidget();
    addWidget.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        addItemAndClear();
      }
    });
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 1, addWidget);

    // Put the Grid in a FlowPanel (so that it doesn't expand to fill its parent) and set that as the widget.
    FlowPanel container = new FlowPanel();
    container.add(itemGrid);
    widget = container;

    setEnterKeyHandler();
  }

  @Override
  public void addItem(final String item) {
    // Add a row for the new item.
    itemGrid.resize(itemGrid.getRowCount() + 1, 2);

    // Put the item in the new row's first column.
    itemGrid.setText(itemGrid.getRowCount() - 1, 0, item);

    // Put a "remove" button in the second column.
    Image removeWidget = createRemoveWidget();
    removeWidget.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        for(int i = 1; i < itemGrid.getRowCount(); i++) { // start from 1 to skip the input widget row
          if(itemGrid.getText(i, 0).equals(item)) {
            removeItem(i - 1);
            break;
          }
        }
      }
    });
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 1, removeWidget);
  }

  /**
   * Removes the item at the specified row.
   *
   * @param row row index of item to remove (zero-based)
   */
  @Override
  public void removeItem(int row) {
    itemGrid.removeRow(row + 1); // +1 to skip the input widget row
  }

  @Override
  public void clear() {
    while(itemGrid.getRowCount() > 1) { // 1 is for the input widget
      removeItem(0);
    }
  }

  @Override
  public int getItemCount() {
    return Math.max(0, itemGrid.getRowCount() - 1);
  }

  @Override
  public List<String> getItems() {
    List<String> items = new ArrayList<String>();
    for(int row = 1; row < itemGrid.getRowCount(); row++) { // start from 1 to skip the input widget row
      items.add(itemGrid.getText(row, 0));
    }

    return items;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  //
  // Methods
  //

  private Image createAddWidget() {
    Image addWidget = new Image("image/20/list-add.png");
    addWidget.addStyleName("clickable");
    addWidget.addStyleName("button");

    return addWidget;
  }

  private Image createRemoveWidget() {
    Image removeWidget = new Image("image/20/list-remove.png");
    removeWidget.addStyleName("clickable");
    removeWidget.addStyleName("button");

    return removeWidget;
  }

  private void addItemAndClear() {
    String item = itemInputDisplay.getItem();
    if(item.trim().length() != 0) {
      addItem(item);
      itemInputDisplay.clear();
    }
  }

  private void setEnterKeyHandler() {
    itemInputDisplay.setEnterKeyHandler(new EnterKeyHandler() {

      @Override
      public void enterKeyPressed() {
        addItemAndClear();
      }
    });
  }
}
