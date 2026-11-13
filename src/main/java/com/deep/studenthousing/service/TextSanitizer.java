package com.deep.studenthousing.service;

/** Small helpers for text that comes from users. */
public final class TextSanitizer {
    private TextSanitizer() {}

    /** Drops control characters (a NUL byte makes PostgreSQL reject the row) but keeps line breaks. */
    public static String clean(String s) {
        return s == null ? "" : s.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "").trim();
    }

    /** Hides email addresses and long digit sequences (phone numbers). */
    public static String redact(String s) {
        if (s == null) return "";
        return s.replaceAll("[A-Za-z0-9._%+'-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[email hidden]")
                .replaceAll("\\+?(?:\\d[\\s().-]?){9,}\\d", "[number hidden]");
    }

    public static String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
