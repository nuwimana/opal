/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.event;

import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class FileDeletedEvent extends GwtEvent<FileDeletedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onFileDeleted(FileDeletedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final FileDto file;

  public FileDeletedEvent(FileDto file) {
    this.file = file;
  }

  public FileDto getFile() {
    return file;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onFileDeleted(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
