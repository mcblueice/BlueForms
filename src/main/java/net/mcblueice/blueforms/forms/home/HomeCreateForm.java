package net.mcblueice.blueforms.forms.home;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;
import net.william278.huskhomes.api.HuskHomesAPI;

public class HomeCreateForm {
	private final Player player;
	private final ConfigManager lang;

	public HomeCreateForm(Player player, ConfigManager lang) {
		this.player = player;
		this.lang = lang;
	}

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;
        Location loc = player.getLocation();
        double playerX = Math.round(loc.getX() * 10.0) / 10.0;
        double playerY = Math.round(loc.getY() * 10.0) / 10.0;
        double playerZ = Math.round(loc.getZ() * 10.0) / 10.0;

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.home.create.title"))
            .input(lang.get("forms.home.create.content.template", playerX, playerY, playerZ) + "\n\n" + lang.get("forms.home.create.input1"), lang.get("forms.home.create.input2"));

        builder.validResultHandler((form, response) -> {
            String input1 = response.asInput(0);
            if (input1 == null || !input1.matches("[\\p{L}\\p{N}_-]+") || input1.length() > 16) {
                player.sendMessage(lang.get("forms.home.create.invalid"));
                return;
            }
            player.sendMessage(lang.get("forms.home.create.success", input1));
            HuskHomesAPI.getInstance().createHome(HuskHomesAPI.getInstance().adaptUser(player), input1, HuskHomesAPI.getInstance().adaptPosition(loc));
            return;
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new HomeHomelistForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
