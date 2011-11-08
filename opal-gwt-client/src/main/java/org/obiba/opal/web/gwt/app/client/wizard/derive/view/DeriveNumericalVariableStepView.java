/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DeriveNumericalVariableStepView extends Composite implements DeriveNumericalVariableStepPresenter.Display {

  @UiTemplate("DeriveNumericalVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveNumericalVariableStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  @UiField
  WizardStep methodStep;

  @UiField
  WizardStep mapStep;

  //
  // Constructors
  //

  public DeriveNumericalVariableStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public DefaultWizardStepController.Builder getMethodStepController() {
    return DefaultWizardStepController.Builder.create(methodStep).title("Numerical: method");
  }

  @Override
  public DefaultWizardStepController.Builder getMapStepController() {
    return DefaultWizardStepController.Builder.create(mapStep).title("Numerical: map");
  }

  //
  // Widget Display methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

}