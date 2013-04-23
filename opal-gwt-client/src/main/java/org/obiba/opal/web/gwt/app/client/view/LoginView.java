package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class LoginView extends ViewImpl implements LoginPresenter.Display {
  @UiTemplate("LoginView.ui.xml")
  interface LoginViewUiBinder extends UiBinder<Widget, LoginView> {}

  private static final LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);

  private final Widget panel;

  @UiField
  Label errorMessage;

  @UiField
  TextBox userName;

  @UiField
  PasswordTextBox password;

  @UiField
  Button login;

  public LoginView() {
    panel = uiBinder.createAndBindUi(this);
    errorMessage.setVisible(false);
    userName.setFocus(true);
  }

  @Override
  public HasClickHandlers getSignIn() {
    return login;
  }

  @Override
  public Widget asWidget() {
    return panel;
  }

  @Override
  public HasValue<String> getPassword() {
    return password;
  }

  @Override
  public HasValue<String> getUserName() {
    return userName;
  }

  @Override
  public void focusOnUserName() {
    userName.setFocus(true);
  }

  @Override
  public void showErrorMessageAndClearPassword() {
    errorMessage.setVisible(true);
    new Animation() {

      @Override
      protected void onUpdate(double progress) {
        errorMessage.getElement().setAttribute("style", "opacity:" + progress);
      }

    }.run(200);
    clearPassword();
  }

  @Override
  public void clear() {
    errorMessage.setVisible(false);
    clearPassword();
  }

  @Override
  public HasKeyUpHandlers getPasswordTextBox() {
    return password;
  }

  @Override
  public HasKeyUpHandlers getUserNameTextBox() {
    return userName;
  }

  private void clearPassword() {
    getPassword().setValue("");
  }

}
