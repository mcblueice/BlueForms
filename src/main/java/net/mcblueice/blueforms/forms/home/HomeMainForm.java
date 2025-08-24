package net.mcblueice.blueforms.forms.home;

import java.util.*;

import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;

public class HomeMainForm {
    private final Player player;
    private final ConfigManager lang;
    public HomeMainForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }
    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.home.main.title"));

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.home.main.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.home.main.buttons." + key + ".name");
            String icon = lang.get("forms.home.main.buttons." + key + ".icon");
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
                case "home":
                    new HomeHomelistForm(player, lang).open();
                    break;
                case "phome":
                    new HomePhomeSelectForm(player, lang).open();
                    break;
                default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    break;
            }
        });
        
		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}