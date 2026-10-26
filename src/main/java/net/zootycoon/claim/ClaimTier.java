package net.zootycoon.claim;

public enum ClaimTier {
    TIER_1_SMALL("Small Exhibit", 16, 16, 160),
    TIER_2_MEDIUM("Medium Enclosure", 32, 32, 640),
    TIER_3_LARGE("Large Safari", 64, 64, 2560),
    TIER_4_MEGA("Mega Park Reserve", 128, 128, 10240);

    private final String displayName;
    private final int width;
    private final int length;
    private final int costInEmeralds;

    ClaimTier(String displayName, int width, int length, int costInEmeralds) {
        this.displayName = displayName;
        this.width = width;
        this.length = length;
        this.costInEmeralds = costInEmeralds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public int getArea() {
        return width * length;
    }

    public int getCostInEmeralds() {
        return costInEmeralds;
    }

    public double getPricePerBlock() {
        return (double) costInEmeralds / getArea();
    }
}

