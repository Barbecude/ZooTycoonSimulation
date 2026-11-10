package net.zootycoon.license;

public enum LicenseProgressionState {
    LOCKED,
    COLLECTING,
    ELIGIBLE_FOR_INSPECTION,
    INSPECTOR_REQUESTED,
    WAITING_FOR_CONFIRMATION,
    INSPECTING,
    INSPECTION_COMPLETED,
    INSPECTION_PASSED,
    INSPECTION_FAILED,
    LETTER_CLAIMED,
    LICENSE_CLAIMED,
    BANNER_CLAIMED,
    LICENSE_UNLOCKED;

    public static LicenseProgressionState fromName(String name, LicenseProgressionState fallback) {
        if (name == null || name.isEmpty()) return fallback;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
