/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.unit;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;

/**
 *
 */
public interface FunctionalUnitService {

  boolean hasFunctionalUnit(String unitName);

  void removeFunctionalUnit(String unitName);

  @Nullable
  FunctionalUnit getFunctionalUnit(@Nullable String unitName);

  Set<FunctionalUnit> getFunctionalUnits();

  void addOrReplaceFunctionalUnit(FunctionalUnit functionalUnit);

  FileObject getUnitDirectory(String unitName) throws NoSuchFunctionalUnitException, FileSystemException;
}
