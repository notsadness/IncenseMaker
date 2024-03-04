package net.botwithus.incense;

public enum Logs {
    LOGS("Logs", 1511, BaseSticks.WOODEN),
    OAK("Oak logs", 1521, BaseSticks.OAK),
    WILLOW("Willow logs", 1519, BaseSticks.WILLOW),
    MAPLE("Maple logs", 1517, BaseSticks.MAPLE),
    ACADIA("Acadia logs", 40285, BaseSticks.ACADIA),
    YEW("Yew logs", 1515, BaseSticks.YEW),
    MAGIC("Magic logs", 1513, BaseSticks.MAGIC);

    private final int ItemId;
    private final String ItemName;
    private final BaseSticks Base;

    Logs(String ItemName, int ItemId, BaseSticks Sticks) {
        this.ItemName = ItemName;
        this.ItemId = ItemId;
        this.Base = Sticks;
    }

    public int getItemId() {
        return ItemId;
    }

    public String getItemName() {
        return ItemName;
    }

    public BaseSticks getBaseSticks() {
        return Base;
    }
}