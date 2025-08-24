package net.mcblueice.blueforms.forms.home.edit;

import java.util.*;

import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Home;

import net.mcblueice.blueforms.ConfigManager;


public class HomeEditDeleteForm {
    private final Player player;
    private final ConfigManager lang;
    private final String mode;
    private final String filterCategory;
    private final Home home;

    public HomeEditDeleteForm(Player player, ConfigManager lang, String mode, String filterCategory, Home home) {
        this.player = player;
        this.lang = lang;
        this.mode = mode;
        this.filterCategory = filterCategory;
        this.home = home;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        if (!player.getName().equals(home.getOwner().getName())) {
			player.sendMessage(lang.get("forms.home.edit.delete.nopermissionmessage", home.getName()));
			return;
		}

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.home.edit.delete.title"))
            .content(generateContent());

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.home.edit.delete.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.home.edit.delete.buttons." + key + ".name");
            String icon = lang.get("forms.home.edit.delete.buttons." + key + ".icon");
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
                    openConfirm();
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

    private void openConfirm() {
        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.home.edit.delete.confirm.title"))
            .input(lang.get("forms.home.edit.delete.confirm.content", home.getName()) + "\n" + lang.get("forms.home.edit.delete.confirm.input1"), lang.get("forms.home.edit.delete.confirm.input2"));

        builder.validResultHandler((form, response) -> {
            String input = response.asInput(0);
            if (input.trim().equals("delete")) {
                player.sendMessage(lang.get("forms.home.edit.delete.confirm.success", home.getName()));
                HuskHomesAPI.getInstance().deleteHome(home);
                return;
            } else {
                new HomeEditDeleteForm(player, lang, mode, filterCategory, home).open();
            }
            
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new HomeEditDeleteForm(player, lang, mode, filterCategory, home).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}