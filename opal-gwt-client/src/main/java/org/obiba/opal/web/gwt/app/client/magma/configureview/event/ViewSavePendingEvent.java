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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Indicates that the form has changed, but it hasn't been saved yet.
 */
public class ViewSavePendingEvent extends GwtEvent<ViewSavePendingEvent.Handler> {

  private static final Type<Handler> TYPE = new Type<Handler>();

  private boolean pending = true;

  public ViewSavePendingEvent() {
  }

  public ViewSavePendingEvent(boolean pending) {
    this.pending = pending;
  }

  public boolean isPending() {
    return pending;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSavePending(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public interface Handler extends EventHandler {
    void onSavePending(ViewSavePendingEvent event);
  }

}