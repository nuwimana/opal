/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class BreadCrumbTabLayout extends AbstractTabLayout {

  private String divider;

  public BreadCrumbTabLayout() {
    this("/");
  }

  public BreadCrumbTabLayout(String divider) {
    super("breadcrumb");
    addStyleName("breadcrumb-tabs");
    this.divider = divider;

    // remove all tabs after the one selected
    addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        int idx = event.getSelectedItem().intValue();
        while(getWidgetCount() > idx + 1) {
          remove(getWidgetCount() - 1);
        }
      }
    });
  }

  @Override
  protected ListItem newListItem(Widget item, int beforeIndex) {
    ListItem li;
    if(beforeIndex > 0) {
      InlineLabel div = new InlineLabel(divider);
      div.setStyleName("divider");
      li = super.newListItem(div, beforeIndex);
      li.add(item);
    } else {
      li = super.newListItem(item, beforeIndex);
    }
    return li;
  }

  public void addAndSelect(Widget w, String text) {
    add(w, text);
    selectTab(w);
  }

}
