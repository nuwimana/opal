/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.DatasourceCopier;

/**
 * Service for export operations. Export allows Magma tables to be copied to an existing Datasource or to an Excel file.
 */
public interface ExportService {

  /**
   * Get a new datasource builder, with default logging facilities.
   *
   * @param destinationDatasource
   * @return
   */
  DatasourceCopier.Builder newCopier(Datasource destinationDatasource);

  /**
   * Export tables to an existing Datasource. This export operation will be logged.
   *
   * @param unit the functional unit to which the tables are being exported (for key separation)
   * @param sourceTableNames tables to export.
   * @param destinationDatasourceName tables will be copied to this existing Datasource.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws NoSuchFunctionalUnitException if a unit has been specified that does not exist
   * @throws NoSuchDatasourceException if the destinationDatasourceName does not exist.
   * @throws NoSuchValueTableException if a sourceTableName does not exist.
   * @throws ExportException if the sourceTableNames are not unique or if the datasource of a sourceTableName matches
   * the destinationDatasource.
   * @throws InterruptedException if the current thread was interrupted
   */
  void exportTablesToDatasource(@Nullable String unit, @Nonnull List<String> sourceTableNames,
      @Nonnull String destinationDatasourceName, boolean incremental) throws InterruptedException;

  /**
   * Export tables to an existing Datasource using the provided {@link DatasourceCopier}. If logging is required ensure
   * that the {@code DatasourceCopier} is configured with an appropriate logger.
   *
   * @param unit the functional unit to which the tables are being exported (for key separation)
   * @param sourceTableNames tables to export.
   * @param destinationDatasourceName tables will be copied to this existing Datasource.
   * @param datasourceCopier copier used to perform the copy.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws NoSuchFunctionalUnitException if a unit has been specified that does not exist
   * @throws NoSuchDatasourceException if the destinationDatasourceName does not exist.
   * @throws NoSuchValueTableException if a sourceTableName does not exist.
   * @throws ExportException if the sourceTableNames are not unique or if the datasource of a sourceTableName matches
   * the destinationDatasource.
   * @throws InterruptedException if the current thread was interrupted
   */
  void exportTablesToDatasource(@Nullable String unit, @Nonnull List<String> sourceTableNames,
      @Nonnull String destinationDatasourceName, DatasourceCopier.Builder datasourceCopier, boolean incremental)
      throws InterruptedException;

  /**
   * Export tables to the provided {@link Datasource} using the provided {@link DatasourceCopier}. If logging is
   * required ensure that the {@code DatasourceCopier} is configured with an appropriate logger. It is the
   * responsibility of the caller to remove the {@code destinationDatasource} from {@code Magma}.
   *
   * @param unit the functional unit to which the tables are being exported (for key separation)
   * @param sourceTables tables to export.
   * @param destinationDatasource tables will be copied to this existing Datasource.
   * @param datasourceCopier copier used to perform the copy.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws NoSuchFunctionalUnitException if a unit has been specified that does not exist
   * @throws ExportException if the datasource of a sourceTable matches the destinationDatasource.
   * @throws InterruptedException if the current thread was interrupted
   */
  void exportTablesToDatasource(@Nullable String unit, @Nonnull Set<ValueTable> sourceTables,
      @Nonnull Datasource destinationDatasource, @Nonnull DatasourceCopier.Builder datasourceCopier,
      boolean incremental) throws InterruptedException;

}
