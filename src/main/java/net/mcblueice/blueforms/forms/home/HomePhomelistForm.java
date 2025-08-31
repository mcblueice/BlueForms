package net.mcblueice.blueforms.forms.home;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.OnlineUser;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.BedrockIconUtils;
import net.mcblueice.blueforms.utils.TaskSchedulerUtils;

public class HomePhomelistForm {
	private final Player player;
	private final ConfigManager lang;
	private final String filterCategory;

	public HomePhomelistForm(Player player, ConfigManager lang, String filterCategory) {
		this.player = player;
		this.lang = lang;
		this.filterCategory = filterCategory;
	}

	public void open() {
		if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;
		HuskHomesAPI huskhomes = HuskHomesAPI.getInstance();
		OnlineUser user = huskhomes.adaptUser(player);
		CompletableFuture<List<Home>> homelist = huskhomes.getUserHomes(user);
		CompletableFuture<List<Home>> phomelist = huskhomes.getPublicHomes();
		homelist.thenCombine(phomelist, (homes, phomes) -> new Object[]{homes, phomes})
			.thenAccept(result -> TaskSchedulerUtils.runTask(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
				if (!player.isOnline()) return;
				@SuppressWarnings("unchecked")
				List<Home> phomeslist = (List<Home>) result[1];

				SimpleForm.Builder builder = SimpleForm.builder();

				List<Home> sorted = new ArrayList<>();
				if (phomeslist != null && !phomeslist.isEmpty()) {
					if ( filterCategory.equals("all") ) {
						sorted.addAll(phomeslist);
					} else {
						for (Home home : phomeslist) {
							String cat = null;
							Map<String, String> tags = home.getMeta().getTags();
							if (tags != null) cat = tags.get("blueforms:category");
							if (filterCategory.equals(cat)) sorted.add(home);
						}
					}
                	int phomes = sorted.size();
					if (sorted != null && !sorted.isEmpty()) {
						if (filterCategory.equals("all")) {
							builder.title(lang.get("forms.home.phomelist.title", phomes));
							builder.content(lang.get("forms.home.phomelist.content.templateall", phomes));
						} else {
							builder.title(lang.get("forms.home.phomelist.title", lang.get("forms.home.categories." + filterCategory + ".button", phomes)));
							builder.content(lang.get("forms.home.phomelist.content.template", lang.get("forms.home.categories." + filterCategory + ".name"), phomes));
						}
					} else {
						builder.title(lang.get("forms.home.phomelist.title", 0));
						builder.content(lang.get("forms.home.phomelist.empty"));
					}
					sorted.sort(Comparator.comparing(h -> h.getName().toLowerCase(Locale.ROOT)));
					for (Home home : sorted) {
						if (home.getName() == null || home.getName().isEmpty()) continue;
						String name = home.getName().replace("&", "§");
						String iconDef = BedrockIconUtils.resolveHomeIcon(home);
						String[] parts = iconDef.split(";", 2);
						if (parts.length == 2) {
							switch (parts[0].toUpperCase(Locale.ROOT)) {
								case "URL":
									builder.button(name, FormImage.Type.URL, parts[1]);
									break;
								case "PATH":
									builder.button(name, FormImage.Type.PATH, parts[1]);
									break;
								default:
									builder.button(name, FormImage.Type.PATH, "textures/blocks/stone.png");
									break;
							}
						} else {
							builder.button(name, FormImage.Type.PATH, "textures/blocks/stone.png");
						}
					}
				} else {
					builder.title(lang.get("forms.home.phomelist.title", 0));
					builder.content(lang.get("forms.home.phomelist.empty"));

				}

				builder.validResultHandler((form, response) -> {
					int id = response.clickedButtonId();
					int homeCount = sorted.size();
					if (id < homeCount) {
						if (id < 0) {
							player.sendMessage(lang.get("forms.etc.unknownoption"));
							return;
						}
						Home selected = sorted.get(id);
						new HomeHomeDetailForm(player, lang, "phome", filterCategory, selected).open();
						return;
					}
				});

				builder.closedOrInvalidResultHandler((form, response) -> {
					new HomePhomeSelectForm(player, lang).open();
				});

				FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
			}));
	}
}
