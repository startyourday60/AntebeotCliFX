package ru.antebeotcli.antebeotcli_;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static ru.antebeotcli.antebeotcli_.AppRun.MainJsonGetter;
import static ru.antebeotcli.antebeotcli_.PriceChartController.getAllowCoins;
import static ru.antebeotcli.antebeotcli_.RegController.getErrorAlert;
import static ru.antebeotcli.antebeotcli_.TemplatesLoader.captchaLoad;
import static ru.antebeotcli.antebeotcli_.TemplatesLoader.getSelfBalances;

public class ProfileChartControler {
    @FXML
    VBox mainPane;
    @FXML
    ImageView captchaImage;
    @FXML
    ListView<String> CryptoTreeView;
    @FXML
    TextField InputAddressArea, AddressSendMoneyField,
            CountSendMoneyField, PasswordSendMoneyField, LoginSendMoneyField, CaptchaTextField;
    @FXML
    Text BalanceText;
    @FXML
    protected void initialize()
    {
        // var getter = MainJsonGetter;
        try {
            var allowCoins = getAllowCoins();
            //System.out.println(allowCoins);
            for(var coin : allowCoins)
            {
                CryptoTreeView.getItems().add(coin);
            }
            TemplatesLoader.captchaLoad(this.captchaImage);
        } catch(Exception err)
        {
            getErrorAlert(err.toString()).show();
        }
    }
    @FXML
    public void OpenPriceChart() {
        TemplatesLoader.load("pricechart.fxml", mainPane);
    }

    public void showHelpAlert() {
        TemplatesLoader.showHelpAlert();
    }

    @FXML
    protected void exitProgram() {
        System.exit(0);
    }

    @FXML
    public void OpenProfile()  {
        TemplatesLoader.load("profile.fxml", mainPane);
    }

    private void getNewInputAddres() {
        String selected = CryptoTreeView.getSelectionModel().getSelectedItem();
       // System.out.println("Get new input address for " + selected);
        JSONObject ownInput = null;
        try {
            ownInput = MainJsonGetter.getUriJSON("/user?w=genAddress&cryptocoin=" + selected.trim());
            if (!ownInput.getBoolean("result")){
                getErrorAlert(ownInput.getString("reason")).show();
            } else {
                getInputAddress();
            }//
        } catch (Exception e) {
            getErrorAlert(e.toString()).show();
        }
    }
    @FXML
    public void ChangeInputAddress() {
        var uLang = new i18n();
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(uLang.getString("AddressChanging"));
            alert.setHeaderText(uLang.getString("ConfirmChangingOfAddress_0"));
            alert.setContentText(uLang.getString("ConfirmChangingOfAddress_1"));
            alert.showAndWait().ifPresent(rs -> {
                if (rs == ButtonType.OK) {
                    try {
                        getNewInputAddres();
                    } catch (Exception e) {
                        getErrorAlert(e.toString()).show();
                    }
                }
            });
        } catch(Exception err)
        {
            getErrorAlert(err.toString()).show();
        }
    }
    protected void getInputAddress()
    {
        String selected = CryptoTreeView.getSelectionModel().getSelectedItem();
        //System.out.println("Get input address for " + selected);
        try {
            var ownInput = new String(MainJsonGetter.getRaw("/user/?w=getowninput&cryptocoin=" + selected.trim())).replace("\"","").trim();
            // System.out.println("Own input is: " + ownInput);
            if (ownInput == null || ownInput.equals("null"))
            {
                ChangeInputAddress();
                getInputAddress();
            }
            InputAddressArea.setText(ownInput);
        } catch(Exception err)
        {
            getErrorAlert(err.toString()).show();
        }
    }

    @FXML
    public void SelectInputClick() {
        getInputAddress();
        try {
            var uLang = new i18n();
            var balances = getSelfBalances();
            //System.out.println(balances);
            var cryptocoin = CryptoTreeView.getSelectionModel().getSelectedItem();
            var balanceForAddress = balances.getJSONObject(cryptocoin);
            var setBalance = String.format(uLang.getString("BalanceValue"), cryptocoin, balanceForAddress.getString("balance"));
            BalanceText.setText(setBalance);
        } catch(Exception err)
        {
            getErrorAlert(err.toString()).show();
        }
    }

    @FXML
    public void SendMoney() {
        // var uLang = new i18n();

        try {
            var address = AddressSendMoneyField.getText();
            var count = new BigDecimal(CountSendMoneyField.getText());
            var login = LoginSendMoneyField.getText();
            var pass = PasswordSendMoneyField.getText();
            var captcha = CaptchaTextField.getText();
            var cryptocoin = CryptoTreeView.getSelectionModel().getSelectedItem();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Отправка средств");
            alert.setHeaderText("Подтвердите отправку средств");
            alert.setContentText(String.format("Подтвердите отправку средств на адрес %s в кол-ве %s для криптовалюты %s", address, count, cryptocoin));
            alert.showAndWait().ifPresent(rs -> {
                if (rs == ButtonType.OK) {
                    try {
                        var link = String.format("/api/?w=output&acc=%s&pass=%s&oAdr=%s&cMoney=%s&coinname=%s&captchaText=%s",
                                login, pass, address, count, cryptocoin, captcha);
                        var res = MainJsonGetter.getUriJSON(link);
                        captchaLoad(this.captchaImage);
                        var result = res.getBoolean("result");
                        var reason = res.getString("reason");
                        if (result == false)
                        {
                            getErrorAlert(reason).show();
                            return;
                        }
                        var a = new Alert(Alert.AlertType.INFORMATION);
                        a.setContentText(reason);
                        a.show();
                    } catch (Exception e) {
                        getErrorAlert(e.toString()).show();
                    }
                }
            });
        } catch(Exception err)
        {
            getErrorAlert(err.toString()).show();
        }

    }
}
