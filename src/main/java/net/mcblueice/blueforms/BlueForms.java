package net.mcblueice.blueforms;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import net.mcblueice.blueforms.utils.BlueCrossServerUtils;

import net.mcblueice.bluecrossserver.BlueCrossServer;

public class BlueForms extends JavaPlugin {
    private static BlueForms instance;
    private ConfigManager languageManager;
    private Logger logger;
    private boolean enableCrossServer;
    private boolean enableResidence;
    private boolean enableHome;
    private boolean enableTp;
    private boolean enableMessage;

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

    @Override
    public void onDisable() {
        logger.info("Blueforms 已卸載");
    }

    public static BlueForms getInstance() { return instance; }

    public ConfigManager getLanguageManager() { return languageManager; }

    public boolean isCrossServerEnabled() { return enableCrossServer; }
    public boolean isResidenceEnabled() { return enableResidence; }
    public boolean isHomeEnabled() { return enableHome; }
    public boolean isTpEnabled() { return enableTp; }
    public boolean isMessageEnabled() { return enableMessage; }

    public void refreshFeatures() {
        enableCrossServer = getConfig().getBoolean("features.cross_server", true);
        enableResidence = getConfig().getBoolean("features.residence", true);
        enableHome = getConfig().getBoolean("features.home", true);
        enableTp = getConfig().getBoolean("features.tp", true);
        enableMessage = getConfig().getBoolean("features.message", true);


        if (enableCrossServer) {
            if (getServer().getPluginManager().getPlugin("BlueCrossServer") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aBlueCrossServer 已啟用 已開啟跨分流讀取功能!");
                BlueCrossServerUtils.enable(BlueCrossServer.getInstance().getPlayerService(), this, 200L, 200L, list -> getServer().getConsoleSender().sendMessage("§r[BlueForms] §9初始化跨服玩家數: §b" + list.size())); // 每 10 秒刷新一次
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cBlueCrossServer 未啟用 已關閉跨分流讀取功能!");
                enableCrossServer = false;
                BlueCrossServerUtils.disable();
            }
        } else {
            BlueCrossServerUtils.disable();
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §c跨分流功能已關閉");
        }


        if (enableResidence) {
            if (getServer().getPluginManager().getPlugin("Residence") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aResidence 已啟用 已開啟 Residence 功能!");
                enableResidence = true;
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cResidence 未啟用 已關閉 Residence 功能!");
                enableResidence = false;
            }
        } else {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §cResidence 功能已關閉");
        }

        if (enableHome) {
            if (getServer().getPluginManager().getPlugin("HuskHomes") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aHuskHomes 已啟用 已開啟 Home 功能!");
                enableHome = true;
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHuskHomes 未啟用 已關閉 Home 功能!");
                enableHome = false;
            }
        } else {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHome 功能已關閉");
            enableHome = false;
        }

        if (enableTp) {
            if (getServer().getPluginManager().getPlugin("HuskHomes") != null) {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §aHuskHomes 已啟用 已開啟 Tp 功能!");
                enableTp = true;
            } else {
                getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHuskHomes 未啟用 已關閉 Tp 功能!");
                enableTp = false;
            }
        } else {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §cHuskHomes 功能已關閉");
            enableTp = false;
        }

        if (enableMessage) {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §a已開啟 Message 功能!");
            enableMessage = true;
        } else {
            getServer().getConsoleSender().sendMessage("§r[BlueForms] §cMessage 功能已關閉");
            enableMessage = false;
        }
    }
}