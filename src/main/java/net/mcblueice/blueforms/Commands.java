package net.mcblueice.blueforms;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.forms.residence.ResidenceMainForm;
import net.mcblueice.blueforms.forms.tp.TpMainForm;
import net.mcblueice.blueforms.forms.home.HomeMainForm;

public class Commands implements CommandExecutor {
    private final BlueForms plugin;
    private final ConfigManager lang;

    public Commands(BlueForms plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    plugin.reloadConfig();
                    plugin.getLanguageManager().reload();
                    plugin.refreshFeatures();
                    sender.sendMessage(lang.get("forms.etc.reloaded"));
                    return true;
                case "residence":
                    if (!plugin.isResidenceEnabled()) {
                        sender.sendMessage(lang.get("forms.residence.disabled"));
                        return true;
                    }
                    if (args.length > 1) {
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(lang.get("forms.etc.targetunknownplayer", args[1]));
                            return true;
                        }
                        if (!FloodgateApi.getInstance().isFloodgatePlayer(target.getUniqueId())) {
                            sender.sendMessage(lang.get("forms.etc.targetnobedrock", args[1]));
                            return true;
                        }
                        new ResidenceMainForm(target, lang).open();
                        sender.sendMessage(lang.get("forms.residence.targetopenform", target.getName()));
                        return true;
                    } else {
                        if (player != null) {
                            if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                sender.sendMessage(lang.get("forms.etc.selfnobedrock"));
                                return true;
                            }
                            new ResidenceMainForm(player, lang).open();
                            sender.sendMessage(lang.get("forms.residence.selfopenform"));
                        } else {
                            sender.sendMessage(lang.get("forms.etc.noconsole"));
                        }
                        return true;
                    }
                case "home":
                    if (!plugin.isHomeEnabled()) {
                        sender.sendMessage(lang.get("forms.home.disabled"));
                        return true;
                    }
                    if (args.length > 1) {
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(lang.get("forms.etc.targetunknownplayer", args[1]));
                            return true;
                        }
                        if (!FloodgateApi.getInstance().isFloodgatePlayer(target.getUniqueId())) {
                            sender.sendMessage(lang.get("forms.etc.targetnobedrock", args[1]));
                            return true;
                        }
                        new HomeMainForm(target, lang).open();
                        sender.sendMessage(lang.get("forms.home.targetopenform", target.getName()));
                        return true;
                    } else {
                        if (player != null) {
                            if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                sender.sendMessage(lang.get("forms.etc.selfnobedrock"));
                                return true;
                            }
                            new HomeMainForm(player, lang).open();
                            sender.sendMessage(lang.get("forms.home.selfopenform"));
                        } else {
                            sender.sendMessage(lang.get("forms.etc.noconsole"));
                        }
                        return true;
                    }
                case "tp":
                    if (!plugin.isTpEnabled()) {
                        sender.sendMessage(lang.get("forms.tp.disabled"));
                        return true;
                    }
                    if (args.length > 1) {
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(lang.get("forms.etc.targetunknownplayer", args[1]));
                            return true;
                        }
                        if (!FloodgateApi.getInstance().isFloodgatePlayer(target.getUniqueId())) {
                            sender.sendMessage(lang.get("forms.etc.targetnobedrock", args[1]));
                            return true;
                        }
                        new TpMainForm(target, lang).open();
                        sender.sendMessage(lang.get("forms.tp.targetopenform", target.getName()));
                        return true;
                    } else {
                        if (player != null) {
                            if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                sender.sendMessage(lang.get("forms.etc.selfnobedrock"));
                                return true;
                            }
                            new TpMainForm(player, lang).open();
                            sender.sendMessage(lang.get("forms.tp.selfopenform"));
                        } else {
                            sender.sendMessage(lang.get("forms.etc.noconsole"));
                        }
                        return true;
                    }
            }
        }

        sender.sendMessage(lang.get("forms.etc.usage"));
        return true;
    }
}
