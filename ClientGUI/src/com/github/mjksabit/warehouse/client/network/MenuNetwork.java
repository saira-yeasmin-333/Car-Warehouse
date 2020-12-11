package com.github.mjksabit.warehouse.client.network;

import com.github.mjksabit.warehouse.client.FXUtil;
import com.github.mjksabit.warehouse.client.controller.Menu;
import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.view.Card;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MenuNetwork {

    private final Menu menuController;
    private ObservableList<Node> cards;

    private boolean isViewer = true;

    private Map<Integer, Car> cars = new HashMap<>();
    private Map<Integer, Card> cardMap = new HashMap<>();

    public MenuNetwork(Menu menuController, ObservableList<Node> cards) {
        this.menuController = menuController;
        this.cards = cards;

        ResponseListener responseListener = ServerConnect.getInstance().getResponseListener();

        responseListener.setErrorHandler(response -> FXUtil.showError(
                (Pane) menuController.getStage().getScene().getRoot(),
                response.getText().optString(Data.INFO, "Information not provided"),
                2000));

        responseListener.addHandler(Data.UPDATE_CAR, response -> {
            System.out.println("Car from Network: " + response.getText());
            if (!response.getText().has(Data.CAR)) {
                System.out.println("REMOVE CAR!!!");
                removeCar(response.getText().optInt(Data.CAR_ID));
            } else {
                final Car car = Car.fromData(response);
                updateCar(response.getText().optInt(Car.ID), car);
            }
        });

        ServerConnect.getInstance().sendRequest(new Data(Data.VIEW_ALL, null, null),
                response -> {});
    }

    private void removeCar(int id) {
        if (cars.containsKey(id) && cardMap.containsKey(id))
            Platform.runLater(() -> cards.remove(cardMap.remove(id)));
    }

    public void setViewer(boolean viewer) {
        isViewer = viewer;
        Card.setAsViewer(viewer);
    }

    private void buyCar(int id) {
        System.out.println("BUYING CAR with ID "+id);
        JSONObject object = new JSONObject();
        try {
            object.put(Data.CAR_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Data data = new Data(Data.BUY_CAR, object, null);
        ServerConnect.getInstance().sendRequest(data, response -> {
            System.out.println(response.getTYPE());
        });
    }

    private void removeCarRequest(int id) {
        System.out.println("Removing CAR with ID "+ id);
        JSONObject object = new JSONObject();
        try {
            object.put(Data.CAR_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Data data = new Data(Data.REMOVE_CAR, object, null);
        ServerConnect.getInstance().sendRequest(data, response -> {
            System.out.println(response.getTYPE());
        });
    }

    public void updateCar(int id, Car car) {
        if (!cars.containsKey(id)) {
            Card card = new Card(car);
            Platform.runLater(() -> cards.add(card));
            cardMap.put(id, card);
            card.setOnBuyListener((actionEvent -> buyCar(id)));
            card.setManufacturerListener(null, actionEvent -> removeCarRequest(id));
        } else {
            Platform.runLater(() -> cardMap.get(id).setCar(car));
        }

        cars.put(id, car);

    }

}
