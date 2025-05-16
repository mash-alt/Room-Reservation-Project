package org.motet.dastruct;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    private MainController mainController;    public void setRoom(Room room, MainController mainController) {
        this.room = room;
        this.mainController = mainController;
        roomNameLabel.setText(room.getName());
        roomTypeLabel.setText(room.getType());
        statusCircle.setFill(room.getStatusColor());
        statusLabel.setText(room.getStatus());
        capacityLabel.setText("Capacity: " + room.getCapacity());
        String nextReservation = room.getNextReservation();
        reservationLabel.setText("Reservation: " + (nextReservation != null ? nextReservation : "None"));
        
        // Add countdown for occupied rooms with an end time
        if (room.getStatus().equals("Occupied") && room.getOccupancyEndTime() != null) {
            addCountdown(statusLabel, room);
        }
    }
    
    private void addCountdown(Label label, Room room) {
        System.out.println("DEBUG: Starting countdown for room: " + room.getName());
        System.out.println("DEBUG: Initial end time: " + room.getOccupancyEndTime());
        
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
            java.time.Duration remaining = room.getRemainingOccupancyDuration();
            System.out.println("DEBUG: " + room.getName() + " - Remaining duration: " + remaining);
            
            if (remaining != null && !remaining.isZero() && !remaining.isNegative()) {
                long hours = remaining.toHours();
                long minutes = (remaining.toMinutes() % 60);
                long seconds = (remaining.getSeconds() % 60);
                String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                System.out.println("DEBUG: Setting countdown text: " + formattedTime);
                label.setText("Occupied (" + formattedTime + ")");
            } else {
                System.out.println("DEBUG: Countdown finished or invalid for " + room.getName());
                label.setText("Occupied (00:00:00)");
                // Change status to Vacant and refresh UI
                if (room.getStatus().equals("Occupied")) {
                    System.out.println("DEBUG: Changing room status to Vacant");
                    room.setStatus("Vacant");
                    room.setOccupancyEndTime(null);
                    mainController.refreshRoomGrid();
                }
            }
        }));
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
        System.out.println("DEBUG: Timeline started for " + room.getName());
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
