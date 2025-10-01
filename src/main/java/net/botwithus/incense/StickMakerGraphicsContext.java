package net.botwithus.incense;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

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

    // Task queue UI variables
    private String newTaskName = "";
    private int selectedTaskIndex = -1;

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

                renderHerbDropDown();

                ImGui.Separator();
                ImGui.Text("Task Queue:");
                // Display current task queue
                synchronized (script.taskQueue) {
                    if (script.taskQueue.isEmpty()) {
                        ImGui.Text("  -- No tasks in the queue -- ");
                    } else {
                        int index = 0;
                        for (Task task : script.taskQueue) {
                            String status = (script.activeTask == task) ? " [ACTIVE]" : "";
                            boolean isSelected = (selectedTaskIndex == index);
                            if (ImGui.Selectable(index + ". " + task.toString() + status, isSelected, 0)) {
                                selectedTaskIndex = index;
                            }
                            index++;
                        }
                    }
                }

                ImGui.Separator();
                ImGui.Text("Add Task:");
                script.newTaskCount = ImGui.InputInt("Count:", script.newTaskCount, 1, 1000, ImGuiWindowFlag.None.getValue());

                String[] phaseOptions = {"Craft Logs", "Ash Sticks", "Herb Sticks"};
                NativeInteger phaseIndex = new NativeInteger(script.newTaskPhase);
                if (ImGui.Combo("Action", phaseIndex, phaseOptions)) {
                    script.newTaskPhase = phaseIndex.get();
                }
                if (ImGui.IsItemHovered()) {
                    ImGui.SetTooltip("Craft Logs: Make base sticks from logs\nAsh Sticks: Add ashes to base sticks\nHerb Sticks: Add herbs to ashed sticks");
                }
                String[] presetOptions = {"Preset 1", "Preset 2", "Preset 3", "Preset 4", "Preset 5", "Preset 6", "Preset 7", "Preset 8", "Preset 9"};
                NativeInteger presetIndex = new NativeInteger(script.newTaskPreset - 1);
                if (ImGui.Combo("Task Preset", presetIndex, presetOptions)) {
                    script.newTaskPreset = presetIndex.get() + 1;
                }

                if (ImGui.Button("Add to Queue")) {
                    if (newTaskName.isEmpty()) {
                        newTaskName = "Task " + (script.taskQueue.size() + 1);
                    }
                    Task newTask = new Task(newTaskName, script.newTaskPhase, script.newTaskCount, script.newTaskPreset, script.activeIncense);
                    script.addTask(newTask);
                    script.println("Task added: " + newTaskName + " for " + script.activeIncense.getItemName());
                    newTaskName = "";
                    script.saveConfig();
                }
                ImGui.SameLine();
                if (ImGui.Button("Remove")) {
                    if (selectedTaskIndex >= 0) {
                        script.removeTask(selectedTaskIndex);
                        selectedTaskIndex = -1;
                    }
                    script.saveConfig();
                }
                ImGui.SameLine();
                if (ImGui.Button("Clear All")) {
                    script.clearTaskQueue();
                    selectedTaskIndex = -1;
                    script.saveConfig();
                }
                ImGui.EndTabItem();
            }
            if (ImGui.BeginTabItem("Instructions", 0)) {
                ImGui.Text("How to use:");
                ImGui.Text("1. Select your incense stick type");
                ImGui.Text("2. Go to Task Queue tab");
                ImGui.Text("3. Add tasks for each action:");
                ImGui.Text("   - Set task count, action, and preset associated with the materials for that task");
                ImGui.Text("   - Tasks will run in the order they are added");
                ImGui.Text("4. Enable 'Use Last Preset' to be a little faster");
                ImGui.Text("   - First run uses load from specific preset");
                ImGui.Text("   - Subsequent runs use load last preset");
                ImGui.Text("5. Click Start Script");
                ImGui.Separator();
                ImGui.Text("The script will process tasks in order");
                ImGui.Text("and stop when all tasks are complete");
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
        if (ImGui.IsItemHovered()) {
            ImGui.SetTooltip("Select your target incense stick\nThis is used to verify correct materials");
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
