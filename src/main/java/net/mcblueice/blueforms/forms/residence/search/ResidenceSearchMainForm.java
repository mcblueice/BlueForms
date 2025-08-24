package net.mcblueice.blueforms.forms.residence.search;

import java.util.*;

import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.forms.residence.ResidenceMainForm;

public class ResidenceSearchMainForm {
    private final Player player;
    private final ConfigManager lang;

    public ResidenceSearchMainForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.residence.search.main.title"))
            .content(lang.get("forms.residence.search.main.content"));

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.residence.search.main.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.residence.search.main.buttons." + key + ".name");
            String icon = lang.get("forms.residence.search.main.buttons." + key + ".icon");
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
                case "check":
                    new ResidenceSearchShowForm(player, lang).open();
                    break;
                case "info":
                    new ResidenceSearchInfoForm(player, lang).open();
                    break;
                default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    break;
            }
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
