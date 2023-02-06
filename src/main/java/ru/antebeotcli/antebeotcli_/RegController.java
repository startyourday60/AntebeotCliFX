package ru.antebeotcli.antebeotcli_;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import ru.antebeotcli.antebeotcli_.AnteBeotWeb.JsonGetter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import static ru.antebeotcli.antebeotcli_.AppRun.MainJsonGetter;


public class RegController {
    private boolean isEnterPageNow = false;
    @FXML
    Text TopText;
    @FXML
    Text loginLabel;
    @FXML
    Text confirmPassLabel;
    @FXML
    Text passLabel;
    @FXML
    Text captchaLabel;
    @FXML
    ImageView ImageCaptcha;
    @FXML
    Pane mainPane;
    @FXML
    private TextArea LoginArea;
    @FXML
    private TextArea PasswordArea;
    @FXML
    private TextArea ConfirmPasswordArea;
    @FXML
    private TextArea CaptchaArea;
    @FXML
    private Button RegButton;
    @FXML
    private Button EnterButton;
    private Alert getConfirmRegistration(String workname, String workpass, String captchaText)
    {
        var uLang = new i18n();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(uLang.getString("REGISTRATION"));
        alert.setHeaderText(uLang.getString("CONFIRM_REGISTRATION_0"));
        alert.setContentText(uLang.getString("confirmRegistration"));
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                try {
                    var getter = MainJsonGetter;
                    var req = String.format("/registration/?workname=%s&workpass=%s&workpass2=%s&captchaText=%s", workname, workpass, workpass, captchaText); // %2s?
                    var jansw = getter.getUriJSON(req);
                    var res = jansw.getBoolean("result");
                    if (res != true)
                    {

                        var reason = jansw.getString("reason");
                        var fixed = JsonGetter.fixEncoding(reason);
                        getErrorAlert( fixed ).show();
                    }else {
                        System.out.println("registered");
                        // successfully
                        // TODO: save cookie
                        getter.saveCookies(AuthControler.cookieFileName);
                        TemplatesLoader.load("profile.fxml", mainPane);
                    }
                } catch (Exception e) {
                    getErrorAlert(e.toString()).show();
                    // throw new RuntimeException(e);
                }
                System.out.println("Pressed OK.");
            }

        });
        return alert;
    }



    @FXML
    protected void onRegButtonClick() {
        var uLang = new i18n();
        var Login = this.LoginArea.getText();
        var Password = this.PasswordArea.getText();
        var ConfirmPassword = this.ConfirmPasswordArea.getText();
        var Captcha = this.CaptchaArea.getText();
        if (!Password.equals(ConfirmPassword))
        {
            getErrorAlert(uLang.getString("PasswordsNotEquals")).show();
        }
        else
        {
            getConfirmRegistration(Login, Password, Captcha).show();
        }

    }

    @FXML
    protected void onEnterButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppRun.class.getResource("authorization.fxml"));
        //fxmlLoader.setRoot(this);
        //fxmlLoader.load();
        mainPane.getChildren().setAll((Node) fxmlLoader.load());

    }
    @FXML
    protected void initialize()
    {
        var uLang = new i18n();
        TopText.setText(uLang.getString("REGISTRATION"));
        loginLabel.setText(uLang.getString("LOGIN"));
        passLabel.setText(uLang.getString("PASSWORD"));
        confirmPassLabel.setText(uLang.getString("CONFIRMPASSWORD"));
        captchaLabel.setText(uLang.getString("CAPTCHA"));
        EnterButton.setText(uLang.getString("ENTER"));
        RegButton.setText(uLang.getString("doRegistration"));
        TemplatesLoader.captchaLoad(ImageCaptcha);
    }
    public static Alert getErrorAlert(String content)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("");
        alert.setContentText(content);
        return alert;
    }

    @FXML void reloadCaptcha()
    {
        TemplatesLoader.captchaLoad(this.ImageCaptcha);
    }
}