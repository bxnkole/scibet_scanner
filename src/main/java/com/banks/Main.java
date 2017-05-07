package com.banks;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class Main extends Application implements Initializable {

    public DatePicker dtPkGameDate;
    public ComboBox<Vendor> cbPredictVendor;
    public ComboBox cbThreshold;
    public Button btnStart;
    public ProgressBar progressBar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.out.println();
        Scene scene = new Scene(new FXMLLoader().load(getClass().getResourceAsStream("/main.fxml")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void doStartAction() {
        java.sql.Date date = java.sql.Date.valueOf(dtPkGameDate.getValue());
        Vendor vendor = cbPredictVendor.getValue();
        Double threshold = Double.valueOf(String.valueOf(cbThreshold.getValue()));

        Scanner scanner = null;
        Runnable runnable;
        switch (vendor) {
            case PROGSPORT_BBALL:
                scanner = new ProgSportBasketballScanner(date, threshold);
                break;
            case SCIBET_FOOTBALL:
                scanner = new ScibetFootballScanner(date, threshold);
                break;
        }

        progressBar.setVisible(true);
        btnStart.setDisable(true);
        final Scanner finalScanner = scanner;
        runnable = new Runnable() {
            @Override
            public void run() {
                finalScanner.scanGames();
                finalScanner.quitDriver();
                progressBar.setVisible(false);
                btnStart.setDisable(false);
            }
        };
        new Thread(runnable).start();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dtPkGameDate.setValue(LocalDate.now());
        cbPredictVendor.setItems(FXCollections.observableArrayList(Vendor.values()));
        cbPredictVendor.setValue(Vendor.SCIBET_FOOTBALL);

        cbThreshold.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 100).boxed().toArray()));
        cbThreshold.setValue(80);

        progressBar.setVisible(false);
    }

    enum Vendor {
        PROGSPORT_BBALL, SCIBET_FOOTBALL
    }

    static {
    }
}
