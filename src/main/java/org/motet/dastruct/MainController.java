package org.motet.dastruct;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.text.Text;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MainController {

    @FXML
    private FlowPane roomFlow;

    @FXML
    private TextField searchField;

    private static final String ROOMS_CSV_PATH = "src/main/resources/org/motet/dastruct/rooms.csv";
    private Text importPromptText;

    // Persistent list of rooms
    private List<Room> rooms;

    public void initialize() {
        importPromptText = new Text("Import room data");
        importPromptText.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");
        if (Files.exists(Paths.get(ROOMS_CSV_PATH))) {
            loadRoomsFromCsv();
            refreshRoomGrid();
        } else {
            roomFlow.getChildren().clear();
            roomFlow.getChildren().add(importPromptText);
        }
    }

    private void addCountdown(Label label, Room room) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            java.time.Duration remaining = room.getRemainingOccupancyDuration();
            if (remaining != null && !remaining.isZero()) {
                long hours = remaining.toHours();
                long minutes = (remaining.toMinutes() % 60);
                long seconds = (remaining.getSeconds() % 60);
                label.setText("Occupied (" + String.format("%02d:%02d:%02d", hours, minutes, seconds) + ")");
            } else {
                label.setText("Occupied (00:00:00)");
                // Change status to Vacant and refresh UI
                if (room.getStatus().equals("Occupied")) {
                    room.setStatus("Vacant");
                    room.setOccupancyEndTime(null);
                    refreshRoomGrid();
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private VBox createRoomCard(Room room) {
        VBox roomCard = new VBox(10);
        roomCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 15; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label roomName = new Label(room.getName());
        roomName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label roomType = new Label(room.getType());

        HBox statusBox = new HBox(5);
        Circle statusCircle = new Circle(5);
        statusCircle.setFill(room.getStatusColor());
        Label statusLabel = new Label(room.getStatus());
        if (room.getStatus().equals("Occupied") && room.getOccupancyEndTime() != null) {
            addCountdown(statusLabel, room);
        }
        statusBox.getChildren().addAll(statusCircle, statusLabel);

        Label capacity = new Label("Capacity: " + room.getCapacity());
        // Removed equipment label

        // Add next reservation label
        String nextReservation = room.getNextReservation();
        Label reservationLabel = new Label("Reservation: " + (nextReservation != null ? nextReservation : "None"));

        HBox buttons = new HBox(10);
        Button reserveButton = new Button("Reserve");
        reserveButton.setOnAction(event -> openReserveWindow(room));
        Button changeStatusButton = new Button("Change Status");
        changeStatusButton.setOnAction(event -> openChangeStatusWindow(room));
        buttons.getChildren().addAll(reserveButton, changeStatusButton);

        roomCard.getChildren().addAll(roomName, roomType, statusBox, capacity, reservationLabel, buttons);

        return roomCard;
    }

    private void openChangeStatusWindow(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("change-status.fxml"));
            Parent root = loader.load();
            root.getProperties().put("controller", this);

            ChangeStatusController controller = loader.getController();
            controller.setRoom(room);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Change Status");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openReserveWindow(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/motet/dastruct/reserve.fxml"));
            Parent root = loader.load();
            root.getProperties().put("controller", this);

            ReserveController controller = loader.getController();
            controller.setRoom(room);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manage Reservations");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onImportRooms() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Rooms (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(roomFlow.getScene().getWindow());
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                List<Room> importedRooms = new ArrayList<>();
                String line;
                boolean first = true;
                while ((line = reader.readLine()) != null) {
                    if (first) { first = false; continue; } // skip header
                    String[] parts = line.split(",", -1);
                    if (parts.length < 4) throw new IllegalArgumentException("Invalid CSV format: missing columns.");
                    String name = parts[0];
                    String type = parts[1];
                    int capacity = Integer.parseInt(parts[2]);
                    String status = parts[3];
                    Room room = new Room(name, type, capacity, status);
                    if (parts.length > 4 && !parts[4].isEmpty()) {
                        room.setOccupancyEndTime(LocalDateTime.parse(parts[4]));
                    }
                    if (parts.length > 5 && !parts[5].isEmpty()) {
                        String[] reservations = parts[5].replaceAll("^\"|\"$", "").split(";");
                        for (String res : reservations) {
                            if (!res.isBlank()) room.addReservation(res.trim());
                        }
                    }
                    importedRooms.add(room);
                }
                rooms = importedRooms;
                saveRoomsToCsv();
                refreshRoomGrid();
            } catch (Exception e) {
                showError("Failed to import rooms: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onExportRooms() {
        try {
            saveRoomsToCsv();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Rooms (CSV)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(roomFlow.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("Room Name,Type,Capacity,Status,OccupancyEndTime,Reservations");
                    for (Room room : rooms) {
                        String occEnd = room.getOccupancyEndTime() != null ? room.getOccupancyEndTime().toString() : "";
                        String reservations = String.join(";", room.getReservations());
                        if (reservations.contains(",") || reservations.contains(";")) {
                            reservations = '"' + reservations + '"';
                        }
                        writer.printf("%s,%s,%d,%s,%s,%s\n",
                            room.getName(),
                            room.getType(),
                            room.getCapacity(),
                            room.getStatus(),
                            occEnd,
                            reservations
                        );
                    }
                }
            }
        } catch (Exception e) {
            showError("Failed to export rooms: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().toLowerCase();
        roomFlow.getChildren().clear();
        for (Room room : rooms) {
            if (room.getName().toLowerCase().contains(query) ||
                room.getType().toLowerCase().contains(query) ||
                room.getStatus().toLowerCase().contains(query)) {
                VBox roomCard = createRoomCard(room);
                roomCard.setPrefWidth(300);
                roomFlow.getChildren().add(roomCard);
            }
        }
    }

    public void refreshRoomGrid() {
        if (rooms == null || rooms.isEmpty()) {
            roomFlow.getChildren().clear();
            roomFlow.getChildren().add(importPromptText);
            return;
        }
        saveRoomsToCsv();
        roomFlow.getChildren().clear();
        for (Room room : rooms) {
            VBox roomCard = createRoomCard(room);
            roomCard.setPrefWidth(300); // Responsive card width
            roomFlow.getChildren().add(roomCard);
        }
    }

    private void loadRoomsFromCsv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ROOMS_CSV_PATH))) {
            List<Room> importedRooms = new ArrayList<>();
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                String name = parts[0];
                String type = parts[1];
                int capacity = Integer.parseInt(parts[2]);
                String status = parts[3];
                Room room = new Room(name, type, capacity, status);
                if (parts.length > 4 && !parts[4].isEmpty()) {
                    room.setOccupancyEndTime(LocalDateTime.parse(parts[4]));
                }
                if (parts.length > 5 && !parts[5].isEmpty()) {
                    String[] reservations = parts[5].replaceAll("^\"|\"$", "").split(";");
                    for (String res : reservations) {
                        if (!res.isBlank()) room.addReservation(res.trim());
                    }
                }
                importedRooms.add(room);
            }
            rooms = importedRooms;
        } catch (Exception e) {
            rooms = new ArrayList<>();
        }
    }

    private void saveRoomsToCsv() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_CSV_PATH))) {
            writer.println("Room Name,Type,Capacity,Status,OccupancyEndTime,Reservations");
            for (Room room : rooms) {
                String occEnd = room.getOccupancyEndTime() != null ? room.getOccupancyEndTime().toString() : "";
                String reservations = String.join(";", room.getReservations());
                if (reservations.contains(",") || reservations.contains(";")) {
                    reservations = '"' + reservations + '"';
                }
                writer.printf("%s,%s,%d,%s,%s,%s\n",
                    room.getName(),
                    room.getType(),
                    room.getCapacity(),
                    room.getStatus(),
                    occEnd,
                    reservations
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}