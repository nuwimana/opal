/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.configureview.event;

import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Sent once the view has be successfully persisted.
 */
public class ViewSavedEvent extends GwtEvent<ViewSavedEvent.Handler> {

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final ViewDto view;

  public ViewSavedEvent(ViewDto view) {
    this.view = view;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onViewSaved(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public ViewDto getView() {
    return view;
  }

  public interface Handler extends EventHandler {
    void onViewSaved(ViewSavedEvent event);
  }
}