package net.botwithus.incense;

public enum Ashes {
    IMPIOUS("Impious ashes", 20264),
    ACCURSED("Accursed ashes", 20266),
    INFERNAL("Infernal ashes", 20268);

    private final int ItemId;
    private final String ItemName;

    Ashes(String ItemName, int ItemId) {
        this.ItemId = ItemId;
        this.ItemName = ItemName;
    }

    public int getItemId() {
        return ItemId;
    }

    public String getItemName() {
        return ItemName;
    }
}