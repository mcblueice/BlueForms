package net.mcblueice.blueforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.forms.residence.ResidenceMainForm;
import net.mcblueice.blueforms.forms.tp.TpMainForm;
import net.mcblueice.blueforms.forms.home.HomeMainForm;
import net.mcblueice.blueforms.forms.message.MessageMainForm;

public class Commands implements CommandExecutor, TabCompleter {
    private final BlueForms plugin;
    private final ConfigManager lang;

    public Commands(BlueForms plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (!sender.hasPermission("blueforms.reload")) {
                        sender.sendMessage("§7§l[§a§l系統§7§l]§r§c你沒有權限使用此指令!");
                        return true;
                    }
                    plugin.reloadConfig();
                    plugin.getLanguageManager().reload();
                    plugin.refreshFeatures();
                    sender.sendMessage(lang.get("forms.etc.reloaded"));
                    return true;
                case "residence":
                        return openForm(sender, args, "blueforms.residence", plugin.isResidenceEnabled(), "residence",
                            (formPlayer, languageManager) -> new ResidenceMainForm(formPlayer, languageManager).open());
                case "home":
                        return openForm(sender, args, "blueforms.home", plugin.isHomeEnabled(), "home",
                            (formPlayer, languageManager) -> new HomeMainForm(formPlayer, languageManager).open());
                case "tp":
                        return openForm(sender, args, "blueforms.tp", plugin.isTpEnabled(), "tp",
                            (formPlayer, languageManager) -> new TpMainForm(formPlayer, languageManager).open());
                case "message":
                        return openForm(sender, args, "blueforms.message", plugin.isMessageEnabled(), "message",
                            (formPlayer, languageManager) -> new MessageMainForm(formPlayer, languageManager).open());
            }
        }

        sender.sendMessage(lang.get("forms.etc.usage"));
        return true;
    }
    private boolean openForm(CommandSender sender,
            String[] args,
            String permission,
            Boolean isEnabled,
            String key,
            BiConsumer<Player, ConfigManager> form) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§7§l[§a§l系統§7§l]§r§c你沒有權限使用此指令!");
            return true;
        }

        if (!isEnabled) {
            sender.sendMessage(lang.get("forms." + key + ".disabled"));
            return true;
        }

        FloodgateApi floodgateApi = FloodgateApi.getInstance();

        if (args.length > 1) {
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(lang.get("forms.etc.targetunknownplayer", args[1]));
                return true;
            }
            if (!floodgateApi.isFloodgatePlayer(target.getUniqueId())) {
                sender.sendMessage(lang.get("forms.etc.targetnobedrock", args[1]));
                return true;
            }
            form.accept(target, lang);
            sender.sendMessage(lang.get("forms." + key + ".targetopenform", target.getName()));
            return true;
        }

        if (player == null) {
            sender.sendMessage(lang.get("forms.etc.noconsole"));
            return true;
        }

        if (!floodgateApi.isFloodgatePlayer(player.getUniqueId())) {
            sender.sendMessage(lang.get("forms.etc.selfnobedrock"));
            return true;
        }

        form.accept(player, lang);
        sender.sendMessage(lang.get("forms." + key + ".selfopenform"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // main command
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("blueforms.reload")) subs.add("reload");
            if (sender.hasPermission("blueforms.use.residence")) subs.add("residence");
            if (sender.hasPermission("blueforms.use.home")) subs.add("home");
            if (sender.hasPermission("blueforms.use.tp")) subs.add("tp");
            if (sender.hasPermission("blueforms.use.message")) subs.add("message");
            StringUtil.copyPartialMatches(args[0], subs, completions);
            Collections.sort(completions);
            return completions;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("residence")
                || args[0].equalsIgnoreCase("home")
                || args[0].equalsIgnoreCase("tp")
                || args[0].equalsIgnoreCase("message"))) {
            List<String> subs = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) subs.add(player.getName());
            StringUtil.copyPartialMatches(args[1], subs, completions);
            Collections.sort(completions);
            return completions;
        }

        return Collections.emptyList();
    }
}
