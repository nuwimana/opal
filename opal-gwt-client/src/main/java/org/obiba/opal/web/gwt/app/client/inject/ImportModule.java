/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.ArchiveStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.CsvFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DatasourceValuesStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.LimesurveyStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.NoFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.RestStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.SpssFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.UnitSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.XmlFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.ArchiveStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.CsvFormatStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.DataImportView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.DatasourceValuesStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.DestinationSelectionStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.LimesurveyStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.NoFormatStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.RestStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.SpssFormatStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.UnitSelectionStepView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.view.XmlFormatStepView;

/**
 * Bind concrete implementations to interfaces within the import wizard.
 */
@SuppressWarnings("OverlyCoupledClass")
public class ImportModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(DataImportPresenter.class, DataImportPresenter.Display.class, DataImportView.class,
        DataImportPresenter.Wizard.class);
    bindPresenterWidget(DestinationSelectionStepPresenter.class, DestinationSelectionStepPresenter.Display.class,
        DestinationSelectionStepView.class);
    bindPresenterWidget(UnitSelectionStepPresenter.class, UnitSelectionStepPresenter.Display.class,
        UnitSelectionStepView.class);
    bindPresenterWidget(DatasourceValuesStepPresenter.class, DatasourceValuesStepPresenter.Display.class,
        DatasourceValuesStepView.class);
    bindPresenterWidget(ArchiveStepPresenter.class, ArchiveStepPresenter.Display.class, ArchiveStepView.class);

    bind(CsvFormatStepPresenter.Display.class).to(CsvFormatStepView.class);
    bind(XmlFormatStepPresenter.Display.class).to(XmlFormatStepView.class);
    bind(SpssFormatStepPresenter.Display.class).to(SpssFormatStepView.class);
    bind(LimesurveyStepPresenter.Display.class).to(LimesurveyStepView.class);
    bind(RestStepPresenter.Display.class).to(RestStepView.class);
    bind(NoFormatStepPresenter.Display.class).to(NoFormatStepView.class);
  }
}
