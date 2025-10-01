package net.botwithus.incense;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.InventoryUpdateEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.configs.ConfigManager;
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
import java.util.LinkedList;
import java.util.Arrays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    public long timeout = 9000;
    private Random random = new Random();
    public boolean logout = false;
    public boolean DebugScript = false;
    public boolean useLastPreset = false;
    public Incense activeIncense;

    // Task queue bits
    public final LinkedList<Task> taskQueue = new LinkedList<>();
    public Task activeTask = null;
    private final Object taskLock = new Object();
    public int newTaskCount = 1;
    public int newTaskPhase = 0;
    public int newTaskPreset = 1;

    // Refactored the cunt
    public int pIncenseId = 0;
    public int sIncenseId = 0;
    public int stickId = 0;
    public int ashesId = 0;
    public int fHerbId = 0;
    public int logsId = 0;

    public String selectedStickName;

    // Required for ImGui status count
    public long scriptStart;
    public int craftedLogCount = 0;
    public int ashedIncenseCount = 0;
    public int finishedIncenseCount = 0;

    // Set the bot state
    public BotState botState = BotState.STOPPED;

    enum BotState {
        IDLE,
        RUNNING,
        STOPPED,
        BANKING,
    }

    @Override
    public boolean initialize() {
        scriptStart = System.currentTimeMillis();
        this.sgc = new StickMakerGraphicsContext(getConsole(), this);
        this.loopDelay = 590;
        loadConfig();

        // Subscribe to inventory updates to track crafted items
        subscribe(InventoryUpdateEvent.class, event -> {
            if (botState == BotState.RUNNING && event.getInventoryId() == 93 && activeTask != null) {
                synchronized (taskLock) {
                    switch (activeTask.getPhaseId()) {
                        case 0: // Craft logs
                            if (event.getNewItem().getId() == stickId) {
                                activeTask.incrementCompleted(1);
                                craftedLogCount++;
                            }
                            break;
                        case 1: // Ash sticks
                            if (event.getNewItem().getId() == sIncenseId) {
                                activeTask.incrementCompleted(1);
                                ashedIncenseCount++;
                            }
                            break;
                        case 2: // Herb sticks
                            if (event.getNewItem().getId() == pIncenseId) {
                                activeTask.incrementCompleted(1);
                                finishedIncenseCount++;
                            }
                            break;
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
        saveConfig();

        if (DebugScript) {
            // Debuging use. Comment out prior to release.
            println("Current bank preset id: " + currentPresetId);
            println("Parent incense stick ID: " + pIncenseId);
            println("Secondary incense stick ID: " + sIncenseId);
            println("Herb ID: " + fHerbId);
            println("Ashes ID: " + ashesId);
            println("Stick ID: " + stickId);
            println("Logs ID: " + logsId);
            if (activeTask != null) {
                if (DebugScript) {
                    println("Active task: " + activeTask.getTaskName() + " | Action: " + activeTask.getPhaseName() + " | Progress: " + activeTask.getCompletedCount() + "/" + activeTask.getTargetCount());
                }
            }
        }

        switch (botState) {
            case IDLE -> {
                println("Bot State | IDLE");
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
        }
    }

    // Refactored state handlers
    private long HandleIdle() {
        if (logout) {
            botState = BotState.STOPPED;
            Logout();
        }
        botState = BotState.STOPPED;
        return random.nextLong(500, 1000);
    }

    private long HandleBanking() {
        println("HandleBankingState");
        int presetToUse = currentPresetId;
        boolean shouldUseLastPreset = useLastPreset;
        boolean isFirstRun = false;

        synchronized (taskLock) {
            if (activeTask != null) {
                presetToUse = activeTask.getBankPresetId();
                isFirstRun = activeTask.isFirstRun();
                if (isFirstRun) {
                    println("HandleBankingState | First run of task, using preset: " + presetToUse);
                    shouldUseLastPreset = false;
                    activeTask.setFirstRunComplete();
                } else {
                    shouldUseLastPreset = useLastPreset;
                }
            }
        }

        // Load from preset
        if (!shouldUseLastPreset) {
            LoadFromPreset(presetToUse);
        } else {
            Execution.delay(loadFromLastPreset());
        }

        // Check supplies based on current function
        switch (SelectedFunction) {
            case 0 -> {
                if (hasLogs(logsId)) {
                    println("HandleBankingState | Logs found, transition to RUNNING.");
                    botState = BotState.RUNNING;
                } else {
                    println("HandleBankingState | Logs not found, transition to IDLE.");
                    botState = BotState.IDLE;
                }
            }
            case 1 -> {
                if (canCoat()) {
                    println("HandleBankingState | Supplies found, transition to RUNNING.");
                    botState = BotState.RUNNING;
                } else {
                    println("HandleBankingState | Supplies not found, transition to IDLE.");
                    botState = BotState.IDLE;
                }
            }
            case 2 -> {
                if (canHerb()) {
                    println("HandleBankingState | Herbs found, transition to RUNNING.");
                    botState = BotState.RUNNING;
                } else {
                    println("HandleBankingState | Herbs not found, transition to IDLE.");
                    botState = BotState.IDLE;
                }
            }
        }
        return random.nextLong(500, 1000);
    }

    public long HandleExec() {
        synchronized (taskLock) {
            if (activeTask != null && activeTask.isCompleted()) {
                println("HandleExec | Task completed: " + activeTask.getTaskName());
                taskQueue.poll();
                activeTask = null;
            }

            if (activeTask == null && !taskQueue.isEmpty()) {
                activeTask = taskQueue.peek();
                if (activeTask != null) {
                    println("HandleExec | Starting new task: " + activeTask.getTaskName() + " | Action: " + activeTask.getPhaseName());
                    SelectedFunction = activeTask.getPhaseId();
                    if (activeTask.getIncense() != null) {
                        setActiveIncense(activeTask.getIncense());
                        setIncenseConfig();
                    }
                    botState = BotState.BANKING;
                    return random.nextLong(500, 1000);
                }
            }

            if (activeTask == null && taskQueue.isEmpty()) {
                println("HandleExec | No tasks in queue, stopping.");
                botState = BotState.IDLE;
                return random.nextLong(500, 1000);
            }
        }

        switch (SelectedFunction) {
            case 0 -> {
                println("Selection 0: Processing logs");
                if (!hasLogs(logsId)) {
                    botState = BotState.BANKING;
                    return random.nextLong(850, 1300);
                } else {
                    return craftLogs(logsId);
                }
            }
            case 1 -> {
                println("Selection 1: Processing incense sticks");
                if (!canCoat()) {
                    botState = BotState.BANKING;
                    return random.nextLong(850, 1200);
                } else {
                    return coatSticks(stickId);
                }
            }
            case 2 -> {
                println("Selection 2: Processing final incense sticks");
                if (!canHerb()) {
                    botState = BotState.BANKING;
                    return random.nextLong(804, 1200);
                } else {
                    println("HandleExec | Adding herb to incense sticks");
                    return herbSticks(sIncenseId);
                }
            }
        }
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

    private long loadFromLastPreset() {
        Execution.delayUntil(30000, () -> Bank.loadLastPreset());
        return random.nextLong(550, 1200);
    }

    public void ExecDelay() {
        int delay = RandomGenerator.nextInt(400, 800);
        Execution.delay(delay);
    }

    public void waitForProcessing(int InterfaceId) {
        while (Interfaces.isOpen(InterfaceId)) {
            if (DebugScript) {
                println("WaitForProcessing | Waiting for the interface to close: " + InterfaceId);
            }
            ExecDelay();
        }
    }

    public boolean canCoat() {
        int sticks = InventoryItemQuery.newQuery(93).ids(stickId).results().first()
                .getStackSize();
        int ashcount = Backpack.getCount(ashesId);
        println("canCoat | stick count: " + sticks + " | ash count: " + ashcount);
        return sticks >= 2 && ashcount >= 3;
    }

    public boolean canHerb() {
        int sticks = InventoryItemQuery.newQuery(93).ids(sIncenseId).results().first()
                .getStackSize();
        int herbcount = Backpack.getQuantity(fHerbId);
        println("canHerb | stick count: " + sticks + " | herb count: " + herbcount);
        return sticks >= 1 && herbcount >= 1;
    }

    public boolean hasLogs(int logId) {
        int inBackpackC = Backpack.getCount(logId);
        if (inBackpackC < 2) {
            println("hasLogs | Not enough logs: " + inBackpackC);
            return false;
        } else {
            println("hasLogs | Enough logs present");
            return true;
        }
    }

    // Section 0: Crafting logs into incense sticks
    public long craftLogs(int itemId) {
        Component craftlog1 = ComponentQuery.newQuery(1473).item(itemId).results().first();
        if (craftlog1 != null && craftlog1.interact("Craft")) {
            println("craftLogs | Opening Craft log dialog");
            ExecDelay();
            if (Interfaces.isOpen(1179)) {
                println("craftLogs | Action select interface detected");
                Component craftlog2 = ComponentQuery.newQuery(1179).componentIndex(32).results().first();
                if (craftlog2 != null) {
                    println("craftLogs | craftlog2 attempting second stage dialog");
                    MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 77266976);
                    Execution.delayUntil(timeout, () -> !Interfaces.isOpen(1179));
                }
            } else {
                println("craftLogs | Processing");
            }
        } else {
            println("craftLogs | craftlog3 dialog not initiated correctly");
            botState = BotState.IDLE;
            return random.nextLong(550, 1200);
        }
        Component craftlog3 = ComponentQuery.newQuery(1370).componentIndex(30).results().first();
        if (craftlog3 != null) {
            println("craftLogs | Incense make dialog");
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
            Execution.delayUntil(timeout, () -> Interfaces.isOpen(1251));
            println("craftLogs | Making incense sticks");
            waitForProcessing(1251);
        } else {
            println("craftLogs | craftlog3 dialog was not found.");
            botState = BotState.IDLE;
        }
        return random.nextLong(550, 1200);
    }

    // Section 1: Adding ash to incense sticks
    public long coatSticks(int IncenseStickId) {
        Component coatsticks1 = ComponentQuery.newQuery(1473).item(IncenseStickId).results().first();
        if (coatsticks1 != null && coatsticks1.interact("Coat")) {
            Execution.delayUntil(timeout, () -> Interfaces.isOpen(1370));
            Component coatsticks2 = ComponentQuery.newQuery(1370).componentIndex(30).results().first();
            if (coatsticks2 != null) {
                println("CoatSticks | Making incense sticks");
                MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
                Execution.delayUntil(timeout, () -> Interfaces.isOpen(1251));
                waitForProcessing(1251);
            } else {
                println("CoatLogs | Coat logs2 dialog was not found.");
                botState = BotState.IDLE;
            }
        }
        return random.nextLong(550, 1200);
    }

    // Section 2: Adding herb to incense sticks
    public long herbSticks(int IncenseStickId) {
        Component herbsticks1 = ComponentQuery.newQuery(1473).item(IncenseStickId).results().first();
        if (herbsticks1 != null && herbsticks1.interact("Add herb")) {
            Execution.delayUntil(timeout, () -> Interfaces.isOpen(1370));
            Component herbsticks2 = ComponentQuery.newQuery(1370).componentIndex(30).results().first();
            if (herbsticks2 != null) {
                println("HerbSticks | Herb sticks dialog");
                MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
                Execution.delayUntil(timeout, () -> Interfaces.isOpen(1251));
                waitForProcessing(1251);
            } else {
                println("HerbSticks | Herb logs2 dialog was not found.");
                botState = BotState.IDLE;
            }
        } else {
            println("HerbSticks | Herb sticks dialog not initiated correctly");
            botState = BotState.IDLE;
        }
        return random.nextLong(550, 1200);
    }

    // Thank u papi Cipher for the bot state logic
    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public void setActiveIncense(Incense incense) {
        println("setActiveIncense");
        this.activeIncense = incense;
        this.selectedStickName = incense.getItemName();
    }

    public void setIncenseConfig() {
        this.pIncenseId = activeIncense.getItemId();
        this.fHerbId = ConfigManager.getItemType(pIncenseId).getIntParam(2655);
        this.sIncenseId = ConfigManager.getItemType(pIncenseId).getIntParam(2656);
        this.stickId = ConfigManager.getItemType(sIncenseId).getIntParam(2655);
        this.ashesId = ConfigManager.getItemType(sIncenseId).getIntParam(2656);
        this.logsId = ConfigManager.getItemType(stickId).getIntParam(2655);
        println("setIncenseConfig | Incense ID: " + pIncenseId + " | Secondary Incense ID: " + sIncenseId + " | Herb ID: " + fHerbId + " | Stick ID: " + stickId
                + " | Ashes ID: " + ashesId + " | Logs ID: " + logsId);
    }

    public Incense getIncense(Incense incense) {
        println("getIncense");
        return this.activeIncense;
    }

    // Task queue methods
    public void addTask(Task task) {
        synchronized (taskLock) {
            taskQueue.add(task);
            println("addTask | Task added: " + task.getTaskName() + " | Queue size: " + taskQueue.size());
        }
    }

    public void removeTask(int index) {
        synchronized (taskLock) {
            if (index >= 0 && index < taskQueue.size()) {
                Task removed = taskQueue.remove(index);
                println("removeTask | Task removed: " + removed.getTaskName());
                if (activeTask == removed) {
                    activeTask = null;
                }
            }
        }
    }

    public void clearTaskQueue() {
        synchronized (taskLock) {
            taskQueue.clear();
            activeTask = null;
            println("clearTaskQueue | Task queue cleared");
        }
    }

    // Configuration persistence
    public void saveConfig() {
        configuration.addProperty("Debugging", String.valueOf(DebugScript));
        configuration.addProperty("useLastPreset", String.valueOf(useLastPreset));
        configuration.addProperty("LogoutOnCompletion", String.valueOf(logout));
        configuration.addProperty("currentPresetId", String.valueOf(currentPresetId));

        synchronized (taskLock) {
            Gson gson = new GsonBuilder().create();
            String tasksJson = gson.toJson(taskQueue);
            configuration.addProperty("taskQueue", tasksJson);
        }

        if (DebugScript) {
            println("saveConfig | Saving script config");
        }
        configuration.save();
    }

    public void loadConfig() {
        try {
            DebugScript = Boolean.parseBoolean(configuration.getProperty("Debugging"));
            logout = Boolean.parseBoolean(configuration.getProperty("LogoutOnCompletion"));
            useLastPreset = Boolean.parseBoolean(configuration.getProperty("useLastPreset"));

            String currentPresetIdValue = configuration.getProperty("currentPresetId");
            if (currentPresetIdValue != null && !currentPresetIdValue.isEmpty()) {
                currentPresetId = Integer.parseInt(currentPresetIdValue);
            } else {
                currentPresetId = 1;
            }

            String tasksJson = configuration.getProperty("taskQueue");
            if (tasksJson != null && !tasksJson.isEmpty()) {
                synchronized (taskLock) {
                    Gson gson = new GsonBuilder().create();
                    Task[] tasks = gson.fromJson(tasksJson, Task[].class);
                    taskQueue.clear();
                    taskQueue.addAll(Arrays.asList(tasks));
                }
            }

            if (DebugScript) {
                println("loadConfig | Script config loaded successfully");
            }
        } catch (Exception e) {
            println("loadConfig | Failed to load script config: " + e.getMessage());
        }
    }
}
