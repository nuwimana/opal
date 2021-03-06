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

import org.obiba.opal.web.model.client.magma.CategoryDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This event contains a {@link CategoryDto} and an {@link UpdateType} to indicate if the CategoryDto is to replace an
 * existing CategoryDto or to be added to the list of existing CategoryDtos. In the case of replacement, the original
 * CategoryDto is also provided and gives a way of locating the original category to be replaced.
 */
public class CategoryUpdateEvent extends GwtEvent<CategoryUpdateEvent.Handler> {

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final CategoryDto category;

  private final CategoryDto originalCategory;

  private final UpdateType updateType;

  public CategoryUpdateEvent(CategoryDto category, CategoryDto originalCategory, UpdateType updateType) {
    this.category = category;
    this.originalCategory = originalCategory;
    this.updateType = updateType;
  }

  public CategoryUpdateEvent(CategoryDto category, UpdateType updateType) {
    this(category, null, updateType);
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCategoryUpdate(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public CategoryDto getCategory() {
    return category;
  }

  public CategoryDto getOriginalCategory() {
    return originalCategory;
  }

  public UpdateType getUpdateType() {
    return updateType;
  }

  public interface Handler extends EventHandler {
    void onCategoryUpdate(CategoryUpdateEvent event);
  }
}
