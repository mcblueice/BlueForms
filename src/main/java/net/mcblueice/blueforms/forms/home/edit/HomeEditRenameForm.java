package net.mcblueice.blueforms.forms.home.edit;

import org.bukkit.*;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Home;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskScheduler;

public class HomeEditRenameForm {
	private final Player player;
	private final ConfigManager lang;
    private final String mode;
    private final String filterCategory;
    private final Home home;

	public HomeEditRenameForm(Player player, ConfigManager lang, String mode, String filterCategory, Home home) {
		this.player = player;
		this.lang = lang;
        this.mode = mode;
        this.filterCategory = filterCategory;
        this.home = home;
	}

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;
        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.home.edit.rename.title"))
            .input(lang.get("forms.home.edit.rename.content.template", home.getName()) + "\n\n" + lang.get("forms.home.edit.rename.input1"), lang.get("forms.home.edit.rename.input2"));

        builder.validResultHandler((form, response) -> {
            String input1 = response.asInput(0);
            if (input1 == null || !input1.matches("[\\p{L}\\p{N}_-]+") || input1.length() > 16) {
                player.sendMessage(lang.get("forms.home.edit.rename.invalid"));
                return;
            }
            HuskHomesAPI.getInstance().renameHome(home, input1);
            TaskScheduler.runTaskLater(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
                new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
            }, 10L);
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
