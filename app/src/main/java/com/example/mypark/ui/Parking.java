package com.example.mypark.ui;

public class Parking {

    private String documentId;
    private String name;
    private double lat;
    private double lng;
    private String time;
    private boolean reserved;

    public Parking(String documentId, String name, double lat, double lng, String time, boolean reserved) {
        this.documentId = documentId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.time = time;
        this.reserved = reserved;
    }

    public String getDocumentId() { return documentId; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getTime() { return time; }
    public boolean isReserved() { return reserved; }
}
