package org.motet.dastruct;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.time.LocalDateTime;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ChangeStatusController {

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField durationField;

    @FXML
    private Spinner<Integer> durationHourSpinner;

    @FXML
    private Spinner<Integer> durationMinuteSpinner;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Room room;

    public void setRoom(Room room2) {
        this.room = room2;
        statusComboBox.getItems().addAll("Vacant", "Occupied");
        statusComboBox.setValue(room2.getStatus());
        durationHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        durationMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        
        // Disable/enable duration spinners based on status selection
        boolean isOccupied = room2.getStatus().equals("Occupied");
        durationHourSpinner.setDisable(!isOccupied);
        durationMinuteSpinner.setDisable(!isOccupied);
        statusComboBox.setOnAction(e -> {
            boolean occupied = statusComboBox.getValue().equals("Occupied");
            durationHourSpinner.setDisable(!occupied);
            durationMinuteSpinner.setDisable(!occupied);
            if (!occupied) {
                durationHourSpinner.getValueFactory().setValue(0);
                durationMinuteSpinner.getValueFactory().setValue(0);
            }
        });
    }

    @FXML
    private void onSaveButtonClick() {
        String selectedStatus = statusComboBox.getValue();
        if (selectedStatus.equals("Occupied")) {
            int hours = durationHourSpinner.getValue();
            int minutes = durationMinuteSpinner.getValue();
            if (hours == 0 && minutes == 0) {
                showError("Please enter a duration (hour or minute must be greater than 0) for Occupied status.");
                return;
            }
            room.setStatus("Occupied");
            LocalDateTime endTime = LocalDateTime.now().plusHours(hours).plusMinutes(minutes);
            room.setOccupancyEndTime(endTime);
        } else {
            room.setStatus("Vacant");
            room.setOccupancyEndTime(null);
        }

        // Refresh the main window
        Stage mainWindow = (Stage) Stage.getWindows().filtered(window -> window instanceof Stage && ((Stage) window).getTitle().equals("Hello!")).get(0);
        if (mainWindow != null) {
            Scene scene = mainWindow.getScene();
            if (scene != null) {
                Parent root = scene.getRoot();
                MainController mainController = (MainController) root.getProperties().get("controller");
                if (mainController != null) {
                    mainController.refreshRoomGrid();
                }
            }
        }

        closeWindow();
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onCancelButtonClick() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}