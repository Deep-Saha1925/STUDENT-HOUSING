package com.deep.studenthousing.entity;

public enum ComplaintStatus {
    NEW("New"),
    UNDER_REVIEW("Under review"),
    SENT_TO_OWNER("Sent to owner"),
    RESOLVED("Resolved"),
    DISMISSED("Dismissed");

    private final String label;
    ComplaintStatus(String label) { this.label = label; }
    public String getLabel() { return label; }

    /** Resolved / Dismissed = finished, the student can no longer add messages. */
    public boolean isClosed() { return this == RESOLVED || this == DISMISSED; }
}
