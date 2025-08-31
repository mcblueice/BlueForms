package net.mcblueice.blueforms.utils;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TaskSchedulerUtils {
    private static final boolean isFolia;
    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        isFolia = folia;
    }

    public static void runTask(Plugin plugin, Runnable task) {
        if (isFolia) {
            try {
                Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
                Method runMethod = scheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);
                runMethod.invoke(scheduler, plugin, task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (isFolia) {
            try {
                Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
                Method runDelayed = scheduler.getClass().getMethod("executeDelayed", Plugin.class, Runnable.class, long.class);
                runDelayed.invoke(scheduler, plugin, task, delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public static void runTask(Player player, Plugin plugin, Runnable task) {
        if (isFolia) {
            try {
                Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(Bukkit.getServer());
                Method runMethod = scheduler.getClass().getMethod("run", Plugin.class, Player.class, Runnable.class);
                runMethod.invoke(scheduler, plugin, player, task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    public static void runTaskLater(Player player, Plugin plugin, Runnable task, long delay) {
        if (isFolia) {
            try {
                Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(Bukkit.getServer());
                Method runDelayed = scheduler.getClass().getMethod("runDelayed", Plugin.class, Player.class, Runnable.class, long.class);
                runDelayed.invoke(scheduler, plugin, player, task, delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public static interface RepeatingTaskHandler {
        void cancel();
    }
    public static RepeatingTaskHandler runRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
        if (isFolia) {
            class FoliaRepeater implements Runnable, RepeatingTaskHandler {
                private volatile boolean running = true;
                @Override
                public void run() {
                    if (!running) return;
                    task.run();
                    TaskSchedulerUtils.runTaskLater(plugin, this, period);
                }
                @Override
                public void cancel() { running = false; }
            }
            FoliaRepeater repeater = new FoliaRepeater();
            TaskSchedulerUtils.runTaskLater(plugin, repeater, delay);
            return repeater;
        } else {
            int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
            return () -> Bukkit.getScheduler().cancelTask(id);
        }
    }
}