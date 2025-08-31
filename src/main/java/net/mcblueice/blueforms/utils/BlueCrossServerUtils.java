package net.mcblueice.blueforms.utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.bukkit.plugin.Plugin;

import net.mcblueice.bluecrossserver.CrossServerPlayerService;

/**
 * 提供跨服玩家快取與刷新功能的工具類。
 * 需在 BlueForms 啟動並檢測到 BlueCrossServer 後呼叫 setup。
 */
public final class BlueCrossServerUtils {

    /** 快取的玩家列表 */
    private static final AtomicReference<List<String>> CACHED = new AtomicReference<>(List.of());
    /** 跨服服務 */
    private static CrossServerPlayerService service;
    /** 用於主執行緒排程的插件實例 */
    private static Plugin plugin;

    private static TaskSchedulerUtils.RepeatingTaskHandler repeatingHandler = null;



    /** 工具類禁止實例化 */
    private BlueCrossServerUtils() {}

    /**
     * 初始化工具類。
     */
    public static void setup(CrossServerPlayerService playerService, Plugin owningPlugin) {
        service = playerService;
        plugin = owningPlugin;
    }

    /**
     * 一次性初始化並開始自動刷新（封裝 setup + 首次拉取 + startAutoRefresh）。
     */
    public static void enable(CrossServerPlayerService playerService, Plugin owningPlugin, long delayTicks, long periodTicks, Consumer<List<String>> firstCallback) {
        disable(); // 確保沒有殘留任務
        setup(playerService, owningPlugin);
        refreshNow(firstCallback);
        startAutoRefresh(owningPlugin, delayTicks, periodTicks);
    }

    /**
     * 停止並清除所有狀態。
     */
    public static void disable() {
        clear();
    }

    /**
     * 取得底層 CrossServerPlayerService (可能為 null)。
     * @return CrossServerPlayerService 實例或 null
     */
    public static CrossServerPlayerService getService() {
        return service;
    }

    /**
     * 取得目前快取（不可變副本）。
     * @return 玩家名稱列表
     */
    public static List<String> getCached() {
        return List.copyOf(CACHED.get());
    }

    /**
     * 清除資料並停止自動刷新。
     */
    public static void clear() {
        CACHED.set(List.of());
        service = null;
        if (repeatingHandler != null) {
            repeatingHandler.cancel();
            repeatingHandler = null;
        }
        plugin = null;
    }

    /**
     * 非同步抓取所有跨服玩家並更新快取；若尚未初始化，直接回傳目前快取。
     */
    public static void refreshNow(Consumer<List<String>> callback) {
        if (service == null || plugin == null) {
            if (callback != null) callback.accept(getCached());
            return;
        }
        service.requestAllPlayers(list -> {
            CACHED.set(list);
            if (callback != null) {
                // 全域調度 Folia 兼容
                TaskSchedulerUtils.runTask(plugin, () -> callback.accept(List.copyOf(list)));
            }
        });
    }

    /**
     * 啟動自動刷新任務。
     * @param owningPlugin 插件實例
     * @param delay 延遲時間（tick）
     * @param period 間隔時間（tick）
     */
    public static void startAutoRefresh(Plugin owningPlugin, long delay, long period) {
        if (owningPlugin == null) return;
        if (repeatingHandler != null) {
            repeatingHandler.cancel();
            repeatingHandler = null;
        }
        repeatingHandler = TaskSchedulerUtils.runRepeatingTask(owningPlugin, () -> refreshNow(null), delay, period);
    }
}
