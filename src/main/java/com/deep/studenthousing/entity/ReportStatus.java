package com.deep.studenthousing.entity;

public enum ReportStatus {
    NEW("New"),
    UNDER_REVIEW("Under review"),
    NEED_INFO("Need info"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String label;
    ReportStatus(String label) { this.label = label; }
    public String getLabel() { return label; }

    /** Approved / Rejected = decision made, owner can no longer add messages. */
    public boolean isClosed() { return this == APPROVED || this == REJECTED; }
}
