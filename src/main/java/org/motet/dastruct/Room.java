package org.motet.dastruct;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;

public class Room {
    private final String name;
    private final String type;
    private final int capacity;
    private String status;
    private String duration;
    private String reservationTime;
    private LocalDateTime occupancyEndTime;
    private final List<String> reservations = new ArrayList<>();

    public Room(String name, String type, int capacity, String status) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }

    public LocalDateTime getOccupancyEndTime() {
        return occupancyEndTime;
    }

    public void setOccupancyEndTime(LocalDateTime endTime) {
        this.occupancyEndTime = endTime;
    }

    public Duration getRemainingOccupancyDuration() {
        if (occupancyEndTime == null) return null;
        Duration duration = Duration.between(LocalDateTime.now(), occupancyEndTime);
        return duration.isNegative() ? Duration.ZERO : duration;
    }

    public List<String> getReservations() {
        return reservations;
    }

    public boolean addReservation(String reservationTime) {
        // Check for overlap logic can be added here
        reservations.add(reservationTime);
        return true;
    }

    public void removeReservation(String reservationTime) {
        reservations.remove(reservationTime);
    }

    public Color getStatusColor() {
        return switch (status) {
            case "Vacant" -> Color.GREEN;
            case "Occupied" -> Color.RED;
            case "Reserved" -> Color.YELLOW;
            default -> Color.GRAY;
        };
    }

    public String getNextReservation() {
        if (reservations.isEmpty()) {
            return null;
        }
        return reservations.get(0); // Assuming reservations are added in chronological order
    }
}