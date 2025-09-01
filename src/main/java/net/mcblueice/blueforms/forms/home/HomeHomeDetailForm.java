package net.mcblueice.blueforms.forms.home;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.position.Home;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskScheduler;
import net.mcblueice.blueforms.forms.home.edit.HomeEditMainForm;

public class HomeHomeDetailForm {
    private final Player player;
    private final ConfigManager lang;
    private final String mode;
    private final String filterCategory;
    private final Home home;

    public HomeHomeDetailForm(Player player, ConfigManager lang, String mode, String filterCategory, Home home) {
        this.player = player;
        this.lang = lang;
        this.mode = mode;
        this.filterCategory = filterCategory;
        this.home = home;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;
        
        String homeowner = home.getOwner().getName();

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.home.detail.title"))
            .content(generateContent());

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.home.detail.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.home.detail.buttons." + key + ".name");
            String icon = lang.get("forms.home.detail.buttons." + key + ".icon");
            if (icon == null) icon = "NONE";
            switch (icon.split(";")[0].toUpperCase()) {
                case "URL":
                    if ( key.equals("edit") && !player.getName().equals(homeowner)) break;
                    builder.button(name, FormImage.Type.URL, icon.substring(4));
                    break;
                case "PATH":
                    if ( key.equals("edit") && !player.getName().equals(homeowner)) break;
                    builder.button(name, FormImage.Type.PATH, icon.substring(5));
                    break;
                default:
                    if ( key.equals("edit") && !player.getName().equals(homeowner)) break;
                    builder.button(name);
                    break;
            }
        }

        builder.validResultHandler((form, response) -> {
            int id = response.clickedButtonId();
            String key = buttonKeys.get(id);
            switch (key) {
                case "teleport":
                    TaskScheduler.dispatchCommand(player, Bukkit.getPluginManager().getPlugin("BlueForms"), "huskhomes:home " + home.getIdentifier());
                    break;
                case "edit":
                    new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
                    break;
                default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    break;
            }
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            switch (mode) {
                case "home":
                    new HomeHomelistForm(player, lang).open();
                    break;
                case "phome":
                    new HomePhomelistForm(player, lang, filterCategory).open();
                    break;
                default:
                    new HomeMainForm(player, lang).open();
                    break;
            }
        });

		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private String generateContent() {

        String homename = home.getName().replace("&", "§");
        String homeowner = home.getOwner().getName();
        String homedesc = !home.getMeta().getDescription().isEmpty() ? home.getMeta().getDescription().replace("&", "§") : lang.get("forms.home.detail.content.nodesc");
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
        return lang.get("forms.home.detail.content.template",
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
