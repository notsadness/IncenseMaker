package net.botwithus.incense;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

public class StickMakerGraphicsContext extends ScriptGraphicsContext {

    private final StickMaker script;

    // Indexes
    public NativeInteger LogIndex;
    public NativeInteger BaseStickIndex;
    public NativeInteger IncenseIndex;
    public NativeInteger AshedStickIndex;
    private int selectedLogIndex = 0;
    private int selectedAshedStickIndex = 0;
    private int selectedIncenseIndex = 0;
    public Logs setLogs;
    public BaseSticks setBaseSticks;
    public AshedSticks setAshedSticks;
    public Incense setIncense;

    private String[] LogNames = java.util.Arrays.stream(Logs.values())
            .map(Enum::name)
            .toArray(String[]::new);

    private String[] AshedStickNames = java.util.Arrays.stream(AshedSticks.values())
            .map(Enum::name)
            .toArray(String[]::new);

    private String[] IncenseStickNames = java.util.Arrays.stream(Incense.values())
            .map(Enum::name)
            .toArray(String[]::new);

    // Vars
    private Map<String, Integer> presetlist = new LinkedHashMap<>(); // Stick craft list (1)
    public boolean Logout = false;

    private String[] scriptOptions = { "Craft incense sticks", "Ash incense sticks", "Herb incense sticks" };
    private int selectedScriptOption = 0; // Default to "Craft incense sticks"

    private String currentBankPreset; // Default selected bank preset
    private int currentBankPresetId; // Default selected bank preset

    public StickMakerGraphicsContext(ScriptConsole console, StickMaker script) {
        super(console);
        this.script = script;

        // Refactored Logs
        this.LogIndex = new NativeInteger(0);
        this.setLogs = Logs.values()[selectedLogIndex];
        script.setActiveLogs(setLogs);

        // Refactored ashed sticks
        this.AshedStickIndex = new NativeInteger(0);
        this.setAshedSticks = AshedSticks.values()[selectedAshedStickIndex];
        script.setActiveAshedSticks(setAshedSticks);

        // Refactored incense sticks
        this.IncenseIndex = new NativeInteger(0);
        this.setIncense = Incense.values()[selectedIncenseIndex];
        script.setActiveIncense(setIncense);

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
        script.logout = Logout;
    }

    @Override
    public void drawSettings() {
        ImGui.SetWindowSize(200.f, 200.f);
        ImGui.Begin("Incense Maker", 0);
        if (ImGui.BeginTabBar("Config", 0)) {
            if (ImGui.BeginTabItem("Config", 0)) {
                if (ImGui.Button("Start Script")) {
                    script.println("Start script button selected.");
                    script.setBotState(script.botState.RUNNING);
                }
                ImGui.SameLine();
                if (ImGui.Button("Stop Script")) {
                    script.println("Stop script button selected.");
                    script.setBotState(script.botState.STOPPED);
                }
                ImGui.SameLine();
                script.logout = ImGui.Checkbox("Logout on Completion", script.logout);
                script.DebugScript = ImGui.Checkbox("Debugging", script.DebugScript);
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
                ImGui.EndTabItem();
            }
            if (ImGui.BeginTabItem("Status", 0)) {
                long elapsed = System.currentTimeMillis() - script.scriptStart;
                long seconds = elapsed / 1000 % 60;
                long minutes = elapsed / 60000 % 60;
                long hours = elapsed / 3600000 % 24;
                ImGui.Text("Run time: %02d:%02d:%02d%n", hours, minutes, seconds);
                ImGui.Text("Crafted Logs: %,d", script.craftedLogCount);
                ImGui.Text("Ashed Incense: %,d", script.ashedIncenseCount);
                ImGui.Text("Completed Incense: %,d", script.finishedIncenseCount);
                ImGui.EndTabItem();
            }
            ImGui.EndTabBar();
        }
        ImGui.End();
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }

    // Render methods to dynamically display relevent menu items
    // Refactored methods
    private void renderLogDropDown() {
        NativeInteger currentIndex = new NativeInteger(selectedLogIndex);
        if (ImGui.Combo("Select Logs", currentIndex, LogNames)) {
            selectedLogIndex = currentIndex.get();
            setLogs = Logs.values()[selectedLogIndex];
            script.setActiveLogs(setLogs);
        }
    }

    private void renderStickDropDown() {
        NativeInteger currentIndex = new NativeInteger(selectedAshedStickIndex);
        if (ImGui.Combo("Select Incense Stick", currentIndex, AshedStickNames)) {
            selectedAshedStickIndex = currentIndex.get();
            setAshedSticks = AshedSticks.values()[selectedAshedStickIndex];
            script.setActiveAshedSticks(setAshedSticks);
        }
    }

    private void renderHerbDropDown() {
        NativeInteger currentIndex = new NativeInteger(selectedIncenseIndex);
        if (ImGui.Combo("Select Incense Stick", currentIndex, IncenseStickNames)) {
            selectedIncenseIndex = currentIndex.get();
            setIncense = Incense.values()[selectedIncenseIndex];
            script.setActiveIncense(setIncense);
        }
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