package net.botwithus.incense;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Task {
    private String taskName;
    private int phaseId; // 0 = craft logs, 1 = ash sticks, 2 = herb sticks
    private int targetCount;
    private int completedCount;
    private int bankPresetId;
    private boolean isFirstRun;
    private transient boolean isCompleted;
    private Incense incense; // The incense stick type for this task

    public Task() {
    }

    public Task(String taskName, int phaseId, int targetCount, int bankPresetId, Incense incense) {
        this.taskName = taskName;
        this.phaseId = phaseId;
        this.targetCount = targetCount;
        this.completedCount = 0;
        this.bankPresetId = bankPresetId;
        this.isFirstRun = true;
        this.isCompleted = false;
        this.incense = incense;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getPhaseId() {
        return phaseId;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public int getBankPresetId() {
        return bankPresetId;
    }

    public boolean isFirstRun() {
        return isFirstRun;
    }

    public void setFirstRunComplete() {
        this.isFirstRun = false;
    }

    public boolean isCompleted() {
        if (completedCount >= targetCount) {
            isCompleted = true;
        } else {
            isCompleted = false;
        }
        return isCompleted;
    }

    public void incrementCompleted(int amount) {
        this.completedCount += amount;
    }

    public Incense getIncense() {
        return incense;
    }

    public String getPhaseName() {
        switch (phaseId) {
            case 0:
                return "Craft Logs";
            case 1:
                return "Ash Sticks";
            case 2:
                return "Herb Sticks";
            default:
                return "Unknown";
        }
    }

    public static String toJson(Task task) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(task);
    }

    public static Task fromJson(String json) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, Task.class);
    }

    @Override
    public String toString() {
        String incenseName = incense != null ? incense.getItemName() : "Unknown";
        return incenseName + " - " + getPhaseName() + " (" + completedCount + "/" + targetCount + ")";
    }
}
