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
    public NativeInteger IncenseIndex;
    private int selectedIncenseIndex = 0;
    public Incense setIncense;

    private String[] IncenseStickNames = java.util.Arrays.stream(Incense.values())
            .map(Incense::getItemName)
            .toArray(String[]::new);

    // Vars
    private Map<String, Integer> presetlist = new LinkedHashMap<>(); // Stick craft list (1)
    public boolean Logout = false;

    private String[] scriptOptions = {"Craft incense sticks", "Ash incense sticks", "Herb incense sticks"};
    private int selectedScriptOption = 0; // Default to "Craft incense sticks"

    private String currentBankPreset; // Default selected bank preset
    private int currentBankPresetId; // Default selected bank preset

    public StickMakerGraphicsContext(ScriptConsole console, StickMaker script) {
        super(console);
        this.script = script;

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
                script.useLastPreset = ImGui.Checkbox("Use Last Preset", script.useLastPreset);
                ImGui.Text("Bot Status: " + script.getBotState());
                NativeInteger currentPresetIndex = new NativeInteger(findIndexForPreset(currentBankPreset));
                String[] bplist = presetlist.keySet().toArray(new String[0]);
                renderHerbDropDown();
                if (!script.useLastPreset) {
                    if (ImGui.Combo("Select Bank Preset", currentPresetIndex, bplist)) {
                        currentBankPreset = bplist[currentPresetIndex.get()];
                        currentBankPresetId = presetlist.get(currentBankPreset);
                        script.currentPresetId = currentBankPresetId;
                    }
                }
                NativeInteger scriptOptionIndex = new NativeInteger(selectedScriptOption);
                if (ImGui.Combo("Select Script Option", scriptOptionIndex, scriptOptions)) {
                    selectedScriptOption = scriptOptionIndex.get();
                    script.SelectedFunction = selectedScriptOption;
                }
                ImGui.EndTabItem();
            }
            if (ImGui.BeginTabItem("Instructions", 0)) {
                ImGui.Text("How to use:");
                ImGui.Separator();
                ImGui.Text("1. Select the incense stick type");
                ImGui.Text("2. Choose the script option:");
                ImGui.Text("   - Craft: Makes base sticks from logs");
                ImGui.Text("   - Ash: Adds ashes to base sticks");
                ImGui.Text("   - Herb: Adds herbs to ashed sticks");
                ImGui.Text("3. Select bank preset with materials, OR use last preset");
                ImGui.Text("4. Click Start Script");
                ImGui.Separator();
                ImGui.Text("Ensure you have the required items in your selected bank preset");
                ImGui.Text("The script will stop when you run out of the required material");
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

    private void renderHerbDropDown() {
        NativeInteger currentIndex = new NativeInteger(selectedIncenseIndex);
        if (ImGui.Combo("Select Incense Stick", currentIndex, IncenseStickNames)) {
            selectedIncenseIndex = currentIndex.get();
            setIncense = Incense.values()[selectedIncenseIndex];
            script.setActiveIncense(setIncense);
            script.setIncenseConfig();
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
