package net.mcblueice.blueforms.forms.tp;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;

public class TpMainForm {
    private final Player player;
    private final ConfigManager lang;

    public TpMainForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.tp.main.title"));

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.tp.main.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.tp.main.buttons." + key + ".name");
            String icon = lang.get("forms.tp.main.buttons." + key + ".icon");
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
                case "accept":
                    Bukkit.dispatchCommand(player, "huskhomes:tpaccept");
                    break;
                case "deny":
                    Bukkit.dispatchCommand(player, "huskhomes:tpdeny");
                    break;
                case "tpa":
                    new TpSelectForm(player, lang, "tpa").open();
                    break;
                case "tpahere":
                    new TpSelectForm(player, lang, "tpahere").open();
                    break;
                default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    break;
            }
        });
        
		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
