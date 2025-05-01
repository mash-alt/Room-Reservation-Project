package org.motet.dastruct;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ReserveController {

    @FXML
    private ListView<String> reservationListView;

    @FXML
    private TextField reservationTimeField;

    @FXML
    private Button addReservationButton;

    @FXML
    private Button closeButton;

    @FXML
    private DatePicker reservationDatePicker;

    @FXML
    private Spinner<Integer> hourSpinner;

    @FXML
    private Spinner<Integer> minuteSpinner;

    private Room room;

    public void setRoom(Room room) {
        this.room = room;
        reservationListView.getItems().addAll(room.getReservations());
    }

    @FXML
    public void initialize() {
        // Initialize spinners for hour and minute
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
    }

    @FXML
    private void onAddReservationButtonClick() {
        LocalDate date = reservationDatePicker.getValue();
        Integer hour = hourSpinner.getValue();
        Integer minute = minuteSpinner.getValue();

        if (date == null) {
            showError("Please select a date for the reservation.");
            return;
        }
        if (hour == null || minute == null) {
            showError("Please select both hour and minute for the reservation.");
            return;
        }
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            showError("Hour must be 0-23 and minute must be 0-59.");
            return;
        }
        String reservationTime = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + String.format("%02d:%02d", hour, minute);
        if (room.addReservation(reservationTime)) {
            reservationListView.getItems().add(reservationTime);
            reservationDatePicker.setValue(null);
            hourSpinner.getValueFactory().setValue(12);
            minuteSpinner.getValueFactory().setValue(0);

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
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}