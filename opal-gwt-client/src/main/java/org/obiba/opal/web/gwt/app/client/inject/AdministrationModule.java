/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.view.ConfigurationView;
import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.view.DatabaseAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.database.view.DatabaseView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageCreatePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackagePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldConfigView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldMethodView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageCreateView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageView;
import org.obiba.opal.web.gwt.app.client.administration.fs.presenter.FilesAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.fs.view.FilesAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexConfigurationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexView;
import org.obiba.opal.web.gwt.app.client.administration.jvm.presenter.JVMPresenter;
import org.obiba.opal.web.gwt.app.client.administration.jvm.view.JVMView;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.view.RAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.server.presenter.ServerPresenter;
import org.obiba.opal.web.gwt.app.client.administration.server.view.ServerView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.AddTaxonomyModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.AddVocabularyModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomiesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.AddTaxonomyModalView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.AddVocabularyModalView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomiesView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomyView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.VocabularyView;
import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserPresenter;
import org.obiba.opal.web.gwt.app.client.administration.user.view.UserAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.user.view.UserView;
import org.obiba.opal.web.gwt.app.client.administration.view.AdministrationView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class AdministrationModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(AdministrationPresenter.class, AdministrationPresenter.Display.class, AdministrationView.class,
        AdministrationPresenter.Proxy.class);
    bindPresenter(DataShieldConfigPresenter.class, DataShieldConfigPresenter.Display.class, DataShieldConfigView.class,
        DataShieldConfigPresenter.Proxy.class);
    bindPresenter(RAdministrationPresenter.class, RAdministrationPresenter.Display.class, RAdministrationView.class,
        RAdministrationPresenter.Proxy.class);

    bindPresenter(DatabaseAdministrationPresenter.class, DatabaseAdministrationPresenter.Display.class,
        DatabaseAdministrationView.class, DatabaseAdministrationPresenter.Proxy.class);
    bindPresenterWidget(DatabasePresenter.class, DatabasePresenter.Display.class, DatabaseView.class);

    bindPresenter(IndexAdministrationPresenter.class, IndexAdministrationPresenter.Display.class,
        IndexAdministrationView.class, IndexAdministrationPresenter.Proxy.class);
    bindPresenterWidget(IndexPresenter.class, IndexPresenter.Display.class, IndexView.class);
    bindPresenterWidget(IndexConfigurationPresenter.class, IndexConfigurationPresenter.Display.class,
        IndexConfigurationView.class);

    bindPresenterWidget(DataShieldPackageAdministrationPresenter.class,
        DataShieldPackageAdministrationPresenter.Display.class, DataShieldPackageAdministrationView.class);
    bindPresenterWidget(DataShieldAdministrationPresenter.class, DataShieldAdministrationPresenter.Display.class,
        DataShieldAdministrationView.class);
    bindPresenterWidget(DataShieldPackageCreatePresenter.class, DataShieldPackageCreatePresenter.Display.class,
        DataShieldPackageCreateView.class);
    bindPresenterWidget(DataShieldPackagePresenter.class, DataShieldPackagePresenter.Display.class,
        DataShieldPackageView.class);
    bindPresenterWidget(DataShieldMethodPresenter.class, DataShieldMethodPresenter.Display.class,
        DataShieldMethodView.class);

    // User and Groups
    bindPresenter(UserAdministrationPresenter.class, UserAdministrationPresenter.Display.class,
        UserAdministrationView.class, UserAdministrationPresenter.Proxy.class);
    bindPresenterWidget(UserPresenter.class, UserPresenter.Display.class, UserView.class);

    // Java virtual machine
    bindPresenter(JVMPresenter.class, JVMPresenter.Display.class, JVMView.class, JVMPresenter.Proxy.class);

    // System config
    bindPresenter(ServerPresenter.class, ServerPresenter.Display.class, ServerView.class, ServerPresenter.Proxy.class);

    bindPresenter(FilesAdministrationPresenter.class, FilesAdministrationPresenter.Display.class,
        FilesAdministrationView.class, FilesAdministrationPresenter.Proxy.class);
    
    bindPresenter(ConfigurationPresenter.class, ConfigurationPresenter.Display.class, ConfigurationView.class,
        ConfigurationPresenter.Proxy.class);

    // Taxonomies
    bindPresenter(TaxonomiesPresenter.class, TaxonomiesPresenter.Display.class, TaxonomiesView.class,
        TaxonomiesPresenter.Proxy.class);
    bindPresenterWidget(AddTaxonomyModalPresenter.class, AddTaxonomyModalPresenter.Display.class,
        AddTaxonomyModalView.class);

    //Taxonomy
    bindPresenter(TaxonomyPresenter.class, TaxonomyPresenter.Display.class, TaxonomyView.class,
        TaxonomyPresenter.Proxy.class);
    //Vocabulary
    bindPresenter(VocabularyPresenter.class, VocabularyPresenter.Display.class, VocabularyView.class,
        VocabularyPresenter.Proxy.class);
    bindPresenterWidget(AddVocabularyModalPresenter.class, AddVocabularyModalPresenter.Display.class,
        AddVocabularyModalView.class);

  }

}
