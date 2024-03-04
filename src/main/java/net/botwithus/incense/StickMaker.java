package net.botwithus.incense;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.InventoryUpdateEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;
import java.util.Random;

public class StickMaker extends LoopingScript {
    public StickMaker(String name, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(name, scriptConfig, scriptDefinition);
    }

    // Core variables
    public int bankPresetId;
    public boolean runScript = false;
    public int currentPresetId;
    public int SelectedFunction; // There are more elegent ways to do this but eh.
    public boolean CheckForItem = false;
    public boolean HasSupplies = false;
    public long timeout = 90000;
    private Random random = new Random();
    public boolean logout = false;
    public boolean DebugScript = false;

    // Refactored for enums
    public Logs activeLogs;
    public Incense activeIncense;
    public AshedSticks activeAshedSticks;

    // SelectionFunction 0: Required for processing logs
    public int selectedLogId;
    public String selectedLogName;
    public boolean HasLogs = false;
    public String selectedBaseStick;

    // SelectionFunction 1: Required for adding ash to incense sticks
    public int selectedAshesId;
    public String selectedAshesName;
    public int selectedIncenseStickId; // The ID of the naked stick to be ashed. Eg: Magic incense stickss
    public String selectedIncenseStickName; // The name of the stick to be ashed. Eg: Magic incense sticks

    // SelectionFunction 2: Required for adding the herb to incense sticks
    public String selectedStickRawName; // The ashed incense stick name. Eg: Infernal magic incense sticks
    public String selectedStickName; // The actual incense stick name. Eg: Torstol incense sticks

    // Required for ImGui status count
    public long scriptStart;
    public int craftedLogCount = 0;
    public int ashedIncenseCount = 0;
    public int finishedIncenseCount = 0;

    // Set the bot state
    public BotState botState = BotState.STOPPED;

    enum BotState {
        // define your own states here
        IDLE,
        RUNNING,
        PROCESSING,
        STOPPED,
        BANKING,
    }

    @Override
    public boolean initialize() {
        scriptStart = System.currentTimeMillis();
        this.sgc = new StickMakerGraphicsContext(getConsole(), this);
        this.loopDelay = 590;

        // Inventory update subscription for script status
        subscribe(InventoryUpdateEvent.class, (event) -> {
            Item item = event.getNewItem();
            int itemid = item.getId();
            int craftedStickId = activeLogs.getBaseSticks().getItemId();
            int ashedStickId = activeAshedSticks.getItemId();
            int finalStickId = activeIncense.getItemId();
            if (item != null) {
                if (item.getInventoryType() != null && item.getInventoryType().getId() != 93) {
                    return;
                }
                if (itemid == craftedStickId) {
                    Item oldItem = event.getOldItem();
                    int newcount = item.getStackSize();
                    int oldcount = (oldItem != null && oldItem.getStackSize() > 1) ? oldItem.getStackSize() : 0;
                    if (newcount > oldcount) {
                        craftedLogCount++;
                    }
                }
                if (itemid == ashedStickId) {
                    Item oldItem = event.getOldItem();
                    int newcount = item.getStackSize();
                    int oldcount = (oldItem != null && oldItem.getStackSize() > 1) ? oldItem.getStackSize() : 0;
                    if (newcount > oldcount) {
                        ashedIncenseCount++;
                    }
                }
                if (itemid == finalStickId) {
                    Item oldItem = event.getOldItem();
                    int newcount = item.getStackSize();
                    println("NEW COUNT: " + newcount);
                    int oldcount = (oldItem != null && oldItem.getStackSize() > 1) ? oldItem.getStackSize() : 0;
                    println("old COUNT: " + oldcount);
                    if (newcount > oldcount) {
                        finishedIncenseCount++;
                    }
                }
            }
        });
        return super.initialize();
    }

    @Override
    public void onLoop() {
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.STOPPED) {
            Execution.delay(random.nextLong(3000, 4000));
            return;
        }

        // Set config
        selectedLogName = activeLogs.getItemName();
        selectedLogId = activeLogs.getItemId();
        selectedIncenseStickId = activeAshedSticks.getBaseSticks().getItemId();
        selectedIncenseStickName = activeAshedSticks.getBaseSticks().getItemName();
        selectedStickName = activeIncense.getItemName();
        selectedStickRawName = activeIncense.getAshedSticks().getItemName();
        selectedBaseStick = activeLogs.getBaseSticks().getItemName();

        if (DebugScript) {
            // Debuging use. Comment out prior to release.
            println("Current bank preset id " + currentPresetId);
            println("Selected function: " + SelectedFunction);
            println("selectedLogName: " + selectedLogName);
            println("selectedLogId: " + selectedLogId);
            println("selectedIncenseStickId: " + selectedIncenseStickId);
            println("selectedIncenseStickName: " + selectedIncenseStickName);
            println("selectedStickRawName: " + selectedStickRawName);
            println("selectedStickName: " + selectedStickName);
        }

        switch (botState) {
            case IDLE -> {
                println("Bot State | IDLE");
                // HandleIdle();
                Execution.delay(HandleIdle());
            }
            case STOPPED -> {
                println("Bot State | STOPPED");
                Execution.delay(random.nextLong(3000, 10000));
            }
            case RUNNING -> {
                println("Bot State | RUNNING");
                Execution.delay(HandleExec());
            }
            case BANKING -> {
                println("Bot State | BANKING");
                Execution.delay(HandleBanking());
            }
            case PROCESSING -> {
                println("Bot State | PROCESSING");
                Execution.delay(handleProcessing());
            }
        }
    }

    // Refactored state handlers
    private long HandleIdle() {
        println("HandleIdle");
        if (logout) {
            botState = BotState.STOPPED;
            Logout();
        }
        return random.nextLong(500, 1000);
    }

    private long HandleBanking() {
        println("HandleBankingState");
        LoadFromPreset(currentPresetId);
        ExecDelay();
        if (SelectedFunction == 0) {
            if (CheckForLogs()) {
                println("HandleBankingState | Logs found, transition to RUNNING.");
                botState = BotState.RUNNING;
                return random.nextLong(800, 1850);
            } else {
                println("HandleBankingState | Logs not found, transition to IDLE.");
                botState = BotState.IDLE;
            }
        } else if (SelectedFunction == 1) {
            if (HasSupplies()) {
                println("HandleBankingState | Supplies found, transition to RUNNING.");
                botState = BotState.RUNNING;
                return random.nextLong(800, 1850);
            } else {
                println("HandleBankingState | Supplies not found, transition to IDLE.");
                botState = BotState.IDLE;
            }
        } else if (SelectedFunction == 2) {
            if (HasHerbs()) {
                println("HandleBankingState | Herbs found, transition to RUNNING.");
                botState = BotState.RUNNING;
                return random.nextLong(800, 1850);
            } else {
                println("HandleBankingState | Herbs not found, transition to IDLE.");
                botState = BotState.IDLE;
            }
        }
        return random.nextLong(500, 1000);
    }

    private long handleProcessing() {
        println("HandleProcessing | Processing resources");
        boolean processing = WaitForProcessing(1251); // Interface ID of the 'item production' interface
        if (!processing) {
            println("HandleProcessing |  Waiting for interface to close");
        } else {
            botState = BotState.RUNNING;
            ExecDelay();
        }
        return random.nextLong(800, 1500);
    }

    public long HandleExec() {
        if (SelectedFunction == 0) {
            println("Selection 0: Processing logs");
            // Check if logs are available in inventory
            HasLogs = CheckForLogs();

            if (!HasLogs) {
                // Attempt to load bank preset
                botState = BotState.BANKING;
                // return random.nextLong(1000, 3000);
                return random.nextLong(850, 2145);
            } else {
                println("HandleExec | Crafting logs");
                CraftLogs(selectedLogId);
            }
        } else if (SelectedFunction == 1) {
            println("Selection 1: Processing incense sticks");
            HasSupplies = HasSupplies();

            if (!HasSupplies) {
                // Attempt to load bank preset
                botState = BotState.BANKING;
                // return random.nextLong(1000, 3000);
                return random.nextLong(850, 2145);
            } else {
                println("HandleExec | Process incense sticks");
                CoatSticks(selectedIncenseStickId);
            }
        } else if (SelectedFunction == 2) {
            println("Selection 2: Processing final incense sticks");
            HasSupplies = HasHerbs();

            if (!HasSupplies) {
                // Attempt to load bank preset
                botState = BotState.BANKING;
                return random.nextLong(804, 2145);
            } else {
                println("HandleExec | Adding herb to incense sticks");
                HerbSticks(selectedStickRawName);
            }
        }
        // return random.nextLong(1500, 4500);
        return random.nextLong(856, 2133);

    }

    // Helper methods

    public void Logout() {
        println("Logout | Logout initiated with a 10 second delay.");
        Execution.delay(10000);
        Component logout = ComponentQuery.newQuery(1433).componentIndex(71).results().first();
        ExecDelay();
        if (logout != null) {
            logout.interact(1);
            println("Logout | Logging out of account");
        } else {
            println("Logout | Logout failed");
        }
    }

    public void LoadFromPreset(int PresetNumber) {
        println("LoadFromPreset | Attempting to open the bank");
        // Attempt to open the bank, wait until it's open
        Bank.open();
        boolean bankOpened = Execution.delayUntil(timeout, Bank::isOpen);
        if (bankOpened) {
            println("LoadFromPreset | Bank is open, selecting preset: " + PresetNumber);
            Bank.loadPreset(PresetNumber);
            boolean bankClosed = Execution.delayUntil(timeout, () -> !Bank.isOpen());
            if (bankClosed) {
                println("LoadFromPreset | Preset loaded and bank closed successfully.");
            } else {
                println("LoadFromPreset | Timeout waiting for bank to close.");
            }
        } else {
            println("LoadFromPreset | Failed to open bank within the timeout period.");
        }
    }

    public void ExecDelay() {
        int delay = RandomGenerator.nextInt(400, 800);
        Execution.delay(delay);
    }

    public boolean CheckInterface(int InterfaceId) {
        boolean IsOpen = Interfaces.isOpen(InterfaceId);
        return IsOpen;
    }

    public boolean WaitForProcessing(int InterfaceId) {
        boolean completed = false;
        if (!completed) {
            boolean checkinterface = CheckInterface(InterfaceId);
            Execution.delay(500);
            if (!checkinterface) {
                completed = true;
                println("ActionCompleted | Interface has closed: " + InterfaceId);
                return true;
            }
        }
        return false;
    }

    public int GetInvLogCount(String LogName) {
        int log = InventoryItemQuery.newQuery(93).name(LogName).results().size();
        return log;
    }

    public int GetInvItemCount(String ItemName) {
        int itemcount = InventoryItemQuery.newQuery(93).name(ItemName).results().size();
        return itemcount;
    }

    public boolean HasSupplies() {
        int stickprecheck = InventoryItemQuery.newQuery(93).name(selectedIncenseStickName).results().size();
        println("HasSupplies | pre check for stack: " + stickprecheck);
        if (stickprecheck <= 0) {
            println("HasSupplies | No incense sticks found");
            return false;
        } else {
            if (!CheckForAshes()) {
                println("HasSupplies | Incompatible ashes detected.");
                return false;
            }
            Execution.delay(300);
            int sticks = InventoryItemQuery.newQuery(93).name(selectedIncenseStickName).results().first()
                    .getStackSize();
            println("HasSupplies | stack size: " + sticks);
            Execution.delay(300);
            int ashid = activeAshedSticks.getAshType().getItemId();
            println("Ash ID : " + ashid);
            if (ashid <= 0) {
                return false;
            } else {
                int ash = InventoryItemQuery.newQuery(93).ids(ashid).results().size();
                if (sticks >= 1 && ash >= 2) {
                    println("HasSupplies | Enough items present | Sticks: " + sticks + "| Ashes: " + ash);
                    return true;
                } else {
                    println("HasSupplies | Not enough items | Sticks: " + sticks + "| Ashes: " + ash);
                    return false;
                }
            }
        }
    }

    public boolean HasHerbs() {
        ExecDelay();
        int stickprecheck = InventoryItemQuery.newQuery(93).name(selectedStickRawName).results().size();
        println("HasHerbs | pre check for stack: " + stickprecheck);
        if (stickprecheck <= 0) {
            println("HasHerbs | No incense sticks found");
            return false;
        } else {
            if (!CheckForHerb(selectedStickName)) {
                println("HasHerbs | Incompatible herb detected.");
                return false;
            }
            Execution.delay(300);
            int sticks = InventoryItemQuery.newQuery(93).name(selectedStickRawName).results().first()
                    .getStackSize();
            println("HasHerbs | Enough items present | Sticks: " + sticks);
            Execution.delay(300);
            int herbid = activeIncense.getSelectedHerbs().getItemId();
            if (herbid <= 0) {
                return false;
            } else {
                Execution.delay(300);
                int herb = InventoryItemQuery.newQuery(93).ids(herbid).results().size();
                {
                    if (sticks >= 1 && herb >= 1) {
                        println("HasHerbs | Enough items present | Sticks: " + sticks + "| Herbs:" + herb);
                        return true;
                    } else {
                        println("HasHerbs | Not enough items | Sticks: " + sticks + "| Herbs: " + herb);
                        return false;
                    }
                }
            }
        }
    }

    public boolean CheckForLogs() {
        int log = GetInvLogCount(selectedLogName);
        if (log >= 2) {
            println("checkForLogs | Logs exist");
            return true;
        } else {
            println("checkForLogs | Not enough logs to make the incense. Count:" + " " + log);
            return false;
        }
    }

    public boolean CheckForItem(int ItemId) {
        ExecDelay();
        boolean IsPresent = Backpack.contains(ItemId);
        if (IsPresent) {
            println("CheckForItem | Item exists: " + ItemId);
            return true;
        } else {
            println("CheckForItem | Item does not exist : " + ItemId);
            return false;
        }
    }

    public boolean CheckForItemStr(String ItemName) {
        ExecDelay();
        boolean IsPresent = Backpack.contains(ItemName);
        if (IsPresent) {
            println("CheckForItem | Item exists: " + ItemName);
            return true;
        } else {
            println("CheckForItem | Item does not exist : " + ItemName);
            return false;
        }
    }

    public boolean CheckForAshes() {
        int AshId = activeAshedSticks.getAshType().getItemId();
        println("CheckForAshes | Ash ID " + AshId);
        if (!CheckForItem(AshId)) {
            // Required ash is not present, halt
            println("CheckForAshes | Required ash not found for incense: " + selectedIncenseStickName);
            return false;
        } else {
            println("CheckForAshes | Required ash was found " + selectedIncenseStickName);
            return true;
        }
    }

    public boolean CheckForHerb(String IncenseStickName) {
        int HerbId = activeIncense.getSelectedHerbs().getItemId();
        if (!CheckForItem(HerbId)) {
            // Required ash is not present, halt
            println("CheckForHerb | Required herb not found for incense: " + IncenseStickName);
            return false;
        } else {
            println("CheckForHerb | Required herb present: " + IncenseStickName);
            return true;
        }
    }

    public boolean CheckForSticks() {
        Execution.delay(200);
        int count = GetInvItemCount(selectedIncenseStickName);
        ExecDelay();
        if (count >= 1) {
            println("CheckForSticks | Incense sticks found.");
            return true;
        } else {
            println("CheckForSticks | None found. Count:" + " " + count);
            return false;
        }
    }

    // Section 0: Crafting logs into incense sticks
    public void CraftLogs(int LogItemId) {
        Component craftlog1 = ComponentQuery.newQuery(1473).item(LogItemId).results().first();
        ExecDelay();
        boolean craftsuccess = craftlog1.interact("Craft");
        ExecDelay();
        if (craftsuccess) {
            println("craftLogs | Opening Craft log dialog");
            boolean actionselectinterface = Interfaces.isOpen(1179);
            ExecDelay();
            if (actionselectinterface) {
                println("CraftLogs | Action select interface detected");
                Component craftlog2 = ComponentQuery.newQuery(1179).componentIndex(32).results().first();
                if (craftlog2 != null) {
                    ExecDelay();
                    println("CraftLogs | craftlog2 attempting second stage dialog");
                    boolean craftlog2comp = MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 77266976);
                    Execution.delayUntil(timeout, () -> !Interfaces.isOpen(1179));
                    ExecDelay();
                    if (!craftlog2comp) {
                        println("CraftLogs | craftlog2comp failed selection dialog");
                        botState = BotState.IDLE;
                        return;
                    }
                }
            } else {
                println("CraftLogs | Processing");
            }
        } else {
            println("CraftLogs | craftlog3 dialog not initiated correctly");
            botState = BotState.IDLE;
            return;
        }
        // Always select make
        ExecDelay();
        Component craftlog3 = ComponentQuery.newQuery(1370).componentIndex(30).results().first();
        if (craftlog3 != null) {
            println("craftLogs | Incense make dialog");
            boolean execute = MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
            ExecDelay();
            if (execute) {
                println("craftLogs | Making incense sticks");
                botState = BotState.PROCESSING;
                return;
            }
        } else {
            println("craftLogs | craftlog3 dialog was not found.");
            botState = BotState.IDLE;
            return;
        }
    }

    // Section 1: Adding ash to incense sticks
    public void CoatSticks(int IncenseStickId) {
        Component coatsticks1 = ComponentQuery.newQuery(1473).itemName(selectedIncenseStickName).results().first();
        ExecDelay();
        boolean coatsuccess = coatsticks1.interact("Coat");
        ExecDelay();
        if (coatsuccess) {
            Component coatsticks2 = ComponentQuery.newQuery(1370).componentIndex(30).results().first();
            ExecDelay();
            if (coatsticks2 != null) {
                println("CoatSticks | Coat incense dialog");
                boolean execute = MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
                ExecDelay();
                if (execute) {
                    println("CoatSticks | Making incense sticks");
                    botState = BotState.PROCESSING;
                    return;
                }
            } else {
                println("CoatLogs | Coat logs2 dialog was not found.");
                botState = BotState.IDLE;
                return;
            }
        } else {
            println("CoatLogs | Coat logs dialog not initiated correctly");
            botState = BotState.IDLE;
            return;
        }
    }

    // Section 2: Adding herb to ashed sticks
    public void HerbSticks(String selectedStickName) {
        Component herbsticks1 = ComponentQuery.newQuery(1473).itemName(selectedStickName).results().first();
        ExecDelay();
        boolean herbsuccess = herbsticks1.interact("Add herb");
        ExecDelay();
        if (herbsuccess) {
            Component herbsticks2 = ComponentQuery.newQuery(1370).componentIndex(30).results().first();
            ExecDelay();
            if (herbsticks2 != null) {
                println("HerbSticks | Herb sticks dialog");
                boolean execute = MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
                ExecDelay();
                if (execute) {
                    println("HerbSticks | Loading herb sticks");
                    botState = BotState.PROCESSING;
                    return;
                }
            } else {
                println("HerbSticks | Herb logs2 dialog was not found.");
                botState = BotState.IDLE;
                return;
            }
        } else {
            println("HerbSticks | Herb sticks dialog not initiated correctly");
            botState = BotState.IDLE;
            return;
        }
    }

    // Thank u papi Cipher for the bot state logic
    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    // Enum helpers
    public void setActiveLogs(Logs log) {
        println("setActiveLogs");
        this.activeLogs = log;
        this.selectedLogName = log.getItemName();
        this.selectedLogId = log.getItemId();
    }

    public Logs getLogs(Logs log) {
        println("getLogs");
        return this.activeLogs;
    }

    public void setActiveIncense(Incense incense) {
        println("setActiveIncense");
        this.activeIncense = incense;
        this.selectedStickName = incense.getItemName();
    }

    public Incense getIncense(Incense incense) {
        println("getIncense");
        return this.activeIncense;
    }

    public void setActiveAshedSticks(AshedSticks stick) {
        println("setActiveAshedSticks");
        this.activeAshedSticks = stick;
        this.selectedIncenseStickName = stick.getBaseSticks().getItemName();
        this.selectedIncenseStickId = stick.getBaseSticks().getItemId();
    }

    public AshedSticks getAshedSticks(AshedSticks stick) {
        println("getAshedSticks");
        return this.activeAshedSticks;
    }
}