package org.motet.dastruct;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class RoomCardController {
    @FXML
    private VBox roomCardVBox;
    @FXML
    private Label roomNameLabel;
    @FXML
    private Label roomTypeLabel;
    @FXML
    private Circle statusCircle;
    @FXML
    private Label statusLabel;
    @FXML
    private Label capacityLabel;
    @FXML
    private Label reservationLabel;
    @FXML
    private Button reserveButton;
    @FXML
    private Button changeStatusButton;

    private Room room;
    private MainController mainController;

    public void setRoom(Room room, MainController mainController) {
        this.room = room;
        this.mainController = mainController;
        roomNameLabel.setText(room.getName());
        roomTypeLabel.setText(room.getType());
        statusCircle.setFill(room.getStatusColor());
        statusLabel.setText(room.getStatus());
        capacityLabel.setText("Capacity: " + room.getCapacity());
        String nextReservation = room.getNextReservation();
        reservationLabel.setText("Reservation: " + (nextReservation != null ? nextReservation : "None"));
    }

    @FXML
    private void onReserve() {
        if (mainController != null && room != null) {
            mainController.openReserveWindow(room);
        }
    }

    @FXML
    private void onChangeStatus() {
        if (mainController != null && room != null) {
            mainController.openChangeStatusWindow(room);
        }
    }
}
