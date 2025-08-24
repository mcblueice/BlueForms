package net.mcblueice.blueforms;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import net.mcblueice.blueforms.utils.BlueCrossServerUtils;

import net.mcblueice.bluecrossserver.BlueCrossServer;

/**
 * BlueForms 主插件類
 * 管理插件啟動、關閉與功能開關
 */
public class BlueForms extends JavaPlugin {
    private static BlueForms instance;
    private ConfigManager languageManager;
    private Logger logger;
    private boolean enableCrossServer;
    private boolean enableResidence;
    private boolean enableHome;
    private boolean enableTp;


    /**
     * 插件啟動時呼叫
     */
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        saveDefaultConfig();
        languageManager = new ConfigManager(this);
        refreshFeatures();

        getCommand("blueforms").setExecutor(new Commands(this));

        logger.info("Blueforms 已啟動");
    }


    /**
     * 插件卸載時呼叫
     */
    @Override
    public void onDisable() {
        logger.info("Blueforms 已卸載");
    }


    /**
     * 取得插件單例
     * @return BlueForms 實例
     */
    public static BlueForms getInstance() {
        return instance;
    }


    /**
     * 取得語言管理器
     * @return ConfigManager 實例
     */
    public ConfigManager getLanguageManager() {
        return languageManager;
    }


    /**
     * 是否啟用跨服功能
     */
    public boolean isCrossServerEnabled() {
        return enableCrossServer;
    }
    /**
     * 是否啟用 Residence
     */
    public boolean isResidenceEnabled() {
        return enableResidence;
    }
    /**
     * 是否啟用 Home
     */
    public boolean isHomeEnabled() {
        return enableHome;
    }
    /**
     * 是否啟用 Tp
     */
    public boolean isTpEnabled() {
        return enableTp;
    }

    /**
     * 重新讀取功能開關（供啟動與 /blueforms reload 使用）
     */
    public void refreshFeatures() {
        enableCrossServer = getConfig().getBoolean("features.cross_server", true);
        enableResidence = getConfig().getBoolean("features.residence", true);
        enableHome = getConfig().getBoolean("features.home", true);
        enableTp = getConfig().getBoolean("features.tp", true);


        if (enableCrossServer) {
            if (getServer().getPluginManager().getPlugin("BlueCrossServer") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aBlueCrossServer 已啟用 已開啟跨分流讀取功能！");
                BlueCrossServerUtils.enable(BlueCrossServer.getInstance().getPlayerService(), this, 200L, 200L, list -> getServer().getConsoleSender().sendMessage("§r[BlueForms] §9初始化跨服玩家數: §b" + list.size())); // 每 10 秒刷新一次
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cBlueCrossServer 未啟用 已關閉跨分流讀取功能！");
                enableCrossServer = false;
                BlueCrossServerUtils.disable();
            }
        } else {
            BlueCrossServerUtils.disable();
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §c跨分流功能已關閉");
        }


        if (enableResidence) {
            if (getServer().getPluginManager().getPlugin("Residence") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aResidence 已啟用 已開啟 Residence 功能！");
                enableResidence = true;
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cResidence 未啟用 已關閉 Residence 功能！");
                enableResidence = false;
            }
        } else {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §cResidence 功能已關閉");
        }

        if (enableHome) {
            if (getServer().getPluginManager().getPlugin("HuskHomes") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aHuskHomes 已啟用 已開啟 Home 功能！");
                enableHome = true;
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHuskHomes 未啟用 已關閉 Home 功能！");
                enableHome = false;
            }
        } else {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHome 功能已關閉");
            enableHome = false;
        }

        if (enableTp) {
            if (getServer().getPluginManager().getPlugin("HuskHomes") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aHuskHomes 已啟用 已開啟 Tp 功能！");
                enableTp = true;
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHuskHomes 未啟用 已關閉 Tp 功能！");
                enableTp = false;
            }
        } else {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHuskHomes 功能已關閉");
            enableTp = false;
        }
    }
}