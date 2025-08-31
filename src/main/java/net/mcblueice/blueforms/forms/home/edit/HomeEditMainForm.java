package net.mcblueice.blueforms.forms.home.edit;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.OnlineUser;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.BedrockIconUtils;
import net.mcblueice.blueforms.forms.home.HomeHomeDetailForm;
import net.mcblueice.blueforms.utils.TaskScheduler;

public class HomeEditMainForm {
    private final Player player;
    private final ConfigManager lang;
    private final String mode;
    private final String filterCategory;
    private final Home home;

    public HomeEditMainForm(Player player, ConfigManager lang, String mode, String filterCategory, Home home) {
        this.player = player;
        this.lang = lang;
        this.mode = mode;
        this.filterCategory = filterCategory;
        this.home = home;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        if (!player.getName().equals(home.getOwner().getName())) {
			player.sendMessage(lang.get("forms.home.edit.main.nopermissionmessage", home.getName()));
			return;
		}

		HuskHomesAPI huskhomes = HuskHomesAPI.getInstance();
		OnlineUser user = huskhomes.adaptUser(player);
		CompletableFuture<List<Home>> phomelist = huskhomes.getUserPublicHomes(user);
		phomelist.thenAccept(result -> TaskScheduler.runTask(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
			if (!player.isOnline()) return;
            List<Home> phomeslist = result;
            int phomes = phomeslist.size();
            int maxphomes = huskhomes.getMaxPublicHomeSlots(user);

            SimpleForm.Builder builder = SimpleForm.builder()
                .title(lang.get("forms.home.edit.main.title"))
                .content(generateContent());

            List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.home.edit.main.buttons"));
            for (String key : buttonKeys) {
                String name = lang.get("forms.home.edit.main.buttons." + key + ".name");
                if (key.equals("editpub")) {
                    name = lang.get("forms.home.edit.main.buttons.editpub.name", phomes, maxphomes);
                }
                if (key.equals("editicon")) {
                    String iconDef = BedrockIconUtils.resolveHomeIcon(home);
                    String[] parts = iconDef.split(";", 2);
                    if (parts.length == 2) {
                        switch (parts[0].toUpperCase(Locale.ROOT)) {
                            case "URL":
                                builder.button(name, FormImage.Type.URL, parts[1]);
                                break;
                            case "PATH":
                                builder.button(name, FormImage.Type.PATH, parts[1]);
                                break;
                            default:
                                builder.button(name, FormImage.Type.PATH, "textures/blocks/stone.png");
                                break;
                        }
                    } else {
                        builder.button(name, FormImage.Type.PATH, "textures/blocks/stone.png");
                    }
                } else {
                    String icon = lang.get("forms.home.edit.main.buttons." + key + ".icon");
                    if (icon == null) icon = "NONE";
                    switch (icon.split(";")[0].toUpperCase()) {
                        case "URL":
                            builder.button(name, FormImage.Type.URL, icon.substring(4));
                            break;
                        case "PATH":
                            builder.button(name, FormImage.Type.PATH, icon.substring(5));
                            break;
                        default:
                            builder.button(name);
                            break;
                    }
                }
            }

            builder.validResultHandler((form, response) -> {
                int id = response.clickedButtonId();
                String key = buttonKeys.get(id);

                switch (key) {
                    case "editicon":
                        ItemStack newicon = player.getInventory().getItemInMainHand();
                        if (newicon != null && newicon.getType() != Material.AIR) {
                            Map<String, String> tags = home.getMeta().getTags();
                            tags.put("huskhomesgui:icon", newicon.getType().getKey().toString());
                            HuskHomesAPI.getInstance().setHomeMetaTags(home, tags);
                        }
                        TaskScheduler.runTaskLater(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
                            new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
                        }, 10L);
                        break;
                    case "rename":
                        new HomeEditRenameForm(player, lang, mode, filterCategory, home).open();
                        break;
                    case "editdesc":
                        new HomeEditDescriptionForm(player, lang, mode, filterCategory, home).open();
                        break;
                    case "editcat":
                        new HomeEditCategoryForm(player, lang, mode, filterCategory, home).open();
                        break;
                    case "editpub":
                        if (home.isPublic()) {
                            HuskHomesAPI.getInstance().setHomePrivacy(home, false);
                            home.setPublic(false);
                            TaskScheduler.runTaskLater(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
                                new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
                            }, 10L);
                        } else {
                            if (phomes >= maxphomes) {
                                player.sendMessage(lang.get("forms.home.edit.main.phomelimit", maxphomes));
                                return;
                            } else {
                                HuskHomesAPI.getInstance().setHomePrivacy(home, true);
                                home.setPublic(true);
                                TaskScheduler.runTaskLater(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
                                    new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
                                }, 10L);
                            }
                        }
                        break;
                    case "editpos":
                        new HomeEditPositionForm(player, lang, mode, filterCategory, home).open();
                        break;
                    case "delete":
                        new HomeEditDeleteForm(player, lang, mode, filterCategory, home).open();
                        break;
                    default:
                        player.sendMessage(lang.get("forms.etc.unknownoption"));
                        break;
                }
            });

            builder.closedOrInvalidResultHandler((form, response) -> {
                new HomeHomeDetailForm(player, lang, mode, filterCategory, home).open();
            });

            FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
        }));
    }

    private String generateContent() {

        String homename = home.getName().replace("&", "§");
        String homeowner = home.getOwner().getName();
        String homedesc = !home.getMeta().getDescription().isEmpty() ? home.getMeta().getDescription().replace("&", "§") : lang.get("forms.home.edit.main.content.nodesc");
        String homepublic = home.isPublic() ? lang.get("forms.home.edit.main.content.public") : lang.get("forms.home.edit.main.content.private");

        String rawhomecat = home.getMeta().getTags().get("blueforms:category");
        String homecat = lang.get("forms.home.nocat");
        if (rawhomecat != null) {
            String homecatLang = lang.get("forms.home.categories." + rawhomecat + ".name");
            if (homecatLang != null && !homecatLang.isEmpty() && !homecatLang.equals("forms.home.categories." + rawhomecat + ".name")) homecat = homecatLang;
        }

        String rawServerName = home.getServer();
        String serverName = lang.get("forms.etc.unknownserver");
        if (rawServerName != null) {
            String serverLang = lang.get("forms.etc.servers." + rawServerName);
            if (serverLang != null && !serverLang.isEmpty() && !serverLang.equals("forms.etc.servers." + rawServerName)) {
                serverName = serverLang;
            }
        }
        String rawWorldName = home.getWorld().getName();
        String worldName = lang.get("forms.etc.unknownworld");
        if (rawWorldName != null) {
            String worldLang = lang.get("forms.etc.worlds." + rawWorldName);
            if (worldLang != null && !worldLang.isEmpty() && !worldLang.equals("forms.etc.worlds." + rawWorldName)) {
                worldName = worldLang;
            }
        }
        double homeX = Math.round(home.getX() * 10.0) / 10.0;
        double homeY = Math.round(home.getY() * 10.0) / 10.0;
        double homeZ = Math.round(home.getZ() * 10.0) / 10.0;
        return lang.get("forms.home.edit.main.content.template",
            homename,
            homeowner,
            homecat,
            homedesc,
            homepublic,
            serverName,
            worldName,
            homeX,
            homeY,
            homeZ
        );
    }
}