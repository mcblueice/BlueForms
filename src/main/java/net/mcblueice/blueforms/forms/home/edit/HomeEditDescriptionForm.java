package net.mcblueice.blueforms.forms.home.edit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Home;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskSchedulerUtils;

public class HomeEditDescriptionForm {
	private final Player player;
	private final ConfigManager lang;
    private final String mode;
    private final String filterCategory;
    private final Home home;

	public HomeEditDescriptionForm(Player player, ConfigManager lang, String mode, String filterCategory, Home home) {
		this.player = player;
		this.lang = lang;
        this.mode = mode;
        this.filterCategory = filterCategory;
        this.home = home;
	}

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        String homedesc = !home.getMeta().getDescription().isEmpty() ? home.getMeta().getDescription().replace("&", "§") : lang.get("forms.home.edit.description.nodesc");
        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.home.edit.description.title"))
            .input(lang.get("forms.home.edit.description.content.template", homedesc) + "\n\n" + lang.get("forms.home.edit.description.input1"), lang.get("forms.home.edit.description.input2"));

        builder.validResultHandler((form, response) -> {
            String input1 = response.asInput(0);
            if (input1 == null || !input1.matches("[\\p{L}\\p{N}_-]+")) {
                player.sendMessage(lang.get("forms.home.edit.description.invalid"));
                return;
            }
            HuskHomesAPI.getInstance().setHomeDescription(home, input1);
            TaskSchedulerUtils.runTaskLater(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
                new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
            }, 10L);
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new HomeEditMainForm(player, lang, mode, filterCategory, home).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
