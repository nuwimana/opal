/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DataShieldConfigPresenter extends ItemAdministrationPresenter<DataShieldConfigPresenter.Display> {

  public interface Display extends WidgetDisplay {

    String getLevel();

    void setLevel(String level);

    void addEnvironmentDisplay(String name, WidgetDisplay display);

    void setPermissionsDisplay(AuthorizationPresenter.Display display);

    HasAuthorization getPermissionsAuthorizer();
  }

  private final DataShieldAdministrationPresenter aggregatePresenter;

  private final DataShieldAdministrationPresenter assignPresenter;

  private final AuthorizationPresenter authorizationPresenter;

  @Inject
  public DataShieldConfigPresenter(Display display, EventBus eventBus, Provider<DataShieldAdministrationPresenter> adminPresenterProvider, AuthorizationPresenter authorizationPresenter) {
    super(display, eventBus);
    aggregatePresenter = adminPresenterProvider.get();
    assignPresenter = adminPresenterProvider.get();
    aggregatePresenter.setEnvironment("aggregate");
    assignPresenter.setEnvironment("assign");
    this.authorizationPresenter = authorizationPresenter;
  }

  @Override
  public void refreshDisplay() {
    aggregatePresenter.refreshDisplay();
    assignPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    aggregatePresenter.revealDisplay();
    assignPresenter.revealDisplay();
  }

  @Override
  public String getName() {
    return "DataSHIELD";
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // TODO: Need to test both environments
    aggregatePresenter.authorize(authorizer);
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder().authorize(new CompositeAuthorizer(getDisplay().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  @Override
  protected void onBind() {
    aggregatePresenter.onBind();
    assignPresenter.onBind();
    authorizationPresenter.bind();

    getDisplay().setLevel("RESTRICTED");
    getDisplay().addEnvironmentDisplay("Aggregate", aggregatePresenter.getDisplay());
    getDisplay().addEnvironmentDisplay("Assign", assignPresenter.getDisplay());
    getDisplay().setPermissionsDisplay(authorizationPresenter.getDisplay());
  }

  @Override
  protected void onUnbind() {
    aggregatePresenter.onUnbind();
    assignPresenter.onUnbind();
    authorizationPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      authorizationPresenter.setAclRequest(AclRequest.newBuilder("Use", "/datashield/session", "*:GET/*"),//
      // AclRequest.newBuilder("Allow Users", "/authz/datashield", "*:GET/*")
      AclRequest.newBuilder("Administrate", "/datashield", "*:GET/*"));
      authorizationPresenter.refreshDisplay();
    }
  }

}