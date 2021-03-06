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

import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class UnhandledResponseNotificationPresenter
    extends PresenterWidget<UnhandledResponseNotificationPresenter.Display> {

  public interface Display extends PopupView {

    HasClickHandlers getOkay();

    Label getErrorMessage();
  }

  @Inject
  public UnhandledResponseNotificationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {

    getView().getOkay().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hide();
      }
    });
  }

  public UnhandledResponseNotificationPresenter withResponseEvent(UnhandledResponseEvent event) {

    String message = event.getResponse().getStatusText() + " (" + event.getResponse().getStatusCode() + ")";

    if(!event.getResponse().getText().isEmpty()) {
      message += ": " + event.getResponse().getText();
    }

    getView().getErrorMessage().setText(message);

    return this;
  }

}

