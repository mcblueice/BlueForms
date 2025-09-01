
package net.mcblueice.blueforms.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class TaskScheduler {

    private TaskScheduler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void runTask(Plugin plugin, Runnable task) {
        try {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        try {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public static void runTask(Player player, Plugin plugin, Runnable task) {
        try {
            player.getScheduler().run(plugin, scheduledTask -> task.run(), () -> {});
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    public static void runTaskLater(Player player, Plugin plugin, Runnable task, long delay) {
        try {
            player.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), () -> {}, delay);
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    /**
     * 在正確的區域/主執行緒上為玩家執行一條指令。
     */
    public static void dispatchCommand(Player player, Plugin plugin, String command) {
        runTask(player, plugin, () -> Bukkit.dispatchCommand(player, command));
    }

    @FunctionalInterface
    public static interface RepeatingTaskHandler {
        void cancel();
    }
    public static RepeatingTaskHandler runRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
        // Folia: 優先使用 GlobalRegionScheduler.runAtFixedRate
        try {
            java.util.concurrent.atomic.AtomicReference<Runnable> cancelRef = new java.util.concurrent.atomic.AtomicReference<>();
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (cancelRef.get() == null) cancelRef.set(scheduledTask::cancel);
                task.run();
            }, delay, period);
            return () -> {
                Runnable c = cancelRef.get();
                if (c != null) c.run();
            };
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            // 舊 Folia：用自我重排模擬 repeating
            try {
                class FoliaRepeater implements Runnable, RepeatingTaskHandler {
                    private volatile boolean running = true;
                    @Override
                    public void run() {
                        if (!running) return;
                        task.run();
                        TaskScheduler.runTaskLater(plugin, this, period);
                    }
                    @Override
                    public void cancel() { running = false; }
                }
                FoliaRepeater repeater = new FoliaRepeater();
                TaskScheduler.runTaskLater(plugin, repeater, delay);
                return repeater;
            } catch (NoSuchMethodError | NoClassDefFoundError e2) {
                // Bukkit：使用原生重複任務
                int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
                return () -> Bukkit.getScheduler().cancelTask(id);
            }
        }
    }

    // 新增：玩家綁定的重複任務（在玩家區域執行，更安全且避免跨區域問題）
    public static RepeatingTaskHandler runRepeatingTask(Player player, Plugin plugin, Runnable task, long delay, long period) {
        // Folia: 使用 EntityScheduler.runAtFixedRate，需要 retired Runnable 參數
        try {
            java.util.concurrent.atomic.AtomicReference<Runnable> cancelRef = new java.util.concurrent.atomic.AtomicReference<>();
            player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (cancelRef.get() == null) cancelRef.set(scheduledTask::cancel);
                task.run();
            }, () -> {}, delay, period);
            return () -> {
                Runnable c = cancelRef.get();
                if (c != null) c.run();
            };
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            // 舊 Folia：自我重排到玩家區域
            try {
                class FoliaRepeater implements Runnable, RepeatingTaskHandler {
                    private volatile boolean running = true;
                    @Override
                    public void run() {
                        if (!running) return;
                        task.run();
                        TaskScheduler.runTaskLater(player, plugin, this, period);
                    }
                    @Override
                    public void cancel() { running = false; }
                }
                FoliaRepeater repeater = new FoliaRepeater();
                TaskScheduler.runTaskLater(player, plugin, repeater, delay);
                return repeater;
            } catch (NoSuchMethodError | NoClassDefFoundError e2) {
                // Bukkit：回退到同步重複任務（不綁定玩家執行緒）
                int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
                return () -> Bukkit.getScheduler().cancelTask(id);
            }
        }
    }
}