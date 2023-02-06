package ru.antebeotcli.antebeotcli_;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static ru.antebeotcli.antebeotcli_.AppRun.MainJsonGetter;
import static ru.antebeotcli.antebeotcli_.RegController.getErrorAlert;

public class TemplatesLoader {
    public static void captchaLoad(ImageView ImageCaptcha)
    {
        System.out.println("captcha reload");
        var getter = MainJsonGetter;
        try {
            var captcha = getter.getRaw("/captcha?w=get");
            //System.out.println("Raw captcha: "+new String(captcha));
            Image img = new Image(new ByteArrayInputStream(captcha));
            ImageCaptcha.setImage(img);
        }catch (Exception e)
        {
            getErrorAlert(e.toString()).show();
        }
    }
    public static JSONObject getSelfBalances() throws ExecutionException, InterruptedException, TimeoutException {
        var t = MainJsonGetter.getUriJSON("/user").getJSONObject("Balances");
        return t;
    }
    public static void showHelpAlert()
    {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("about");
        alert.setContentText("2023");
        alert.setHeaderText("AnteBeot - новое средство для обмена криптовалюты в криптовалюту");
        alert.show();
    }
    public static void load(String name, Pane mainPane)
    {
        if (!name.contains(".fxml")) name = name + ".fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(AppRun.class.getResource(name));
        //fxmlLoader.setRoot(this);
        //fxmlLoader.load();
        try {
            mainPane.getChildren().setAll((Node) fxmlLoader.load());
        } catch(Exception err)
        {
            getErrorAlert(err.toString()).show();
        }
    }
}
