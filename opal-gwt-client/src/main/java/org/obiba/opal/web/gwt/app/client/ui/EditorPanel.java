/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class EditorPanel extends FlowPanel {

  private final Button edit;

  private final Button save;

  private final Button cancel;

  private final DeckPanel deck;

  private Handler handler;

  private Object editWidget;

  public EditorPanel() {
    // TODO translate
    edit = new Button("Edit");
    save = new Button("Save");
    cancel = new Button("Cancel");
    addControls();
    super.add(deck = new DeckPanel());
  }

  private void addControls() {
    FlowPanel controls = new FlowPanel();
    controls.addStyleName("bottom-margin");
    edit.setIcon(IconType.EDIT);
    edit.setType(ButtonType.INFO);
    edit.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showEditor(true);
        if (handler != null) handler.onEdit();
      }
    });
    controls.add(edit);

    save.setType(ButtonType.PRIMARY);
    save.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showEditor(false);
        if (handler != null) handler.onSave();
      }
    });
    save.setVisible(false);
    controls.add(save);

    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showEditor(false);
        if (handler != null) handler.onCancel();
      }
    });
    cancel.addStyleName("small-indent");
    cancel.setVisible(false);
    controls.add(cancel);

    super.add(controls);
  }

  @Override
  public void add(IsWidget child) {
    deck.add(child);
  }

  @Override
  public void add(Widget w) {
    deck.add(w);
  }

  /**
   * Set the control buttons size.
   * @param size
   */
  public void setSize(ButtonSize size) {
    edit.setSize(size);
    save.setSize(size);
    cancel.setSize(size);
  }

  public void setHandler(Handler handler) {
    this.handler = handler;
  }

  public void showEditor(boolean visible) {
    edit.setVisible(!visible);
    save.setVisible(visible);
    cancel.setVisible(visible);
    deck.showWidget(visible ? 1 : 0);
  }

  public Widget getEditWidget() {
    return edit;
  }

  public interface Handler {

    void onEdit();

    void onSave();

    void onCancel();
  }
}