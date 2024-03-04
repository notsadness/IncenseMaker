package net.botwithus.incense;

public enum BaseSticks {
   WOODEN("Wooden incense sticks", 47685, Logs.LOGS),
   OAK("Oak incense sticks", 47686, Logs.OAK),
   WILLOW("Willow incense sticks", 47687, Logs.WILLOW),
   ACADIA("Acadia incense sticks", 47689, Logs.ACADIA),
   MAPLE("Maple incense sticks", 47688, Logs.MAPLE),
   YEW("Yew incense sticks", 47690, Logs.YEW),
   MAGIC("Magic incense sticks", 47691, Logs.MAGIC);
   
   private final int ItemId;
   private final String ItemName;
   private final Logs logType; // Example of a relationship

   BaseSticks(String ItemName, int ItemId, Logs Logs) {
      this.ItemId = ItemId;
      this.ItemName = ItemName;
      this.logType = Logs;
   }

   public int getItemId() {
      return ItemId;
   }

   public String getItemName() {
      return ItemName;
   }

   public Logs getLogType() {
      return logType;
   }
}