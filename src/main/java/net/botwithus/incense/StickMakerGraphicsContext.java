package net.botwithus.incense;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class StickMakerGraphicsContext extends ScriptGraphicsContext {

    private final StickMaker script;

    // Vars required
    // Does initial/current item check
    private Map<String, Integer> itemlist = new LinkedHashMap<>(); // Log list for crafting incense sticks
    private Map<String, Integer> ashlist = new LinkedHashMap<>(); // Ash list
    private Map<String, Integer> rawlist = new LinkedHashMap<>(); // Raw stick list
    private Map<String, Integer> presetlist = new LinkedHashMap<>(); // Stick craft list (1)
    private Map<String, String> StickMap = new HashMap<>();

    public boolean Logout = false;

    private String currentLogName; // Default selected item
    private int currentLogId; // This will be set based on the selection

    private String[] scriptOptions = { "Craft incense sticks", "Ash incense sticks", "Herb incense sticks" };
    private int selectedScriptOption = 0; // Default to "Craft incense sticks"
    private String currentIncenseStick;
    private int currentIncenseStickId;
    private String currentAshesName;
    private int currentAshesId;
    private String currentStickName;
    private String currentStickRawName;

    // Vars for bank presets
    private String currentBankPreset; // Default selected bank preset
    private int currentBankPresetId; // Default selected bank preset

    public StickMakerGraphicsContext(ScriptConsole console, StickMaker script) {
        super(console);
        this.script = script;

        // Define bank preset numbers
        presetlist.put("Preset 1", 1);
        presetlist.put("Preset 2", 2);
        presetlist.put("Preset 3", 3);
        presetlist.put("Preset 4", 4);
        presetlist.put("Preset 5", 5);
        presetlist.put("Preset 6", 6);
        presetlist.put("Preset 7", 7);
        presetlist.put("Preset 8", 8);
        presetlist.put("Preset 9", 9);

        currentBankPreset = "Preset 1";
        currentBankPresetId = presetlist.get(currentBankPreset);
        script.currentPresetId = currentBankPresetId;

        // Phase 1: Crafting incense sticks
        itemlist.put("Logs", 1511);
        itemlist.put("Oak logs", 1521);
        itemlist.put("Willow logs", 1519);
        itemlist.put("Maple logs", 1517);
        itemlist.put("Acadia logs", 40285);
        itemlist.put("Yew logs", 1515);
        itemlist.put("Magic logs", 1513);

        // Set the default item ID
        currentLogName = "Logs";
        currentLogId = itemlist.get(currentLogName);
        script.selectedLogId = currentLogId;
        script.selectedLogName = currentLogName;

        // Ashes
        ashlist.put("Impious ashes", 20264);
        ashlist.put("Accursed ashes", 20266);
        ashlist.put("Infernal ashes", 20268);

        currentAshesName = "Impious ashes";
        currentAshesId = ashlist.get(currentAshesName);
        script.selectedAshesId = currentAshesId;
        script.selectedAshesName = currentAshesName;

        // Raw incense sticks
        rawlist.put("Wooden incense sticks", 47685);
        rawlist.put("Oak incense sticks", 47686);
        rawlist.put("Willow incense sticks", 47687);
        rawlist.put("Acadia incense sticks", 47689);
        rawlist.put("Maple incense sticks", 47688);
        rawlist.put("Yew incense sticks", 47690);
        rawlist.put("Magic incense sticks", 47691);

        // Set the default item ID
        currentIncenseStick = "Wooden incense sticks";
        currentIncenseStickId = rawlist.get(currentIncenseStick);
        script.selectedIncenseStickId = currentIncenseStickId;
        script.selectedIncenseStickName = currentIncenseStick;

        // Stick map for raw product > completed product mapping
        StickMap.put("Guam incense sticks", "Impious incense sticks");
        StickMap.put("Tarromin incense sticks", "Impious incense sticks");
        StickMap.put("Marrentill incense sticks", "Impious incense sticks");
        StickMap.put("Harralander incense sticks", "Impious oak incense sticks");
        StickMap.put("Ranarr incense sticks", "Impious oak incense sticks");
        StickMap.put("Toadflax incense sticks", "Impious willow incense sticks");
        StickMap.put("Spirit weed incense sticks", "Impious willow incense sticks");
        StickMap.put("Irit incense sticks", "Accursed maple incense sticks");
        StickMap.put("Wergali incense sticks", "Accursed maple incense sticks");
        StickMap.put("Avantoe incense sticks", "Accursed acadia incense sticks");
        StickMap.put("Kwuarm incense sticks", "Accursed acadia incense sticks");
        StickMap.put("Bloodweed incense sticks", "Accursed acadia incense sticks");
        StickMap.put("Snapdragon incense sticks", "Accursed acadia incense sticks");
        StickMap.put("Cadantine incense sticks", "Infernal yew incense sticks");
        StickMap.put("Lantadyme incense sticks", "Infernal yew incense sticks");
        StickMap.put("Dwarf weed incense sticks", "Infernal yew incense sticks");
        StickMap.put("Torstol incense sticks", "Infernal magic incense sticks");
        StickMap.put("Fellstalk incense sticks", "Infernal magic incense sticks");

        // Set the default item ID
        currentStickName = "Guam incense sticks";
        currentStickRawName = StickMap.get(currentStickName);
        script.selectedStickName = currentStickName;
        script.selectedStickRawName = currentStickRawName;
        script.logout = Logout;
    }

    private String result = "";

    @Override
    public void drawSettings() {
        ImGui.SetWindowSize(200.f, 200.f);
        if (ImGui.Begin("Incense Maker Config", 0)) {
            if (ImGui.Button("Start Script")) {
                script.println("Start script button selected.");
                // script.runScript = true;
                script.setBotState(script.botState.RUNNING);
            }
            ImGui.SameLine();
            if (ImGui.Button("Stop Script")) {
                script.println("Stop script button selected.");
                // script.runScript = false;
                // script.setBotState(script.botState.IDLE);
                script.setBotState(script.botState.STOPPED);
            }
            ImGui.SameLine();
            script.logout = ImGui.Checkbox("Logout on Completion", script.logout);
            ImGui.Text("Bot Status: " + script.getBotState());
            NativeInteger currentPresetIndex = new NativeInteger(findIndexForPreset(currentBankPreset));
            String[] bplist = presetlist.keySet().toArray(new String[0]);
            if (ImGui.Combo("Select Bank Preset", currentPresetIndex, bplist)) {
                currentBankPreset = bplist[currentPresetIndex.get()];
                currentBankPresetId = presetlist.get(currentBankPreset);
                script.currentPresetId = currentBankPresetId;
            }
            NativeInteger scriptOptionIndex = new NativeInteger(selectedScriptOption);
            if (ImGui.Combo("Select Script Option", scriptOptionIndex, scriptOptions)) {
                selectedScriptOption = scriptOptionIndex.get();
                script.SelectedFunction = selectedScriptOption;
            }
            switch (selectedScriptOption) {
                case 0: // "Craft incense sticks"
                    renderLogDropDown();
                    break;
                case 1: // "Ash incense sticks"
                    renderStickDropDown();
                    break;
                case 2: // "Herb incense sticks"
                    renderHerbDropDown();
                    break;
            }
        }
        ImGui.End();
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }

    // Render methods to dynamically display relevent menu items
    // Logs
    private void renderLogDropDown() {
        NativeInteger currentIndex = new NativeInteger(findIndexForItemName(currentLogName));
        String[] items = itemlist.keySet().toArray(new String[0]);
        if (ImGui.Combo("Select Logs", currentIndex, items)) {
            currentLogName = items[currentIndex.get()];
            currentLogId = itemlist.get(currentLogName);
            script.selectedLogId = currentLogId;
            script.selectedLogName = currentLogName;
        }
    }

    // Incense sticks
    private void renderStickDropDown() {
        NativeInteger currentIncenseIndex = new NativeInteger(findIndexForSticks(currentIncenseStick));
        String[] items = rawlist.keySet().toArray(new String[0]);
        if (ImGui.Combo("Select Incense Stick", currentIncenseIndex, items)) {
            currentIncenseStick = items[currentIncenseIndex.get()];
            currentIncenseStickId = rawlist.get(currentIncenseStick);
            script.selectedIncenseStickId = currentIncenseStickId;
            script.selectedIncenseStickName = currentIncenseStick;
        }
    }

    // Herbs
    private void renderHerbDropDown() {
        NativeInteger currentStickIndex = new NativeInteger(findIndexForHerb(currentStickName));
        String[] items = StickMap.keySet().toArray(new String[0]);
        if (ImGui.Combo("Select Incense Stick", currentStickIndex, items)) {
            currentStickName = items[currentStickIndex.get()];
            currentStickRawName = StickMap.get(currentStickName);
            script.selectedStickRawName = currentStickRawName;
            script.selectedStickName = currentStickName;
        }
    }

    // Helper methods to find the index of a given item name from my dodgy result style
    // Pls don't laugh.
    private int findIndexForItemName(String itemName) {
        Set<String> keys = itemlist.keySet();
        int index = 0;
        for (String key : keys) {
            if (key.equals(itemName)) {
                return index;
            }
            index++;
        }
        return -1; // Should not happen if itemName is valid
    }

    private int findIndexForSticks(String itemName) {
        Set<String> keys = rawlist.keySet();
        int index = 0;
        for (String key : keys) {
            if (key.equals(itemName)) {
                return index;
            }
            index++;
        }
        return -1; // Should not happen if itemName is valid
    }

    private int findIndexForHerb(String itemName) {
        Set<String> keys = StickMap.keySet();
        int index = 0;
        for (String key : keys) {
            if (key.equals(itemName)) {
                return index;
            }
            index++;
        }
        return -1; // Should not happen if itemName is valid
    }

    private int findIndexForPreset(String presetName) {
        Set<String> keys = presetlist.keySet();
        int index = 0;
        for (String key : keys) {
            if (key.equals(presetName)) {
                return index;
            }
            index++;
        }
        return -1; // Should not happen if presetName is valid
    }

}