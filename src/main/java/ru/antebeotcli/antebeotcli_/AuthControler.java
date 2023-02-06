package ru.antebeotcli.antebeotcli_;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import ru.antebeotcli.antebeotcli_.AnteBeotWeb.JsonGetter;

import java.io.File;
import java.io.IOException;

import static ru.antebeotcli.antebeotcli_.AppRun.MainJsonGetter;
import static ru.antebeotcli.antebeotcli_.TemplatesLoader.captchaLoad;
import static ru.antebeotcli.antebeotcli_.RegController.getErrorAlert;

public class AuthControler {
    final static public String  cookieFileName = "cookies.dat";
    @FXML
    ImageView ImageCaptcha;
    @FXML
    Pane mainPane;
    @FXML
    private TextArea _2faArea, CaptchaArea, PasswordArea, LoginArea;
    @FXML
    private Text TopText, loginLabel, PassLabel, captchaLabel, _2faLabel;
    @FXML
    private Button EnterButton, RegButton;
    @FXML
    protected void onRegButtonClick() throws IOException {
        TemplatesLoader.load("registration.fxml", mainPane);
    }
    private void loadProfile() throws IOException
    {
        TemplatesLoader.load("profile.fxml", mainPane);
    }
    @FXML
    protected void initialize()
    {
        if (new File(cookieFileName).exists())
        {
            var getter = MainJsonGetter;
            try {
                getter.loadCookies(cookieFileName);
                if (getter.updateSession()) {
                    System.out.println("Load profile");
                    loadProfile();
                } else {
                    new File(cookieFileName).delete();
                    getErrorAlert("Session outdated").show();
                }
            } catch(Exception e)
            {
                new File(cookieFileName).delete();
                getErrorAlert(e.toString()).show();
            }
        }
        var uLang = new i18n();
        TopText.setText(uLang.getString("AUTHORIZATION"));
        loginLabel.setText(uLang.getString("LOGIN"));
        PassLabel.setText(uLang.getString("PASSWORD"));
        _2faLabel.setText(uLang.getString("TWOFA"));
        captchaLabel.setText(uLang.getString("CAPTCHA"));
        EnterButton.setText(uLang.getString("ENTER"));
        RegButton.setText(uLang.getString("doRegistration"));


        captchaLoad(ImageCaptcha);
    }
    @FXML
    protected void onEnterButtonClick() throws IOException {
        //System.out.println("on enter cliecked");
        var Login = this.LoginArea.getText();
        var Password = this.PasswordArea.getText();
        var Captcha = this.CaptchaArea.getText();
        var code = this._2faArea.getText();
        try {
            var getter = MainJsonGetter;
            var req = String.format("/signin/?workname=%s&workpass=%s&code=%s&captchaText=%s", Login, Password, code, Captcha); // %2s?
            var jansw = getter.getUriJSON(req);
            var res = jansw.getBoolean("result");
            if (res != true) {
                var reason = jansw.getString("reason");
                var fixed = JsonGetter.fixEncoding(reason);
                RegController.getErrorAlert( fixed ).show();
            } else {
                // succesfully;
                //System.out.println("Succesfully");
                getter.saveCookies(cookieFileName);
                loadProfile();
            }
        } catch(Exception err)
        {
            RegController.getErrorAlert(err.toString()).show();
        }
    }
    @FXML void reloadCaptcha()
    {
        captchaLoad(ImageCaptcha);
    }
}
