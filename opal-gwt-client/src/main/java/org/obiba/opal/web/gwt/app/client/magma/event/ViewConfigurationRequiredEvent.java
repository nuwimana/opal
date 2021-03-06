/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.event;

import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * An event signalling that an Opal view needs to be configured.
 */
public class ViewConfigurationRequiredEvent extends GwtEvent<ViewConfigurationRequiredEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<Handler>();

  //
  // Instance Variables
  //

  private final ViewDto view;

  private VariableDto variable;

  //
  // Constructors
  //

  public ViewConfigurationRequiredEvent(ViewDto view) {
    this.view = view;
  }

  public ViewConfigurationRequiredEvent(ViewDto view, VariableDto variable) {
    this(view);
    this.variable = variable;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onViewConfigurationRequired(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE;
  }

  public ViewDto getView() {
    return view;
  }

  public VariableDto getVariable() {
    return variable;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onViewConfigurationRequired(ViewConfigurationRequiredEvent event);
  }
}
