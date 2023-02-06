package ru.antebeotcli.antebeotcli_;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static ru.antebeotcli.antebeotcli_.AppRun.MainJsonGetter;
import static ru.antebeotcli.antebeotcli_.RegController.getErrorAlert;
import static ru.antebeotcli.antebeotcli_.TemplatesLoader.getSelfBalances;

public class PriceChartController {
    @FXML
    protected void exitProgram() {
        System.exit(0);
    }
    @FXML
    ListView<String> coinsList, coinsList1;
    @FXML Text balanceLabel;
    @FXML
    BarChart chart;
    @FXML
    Button getGraphBtn;
    private String first,second;
    private int curOffset=0;
    private JSONArray m_lastTrades;
    @FXML
    VBox mainPane;
    public static List<String> getAllowCoins() throws ExecutionException, InterruptedException, TimeoutException {
        var coins = new String(MainJsonGetter.getRaw("/api?w=getallowcoins")).replace("[","").
                replace("]","").replace("\"","");
        return List.of(coins.split(","));
    }

    @FXML
    protected void initialize()
    {
        // var getter = MainJsonGetter;
        try {
            var allowCoins = getAllowCoins();
            //System.out.println(allowCoins);
            getLastTradeHistory();
            for(var coin : allowCoins)
            {
                coinsList.getItems().add(coin);
                coinsList1.getItems().add(coin);
            }
        } catch(Exception err)
        {
            getErrorAlert(err.toString()).show();
        }
    }
    @FXML
    protected  void conList1CoinSelected()
    {
        first = coinsList.getSelectionModel().getSelectedItem();
        //System.out.println("first is "+ first);
        if (second != null && first != null) updateChart();
    }
    private void getLastTradeHistory()
    {
        try {
            //System.out.println("get lastDoneTrades");
            if (m_lastTrades == null) m_lastTrades = new JSONArray();
            while(true)
            {
                // todo: getLastDoneTradesOfToday. because after 10kk trades is will freeze Desktop.
                // So. in future version will be exists like getLastDonTradesOfLastDays?days=1&lim=25&offset=... but for now is okey
                var doneTrades = new JSONArray(new String(MainJsonGetter.getRaw("/exchange?w=getLastDoneTrades&lim=25&offset="+curOffset)));
                if (doneTrades.length() == 0) break;
                m_lastTrades.putAll(doneTrades);
                /*var curTime = System.currentTimeMillis();
                var oneDay = TimeUnit.DAYS.toMillis(1);
                for(var obj : doneTrades)
                {
                    var jo = (JSONObject)obj;
                    var time = jo.getLong("time");
                    if (time > curTime - oneDay) m_lastTrades.put(obj);
                }*/
                //System.out.println("doneTrades was put:" + doneTrades);
                curOffset+=25;
            }
            //System.out.println("m_lastTrades = "+ m_lastTrades);
        } catch(Exception err)
        {
            // System.out.println("err: " +err.toString());
            getErrorAlert(err.toString());
        }

    }

    private void updateChart() {
        try {
            var uLang = new i18n();
            var balances = getSelfBalances();
            //System.out.println(balances);
            var balanceForFirst = balances.getJSONObject(first);
            var balanceForSecond = balances.getJSONObject(second);
            var setBalance = String.format(uLang.getString("PriceChartBalance"), balanceForFirst.getString("balance"), balanceForFirst.getString("CoinName"),
                    balanceForSecond.getString("balance"), balanceForSecond.getString("CoinName"));
            balanceLabel.setText(setBalance);
        } catch(Exception err){
            getErrorAlert(err.toString()).show();
        }
        chart.getData().clear();
        //System.out.println("Update chart for "+first+"/"+second);
        var prices = new HashSet<String>();
        //chartLabel.setText( first + "/" + second );
        // .forEach({if(it)});
        var it = m_lastTrades.iterator();
        while(it.hasNext())
        {
            var element = (JSONObject)it.next();
            var info = element.getJSONObject("info");
            if(info.getString("toGiveName").equals(first) && info.getString("toGetName").equals(second))
            {
                // found
                prices.add(info.getString("priceRatio"));
            }
        }
        //System.out.println("prices:");
       // System.out.println(prices);

// https://github.com/brcolow/candlefx simillar in future will be

        XYChart.Series series = new XYChart.Series();
        series.setName(first + "/" + second );
        int x = 0;
        var lastPrice = new BigDecimal(0);
        for(var price :prices)
        {
            var y = new BigDecimal(price);
            series.getData().add(new XYChart.Data(String.valueOf(x), y));
            if ( y.compareTo(lastPrice) > 0 ) x++;
            else if (y.compareTo(lastPrice) < 0) x--;
            lastPrice = y;
        }
        chart.getData().addAll(series); // we can use BarArea with all prices. is will be better way.
    }

    @FXML
    protected  void conListCoinSelected()
    {
        second = coinsList1.getSelectionModel().getSelectedItem();
        //System.out.println("second is "+ first);
        if (second != null && first != null) {
            updateChart();
            second = first = null;
        }
    }

    @FXML
    protected void showHelpAlert() {
        TemplatesLoader.showHelpAlert();
    }
    @FXML
    protected void getGraph()
    {
        getLastTradeHistory();
        first = coinsList.getSelectionModel().getSelectedItem();
        second = coinsList1.getSelectionModel().getSelectedItem();
        updateChart();
    }

    @FXML
    public void OpenPriceChart() {
        TemplatesLoader.load("pricechart.fxml", mainPane);
    }
    @FXML
    public void OpenProfile() {
        TemplatesLoader.load("profile.fxml", mainPane);
    }
}
