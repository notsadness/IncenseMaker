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
                ImGui.Text("Add Task:");
                String[] phaseOptions = {"Make Sticks", "Add Ash", "Add Herb"};
                NativeInteger phaseIndex = new NativeInteger(script.newTaskPhase);
                if (ImGui.Combo("Action", phaseIndex, phaseOptions)) {
                    script.newTaskPhase = phaseIndex.get();
                }
                if (ImGui.IsItemHovered()) {
                    ImGui.SetTooltip("Make Sticks: Make base sticks from logs\nAdd Ash: Add ashes to base sticks\nAdd Herb: Add herbs to ashed sticks");
                }
                String[] presetOptions = {"Preset 1", "Preset 2", "Preset 3", "Preset 4", "Preset 5", "Preset 6", "Preset 7", "Preset 8", "Preset 9"};
                NativeInteger presetIndex = new NativeInteger(script.newTaskPreset - 1);
                if (ImGui.Combo("Task Preset", presetIndex, presetOptions)) {
                    script.newTaskPreset = presetIndex.get() + 1;
                }
                script.newTaskCount = ImGui.InputInt("Count:", script.newTaskCount, 1, 1000, ImGuiWindowFlag.None.getValue());

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
                if (ImGui.Button("Clear All")) {
                    script.clearTaskQueue();
                    script.saveConfig();
                }

                ImGui.Separator();
                // Display task queue in table format
                if (ImGui.BeginTable("Tasks", 5, ImGuiWindowFlag.None.getValue())) {
                    ImGui.TableSetupColumn("Incense", 0);
                    ImGui.TableSetupColumn("Action", 0);
                    ImGui.TableSetupColumn("Progress", 0);
                    ImGui.TableSetupColumn("Preset", 0);
                    ImGui.TableSetupColumn("Remove", 0);
                    ImGui.TableHeadersRow();

                    synchronized (script.taskQueue) {
                        for (Task task : script.taskQueue) {
                            ImGui.TableNextRow();
                            ImGui.TableNextColumn();
                            String incenseName = task.getIncense() != null ? task.getIncense().getItemName() : "Unknown";
                            ImGui.Text(incenseName);

                            ImGui.TableNextColumn();
                            ImGui.Text(task.getPhaseName());

                            ImGui.TableNextColumn();
                            String progress = task.getCompletedCount() + "/" + task.getTargetCount();
                            if (script.activeTask == task) {
                                progress += " [ACTIVE]";
                            }
                            ImGui.Text(progress);

                            ImGui.TableNextColumn();
                            ImGui.Text("Preset " + task.getBankPresetId());

                            ImGui.TableNextColumn();
                            if (ImGui.Button("Remove##" + task.hashCode())) {
                                script.taskQueue.remove(task);
                                script.saveConfig();
                                break;
                            }
                        }
                    }
                    ImGui.EndTable();
                }
                ImGui.EndTabItem();
            }
            if (ImGui.BeginTabItem("Instructions", 0)) {
                ImGui.Text("How to use:");
                ImGui.Text("1. Select your incense stick type");
                ImGui.Text("2. Add tasks for each stick making action:");
                ImGui.Text("   - Set task count, action, and bank preset containing materials for that task");
                ImGui.Text("   - Tasks will run in the order they are added");
                ImGui.Text("3. Enable 'Use Last Preset' to be a little faster");
                ImGui.Text("   - First run uses load from specific preset");
                ImGui.Text("   - Subsequent runs use load last preset");
                ImGui.Text("4. Click Start Script");
                ImGui.Separator();
                ImGui.Text("The script will process tasks in order and stop when all tasks are completed or if there are no materials found");
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
