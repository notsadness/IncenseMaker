package net.botwithus.incense;

public enum Incense {
    GUAM_INCENSE("Guam incense sticks", 47699),
    TARROMIN_INCENSE("Tarromin incense sticks", 47700),
    MARRENTILL_INCENSE("Marrentill incense sticks", 47701),
    HARRALANDER_INCENSE("Harralander incense sticks", 47702),
    RANARR_INCENSE("Ranarr incense sticks", 47703),
    TOADFLAX_INCENSE("Toadflax incense sticks", 47704),
    SPIRITWEED_INCENSE("Spirit weed incense sticks", 47705),
    IRIT_INCENSE("Irit incense sticks", 47706),
    WERGALI_INCENSE("Wergali incense sticks", 47707),
    AVANTOE_INCENSE("Avantoe incense sticks", 47708),
    KWUARM_INCENSE("Kwuarm incense sticks", 47709),
    BLOODWEED_INCENSE("Bloodweed incense sticks", 47710),
    SNAPDRAGON_INCENSE("Snapdragon incense sticks", 47711),
    CADANTINE_INCENSE("Cadantine incense sticks", 47712),
    LANTADYME_INCENSE("Lantadyme incense sticks", 47713),
    DWARFWEED_INCENSE("Dwarf weed incense sticks", 47714),
    TORSTOL_INCENSE("Torstol incense sticks", 47715),
    FELLSTALK_INCENSE("Fellstalk incense sticks", 47716);

    private final String ItemName;
    private final int ItemId;

    Incense(String ItemName, int ItemId) {
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
