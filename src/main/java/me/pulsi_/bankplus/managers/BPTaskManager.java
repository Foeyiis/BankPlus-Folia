package me.pulsi_.bankplus.managers;

<<<<<<< HEAD
import org.bukkit.scheduler.BukkitTask;
=======
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
>>>>>>> afb2ba7 (-)

import java.util.HashMap;

public class BPTaskManager {

    public static final String INTEREST_TASK = "interest", MONEY_SAVING_TASK = "money_saving", BANKTOP_BROADCAST_TASK = "banktop_broadcast";

<<<<<<< HEAD
    private static final HashMap<String, BukkitTask> tasks = new HashMap<>();

    public static void setTask(String name, BukkitTask task) {
=======
    private static final HashMap<String, MyScheduledTask> tasks = new HashMap<>();

    public static void setTask(String name, MyScheduledTask task) {
>>>>>>> afb2ba7 (-)
        String identifier = name.toLowerCase();
        if (tasks.containsKey(identifier)) tasks.get(identifier).cancel();
        tasks.put(identifier, task);
    }

<<<<<<< HEAD
    public static BukkitTask getTask(String name) {
        return tasks.get(name.toLowerCase());
    }

    public static BukkitTask removeTask(String name) {
=======
    public static MyScheduledTask getTask(String name) {
        return tasks.get(name.toLowerCase());
    }

    public static MyScheduledTask removeTask(String name) {
>>>>>>> afb2ba7 (-)
        return tasks.remove(name.toLowerCase());
    }

    public static boolean contains(String name) {
        return tasks.containsKey(name.toLowerCase());
    }
}