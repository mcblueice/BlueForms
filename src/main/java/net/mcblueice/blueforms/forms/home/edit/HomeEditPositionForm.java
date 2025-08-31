package net.mcblueice.blueforms.forms.home.edit;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.OnlineUser;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskScheduler;

public class HomeEditPositionForm {
	private final Player player;
	private final ConfigManager lang;
    private final String mode;
    private final String filterCategory;
    private final Home home;

	public HomeEditPositionForm(Player player, ConfigManager lang, String mode, String filterCategory, Home home) {
		this.player = player;
		this.lang = lang;
		this.mode = mode;
		this.filterCategory = filterCategory;
		this.home = home;
	}

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        if (!player.getName().equals(home.getOwner().getName())) {
			player.sendMessage(lang.get("forms.home.edit.position.nopermissionmessage", home.getName()));
			return;
		}

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.home.edit.position.title"))
            .content(generateContent());

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.home.edit.position.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.home.edit.position.buttons." + key + ".name");
            String icon = lang.get("forms.home.edit.position.buttons." + key + ".icon");
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

        builder.validResultHandler((form, response) -> {
            int id = response.clickedButtonId();
            String key = buttonKeys.get(id);
            switch (key) {
                case "confirm":
                    OnlineUser user = HuskHomesAPI.getInstance().adaptUser(player);
                    HuskHomesAPI.getInstance().relocateHome(home, user.getPosition());
                    TaskScheduler.runTaskLater(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
                        new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
                    }, 10L);
                    break;
                case "cancel":
                    new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
                    break;
                default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    break;
            }
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
        });

		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private String generateContent() {
        String homeRawServerName = home.getServer();
        String homeServerName = lang.get("forms.etc.unknownserver");
        if (homeRawServerName != null) {
            String serverLang = lang.get("forms.etc.servers." + homeRawServerName);
            if (serverLang != null && !serverLang.isEmpty() && !serverLang.equals("forms.etc.servers." + homeRawServerName)) {
                homeServerName = serverLang;
            }
        }
        String homeRawWorldName = home.getWorld().getName();
        String homeWorldName = lang.get("forms.etc.unknownworld");
        if (homeRawWorldName != null) {
            String worldLang = lang.get("forms.etc.worlds." + homeRawWorldName);
            if (worldLang != null && !worldLang.isEmpty() && !worldLang.equals("forms.etc.worlds." + homeRawWorldName)) {
                homeWorldName = worldLang;
            }
        }
        double homeX = Math.round(home.getX() * 10.0) / 10.0;
        double homeY = Math.round(home.getY() * 10.0) / 10.0;
        double homeZ = Math.round(home.getZ() * 10.0) / 10.0;

        String playerRawServerName = HuskHomesAPI.getInstance().getServer();
        String playerServerName = lang.get("forms.etc.unknownserver");
        if (playerRawServerName != null) {
            String serverLang = lang.get("forms.etc.servers." + playerRawServerName);
            if (serverLang != null && !serverLang.isEmpty() && !serverLang.equals("forms.etc.servers." + playerRawServerName)) {
                playerServerName = serverLang;
            }
        }
        String playerRawWorldName = player.getLocation().getWorld().getName();
        String playerWorldName = lang.get("forms.etc.unknownworld");
        if (playerRawWorldName != null) {
            String worldLang = lang.get("forms.etc.worlds." + playerRawWorldName);
            if (worldLang != null && !worldLang.isEmpty() && !worldLang.equals("forms.etc.worlds." + playerRawWorldName)) {
                playerWorldName = worldLang;
            }
        }
        double playerX = Math.round(player.getLocation().getX() * 10.0) / 10.0;
        double playerY = Math.round(player.getLocation().getY() * 10.0) / 10.0;
        double playerZ = Math.round(player.getLocation().getZ() * 10.0) / 10.0;

        return lang.get("forms.home.edit.position.content.template",
            homeServerName,
            homeWorldName,
            homeX,
            homeY,
            homeZ,
            playerServerName,
            playerWorldName,
            playerX,
            playerY,
            playerZ
        );
    }
}
