package com.expresofast.dto;

public class TrackingExisteDTO {

    private String trackingNumber;
    private boolean existe;

    public TrackingExisteDTO() {
    }

    public TrackingExisteDTO(String trackingNumber, boolean existe) {
        this.trackingNumber = trackingNumber;
        this.existe = existe;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public boolean isExiste() {
        return existe;
    }

    public void setExiste(boolean existe) {
        this.existe = existe;
    }
}
