package com.deep.studenthousing.entity;

public enum ReportType {
    MAINTENANCE("Service down / maintenance"),
    PROPERTY_ISSUE("Property issue (damage, safety, repairs)"),
    BOOKING_DISPUTE("Booking or tenant dispute"),
    OTHER("Other");

    private final String label;
    ReportType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
