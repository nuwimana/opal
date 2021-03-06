/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.datasource.limesurvey.LimesurveyDatasource;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

public class DatabaseLimesurveyDatasourceFactory extends AbstractDatasourceFactory implements Disposable {

  private String databaseName;

  private String tablePrefix;

  // transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  @Autowired
  private transient DatabaseRegistry databaseRegistry;

  // empty public constructor because of XML serialization
  @SuppressWarnings("UnusedDeclaration")
  public DatabaseLimesurveyDatasourceFactory() {
  }

  /**
   * @param name
   * @param databaseName
   * @param tablePrefix
   * @param dataSourceRegistry
   */
  public DatabaseLimesurveyDatasourceFactory(String name, String databaseName, String tablePrefix,
      DatabaseRegistry databaseRegistry) {
    setName(name);
    Preconditions.checkArgument(name != null);
    Preconditions.checkArgument(databaseName != null);
    Preconditions.checkArgument(databaseRegistry != null);

    this.databaseName = databaseName;
    this.tablePrefix = tablePrefix;
    this.databaseRegistry = databaseRegistry;
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    return new LimesurveyDatasource(getName(), databaseRegistry.getDataSource(databaseName, getName()), tablePrefix);
  }

  @Override
  public void dispose() {
    databaseRegistry.unregister(databaseName, getName());
  }

}
