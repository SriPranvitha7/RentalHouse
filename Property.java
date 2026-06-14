package com.housefinder;

public class Property {

    private int    id;
    private String owner;
    private String phone;
    private String location;
    private String address;
    private int    rent;
    private String type;
    private double lat;
    private double lng;

    // ── Constructor (used when creating from form data) ──────
    public Property(String owner, String phone, String location,
                    String address, int rent, String type,
                    double lat, double lng) {
        this.owner    = owner;
        this.phone    = phone;
        this.location = location;
        this.address  = address;
        this.rent     = rent;
        this.type     = type;
        this.lat      = lat;
        this.lng      = lng;
    }

    // ── Constructor (used when reading from database) ────────
    public Property(int id, String owner, String phone, String location,
                    String address, int rent, String type,
                    double lat, double lng) {
        this.id       = id;
        this.owner    = owner;
        this.phone    = phone;
        this.location = location;
        this.address  = address;
        this.rent     = rent;
        this.type     = type;
        this.lat      = lat;
        this.lng      = lng;
    }

    // ── Getters ──────────────────────────────────────────────
    public int    getId()       { return id; }
    public String getOwner()    { return owner; }
    public String getPhone()    { return phone; }
    public String getLocation() { return location; }
    public String getAddress()  { return address; }
    public int    getRent()     { return rent; }
    public String getType()     { return type; }
    public double getLat()      { return lat; }
    public double getLng()      { return lng; }

    // ── Convert to JSON string (sent back to frontend) ───────
    public String toJSON() {
        return "{"
            + "\"id\":"       + id           + ","
            + "\"owner\":\""  + owner        + "\","
            + "\"phone\":\""  + phone        + "\","
            + "\"location\":\"" + location   + "\","
            + "\"address\":\"" + address     + "\","
            + "\"rent\":"     + rent         + ","
            + "\"type\":\""   + type         + "\","
            + "\"lat\":"      + lat          + ","
            + "\"lng\":"      + lng
            + "}";
    }
}