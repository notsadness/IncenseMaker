package net.botwithus.incense;

public enum Incense {
    GUAM_INCENSE("Guam incense sticks", 47699, Herbs.GUAM, Ashes.IMPIOUS, Logs.LOGS, BaseSticks.WOODEN, AshedSticks.IMPIOUS),
    TARROMIN_INCENSE("Tarromin incense sticks", 47700, Herbs.TARROMIN, Ashes.IMPIOUS, Logs.LOGS, BaseSticks.WOODEN, AshedSticks.IMPIOUS),
    MARRENTILL_INCENSE("Marrentill incense sticks", 47701, Herbs.MARRENTILL, Ashes.IMPIOUS, Logs.LOGS, BaseSticks.WOODEN, AshedSticks.IMPIOUS),
    HARRALANDER_INCENSE("Harralander incense sticks", 47702, Herbs.GUAM, Ashes.IMPIOUS, Logs.OAK, BaseSticks.OAK, AshedSticks.IMPIOUS),
    RANARR_INCENSE("Ranarr incense sticks", 47703, Herbs.RANARR, Ashes.IMPIOUS, Logs.OAK, BaseSticks.OAK, AshedSticks.IMPIOUS),
    TOADFLAX_INCENSE("Toadflax incense sticks", 47704, Herbs.TOADFLAX, Ashes.IMPIOUS, Logs.WILLOW, BaseSticks.WILLOW, AshedSticks.IMPIOUS),
    SPIRITWEED_INCENSE("Spirit weed incense sticks", 47705, Herbs.SPIRIT_WEED, Ashes.IMPIOUS, Logs.WILLOW, BaseSticks.WILLOW, AshedSticks.IMPIOUS),
    IRIT_INCENSE("Irit incense sticks", 47706, Herbs.IRIT, Ashes.ACCURSED, Logs.MAPLE, BaseSticks.MAPLE, AshedSticks.ACCURSED_MAPLE),
    WERGALI_INCENSE("Wergali incense sticks", 47707, Herbs.IRIT, Ashes.ACCURSED, Logs.MAPLE, BaseSticks.MAPLE, AshedSticks.ACCURSED_MAPLE),
    AVANTOE_INCENSE("Avantoe incense sticks", 47708, Herbs.AVANTOE, Ashes.ACCURSED, Logs.ACADIA, BaseSticks.ACADIA, AshedSticks.ACCURSED_ACADIA),
    KWUARM_INCENSE("Kwuarm incense sticks", 47709, Herbs.KWUARM, Ashes.ACCURSED, Logs.ACADIA, BaseSticks.ACADIA, AshedSticks.ACCURSED_ACADIA),
    BLOODWEED_INCENSE("Bloodweed incense sticks", 47710, Herbs.BLOODWEED, Ashes.ACCURSED, Logs.ACADIA, BaseSticks.ACADIA, AshedSticks.ACCURSED_ACADIA),
    SNAPDRAGON_INCENSE("Snapdragon incense sticks", 47711, Herbs.SNAPDRAGON, Ashes.ACCURSED, Logs.ACADIA, BaseSticks.ACADIA, AshedSticks.ACCURSED_ACADIA),
    CADANTINE_INCENSE("Cadantine incense sticks", 47712, Herbs.CADANTINE, Ashes.INFERNAL, Logs.YEW, BaseSticks.YEW, AshedSticks.INFERNAL_YEW),
    LANTADYME_INCENSE("Lantadyme incense sticks", 47713, Herbs.LANTADYME, Ashes.INFERNAL, Logs.YEW, BaseSticks.YEW, AshedSticks.INFERNAL_YEW),
    DWARFWEED_INCENSE("Dwarf weed incense sticks", 47714, Herbs.DWARF_WEED, Ashes.INFERNAL, Logs.YEW, BaseSticks.YEW, AshedSticks.INFERNAL_YEW),
    TORSTOL_INCENSE("Torstol incense sticks", 47715, Herbs.TORSTOL, Ashes.INFERNAL, Logs.MAGIC, BaseSticks.MAGIC, AshedSticks.INFERNAL_MAGIC),
    FELLSTALK_INCENSE("Fellstalk incense sticks", 47716, Herbs.FELLSTALK, Ashes.INFERNAL, Logs.MAGIC, BaseSticks.MAGIC, AshedSticks.INFERNAL_MAGIC);

    private final String ItemName;
    private final int ItemId;
    private final Herbs SelectedHerbs;
    private final Ashes SelectedAshes;
    private final Logs SelectedLogs;
    private final BaseSticks SelectedBase;
    private final AshedSticks HalfSticks;

    Incense(String ItemName, int ItemId, Herbs Herbs, Ashes Ashes, Logs Logs, BaseSticks Base, AshedSticks Ashed) {
        this.ItemName = ItemName;
        this.ItemId = ItemId;
        this.SelectedHerbs = Herbs;
        this.SelectedAshes = Ashes;
        this.SelectedLogs = Logs;
        this.SelectedBase = Base;
        this.HalfSticks = Ashed;
    }

    public String getItemName() {
        return ItemName;
    }

    public int getItemId() {
        return ItemId;
    }

    public Herbs getSelectedHerbs() {
        return SelectedHerbs;
    }

    public Ashes getSelectedAshes() {
        return SelectedAshes;
    }

    public Logs getSelectedLogs() {
        return SelectedLogs;
    }

    public BaseSticks getBaseSticks() {
        return SelectedBase;
    }

    public AshedSticks getAshedSticks() {
        return HalfSticks;
    }
}