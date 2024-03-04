package net.botwithus.incense;

public enum AshedSticks {
    IMPIOUS("Impious incense sticks", 47692, Logs.LOGS, Ashes.IMPIOUS, BaseSticks.WOODEN),
    IMPIOUS_OAK("Impious oak incense sticks", 47693, Logs.OAK, Ashes.IMPIOUS, BaseSticks.OAK),
    IMPIOUS_WILLOW("Impious willow incense sticks", 47694, Logs.WILLOW, Ashes.IMPIOUS, BaseSticks.WILLOW),
    ACCURSED_MAPLE("Accursed acadia incense sticks", 47695, Logs.MAPLE, Ashes.ACCURSED, BaseSticks.MAPLE),
    ACCURSED_ACADIA("Accursed maple incense sticks", 47696, Logs.ACADIA, Ashes.ACCURSED, BaseSticks.ACADIA),
    INFERNAL_YEW("Infernal yew incense sticks", 47695, Logs.YEW, Ashes.INFERNAL, BaseSticks.YEW),
    INFERNAL_MAGIC("Infernal magic incense sticks", 47698, Logs.MAGIC, Ashes.INFERNAL,BaseSticks.MAGIC);

    private final int ItemId;
    private final String ItemName;
    private final Logs LogType;
    private final Ashes AshType;
    private final BaseSticks Base;

    AshedSticks(String ItemName, int ItemId, Logs Logs, Ashes Ash, BaseSticks Sticks) {
        this.ItemId = ItemId;
        this.ItemName = ItemName;
        this.LogType = Logs;
        this.AshType = Ash;
        this.Base = Sticks;
    }

    public int getItemId() {
        return ItemId;
    }

    public String getItemName() {
        return ItemName;
    }

    public Logs getLogType() {
        return LogType;
    }

    public Ashes getAshType() {
        return AshType;
    }

    public BaseSticks getBaseSticks() {
        return Base;
    }
}