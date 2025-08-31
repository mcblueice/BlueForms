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
import net.mcblueice.blueforms.utils.TaskScheduler;

public class HomeHomelistForm {
	private final Player player;
	private final ConfigManager lang;

	public HomeHomelistForm(Player player, ConfigManager lang) {
		this.player = player;
		this.lang = lang;
	}

	public void open() {
		if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;
		HuskHomesAPI huskhomes = HuskHomesAPI.getInstance();
		OnlineUser user = huskhomes.adaptUser(player);
		CompletableFuture<List<Home>> homelist = huskhomes.getUserHomes(user);
		CompletableFuture<List<Home>> phomelist = huskhomes.getPublicHomes();
		homelist.thenCombine(phomelist, (homes, phomes) -> new Object[]{homes, phomes})
			.thenAccept(result -> TaskScheduler.runTask(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
				if (!player.isOnline()) return;
				@SuppressWarnings("unchecked")
				List<Home> homeslist = (List<Home>) result[0];
                int homes = homeslist.size();
                int maxhomes = huskhomes.getMaxHomeSlots(user);
				boolean cansethome = ((maxhomes > homes) || player.hasPermission("huskhomes.bypass_home_limit")) && player.hasPermission("huskhomes.command.sethome");

				SimpleForm.Builder builder = SimpleForm.builder()
					.title(lang.get("forms.home.homelist.title"));

				List<Home> sorted = new ArrayList<>();
				if (homeslist != null && !homeslist.isEmpty()) {
					builder.content(lang.get("forms.home.homelist.content.template", homes, maxhomes, cansethome));
					sorted.addAll(homeslist);
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
					builder.content(lang.get("forms.home.homelist.empty"));
				}

				List<String> footerKeys = new ArrayList<>(lang.getSectionKeys("forms.home.homelist.buttons"));
				List<String> appliedFooterKeys = new ArrayList<>();
				for (String key : footerKeys) {
					if (key.equalsIgnoreCase("add")) if (!cansethome) continue;
					String name = lang.get("forms.home.homelist.buttons." + key + ".name");
					if (name == null) name = lang.get("forms.home.homelist.buttons." + key);
					String icon = lang.get("forms.home.homelist.buttons." + key + ".icon");
					if (icon == null) icon = "NONE";
					switch (icon.split(";")[0].toUpperCase(Locale.ROOT)) {
						case "URL":
							builder.button(name, FormImage.Type.URL, icon.length() >= 5 ? icon.substring(4) : "");
							break;
						case "PATH":
							builder.button(name, FormImage.Type.PATH, icon.length() >= 6 ? icon.substring(5) : "");
							break;
						default:
							builder.button(name);
							break;
					}
					appliedFooterKeys.add(key);
				}
				final List<String> bottomButtonKeys = Collections.unmodifiableList(appliedFooterKeys);

				builder.validResultHandler((form, response) -> {
					int id = response.clickedButtonId();
					int homeCount = sorted.size();
					if (id < homeCount) {
						if (id < 0) {
							player.sendMessage(lang.get("forms.etc.unknownoption"));
							return;
						}
						Home selected = sorted.get(id);
						new HomeHomeDetailForm(player, lang, "home", "all", selected).open();
						return;
					}

					int footerIndex = id - homeCount;
					String key = bottomButtonKeys.get(footerIndex);
					switch (key.toLowerCase(Locale.ROOT)) {
						case "add":
						    new HomeCreateForm(player, lang).open();
							break;
						default:
							player.sendMessage(lang.get("forms.etc.unknownoption"));
							break;
					}
				});

				builder.closedOrInvalidResultHandler((form, response) -> {
					new HomeMainForm(player, lang).open();
				});

				FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
			}));
	}
}
