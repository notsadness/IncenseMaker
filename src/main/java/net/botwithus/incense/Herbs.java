package net.botwithus.incense;

public enum Herbs {
    GUAM("Clean guam", 249),
    TARROMIN("Clean Tarromin", 254),
    MARRENTILL("Clean Marrentill", 251),
    HARRALANDER("Clean Harralander", 255),
    RANARR("Clean Ranarr", 257),
    TOADFLAX("Clean Toadflax", 2998),
    SPIRIT_WEED("Clean Spirit Weed", 12172),
    IRIT("Clean Irit", 259),
    WERGALI("Clean Wergali", 14854),
    AVANTOE("Clean Avantoe", 261),
    KWUARM("Clean Kwuarm", 263),
    BLOODWEED("Clean Bloodweed", 37953),
    SNAPDRAGON("Clean Snapdragon", 3000),
    CADANTINE("Clean Cadantine", 265),
    LANTADYME("Clean Lantadyme", 2481),
    DWARF_WEED("Clean Dwarf Weed", 267),
    TORSTOL("Clean Torstol", 269),
    FELLSTALK("Clean Fellstalk", 21624);

    private final String ItemName;
    private final int ItemId;

    Herbs(String ItemName, int ItemId) {
        this.ItemName = ItemName;
        this.ItemId = ItemId;
    }

    public String getItemName() {
        return ItemName;
    }

    public int getItemId() {
        return ItemId;
    }
}