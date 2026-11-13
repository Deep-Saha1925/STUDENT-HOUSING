package com.deep.studenthousing.entity;

public enum ComplaintCategory {
    CLEANLINESS("Cleanliness / hygiene"),
    SAFETY("Safety or security"),
    OVERCHARGING("Overcharging / payment problem"),
    LISTING_MISMATCH("Not as advertised"),
    BEHAVIOUR("Owner behaviour"),
    OTHER("Other");

    private final String label;
    ComplaintCategory(String label) { this.label = label; }
    public String getLabel() { return label; }
}
