package net.mcblueice.blueforms.forms.home.edit;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Home;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskScheduler;

public class HomeEditCategoryForm {
	private final Player player;
	private final ConfigManager lang;
    private final String mode;
    private final String filterCategory;
    private final Home home;

	public HomeEditCategoryForm(Player player, ConfigManager lang, String mode, String filterCategory, Home home) {
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

        String rawhomecat = home.getMeta().getTags().get("blueforms:category");
        String homecat = lang.get("forms.home.nocat");
        if (rawhomecat != null) {
            String homecatLang = lang.get("forms.home.categories." + rawhomecat + ".name");
            if (homecatLang != null && !homecatLang.isEmpty() && !homecatLang.equals("forms.home.categories." + rawhomecat + ".name")) homecat = homecatLang;
        }

		List<String> options = new ArrayList<>();
		options.add(lang.get("forms.home.removecat"));
        List<String> categoryKeys = new ArrayList<>(lang.getSectionKeys("forms.home.categories"));
        for (String key : categoryKeys) {
            options.add(lang.get("forms.home.categories." + key + ".name"));
        }

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.home.edit.category.title"))
			.dropdown(lang.get("forms.home.edit.category.content.template", homecat) + "\n\n" + lang.get("forms.home.edit.category.dropdown"), options);

		builder.validResultHandler((form, response) -> {
			int dropdown = response.asDropdown(0);
			String category = null;
            switch (dropdown) {
                case 0:
                    category = "remove";
                    break;
                case 1:
                    category = "build";
                    break;
                case 2:
                    category = "shop";
                    break;
                case 3:
                    category = "farm";
                    break;
                default:
                    player.sendMessage(lang.get("forms.home.edit.category.invalid"));
                    return;
            }
            
            Map<String, String> tags = home.getMeta().getTags();
            if (category != null && !category.equals("remove")) {
                tags.put("blueforms:category", category);
            } else {
                tags.remove("blueforms:category");
            }
            HuskHomesAPI.getInstance().setHomeMetaTags(home, tags);
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
