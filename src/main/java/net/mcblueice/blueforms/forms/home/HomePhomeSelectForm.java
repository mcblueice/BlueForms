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

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskSchedulerUtils;

public class HomePhomeSelectForm {
    private final Player player;
    private final ConfigManager lang;
    public HomePhomeSelectForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		HuskHomesAPI huskhomes = HuskHomesAPI.getInstance();
		CompletableFuture<List<Home>> phomelist = huskhomes.getPublicHomes();
		phomelist.thenAccept(result -> 
            TaskSchedulerUtils.runTask(player, Bukkit.getPluginManager().getPlugin("BlueForms"), () -> {
				List<Home> phomeslist = (List<Home>) result;
                
                SimpleForm.Builder builder = SimpleForm.builder()
                    .title(lang.get("forms.home.phomeselect.title"));

                Map<String, Integer> categoryIndexMap = new HashMap<>();
                List<String> categoryKeys = new ArrayList<>(lang.getSectionKeys("forms.home.categories"));

                for (String key : categoryKeys) {
                    int count = 0;
                    for (Home home : phomeslist) {
                        String cat = null;
                        Map<String, String> tags = home.getMeta().getTags();
                        if (tags != null) cat = tags.get("blueforms:category");
                        if (key.equals(cat)) count++;
                    }
                    categoryIndexMap.put(key, count);
                }

                List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.home.categories"));
                for (String key : buttonKeys) {
                    String name = lang.get("forms.home.categories." + key + ".button", categoryIndexMap.get(key));
                    String icon = lang.get("forms.home.categories." + key + ".icon");
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

                String categoryallname = lang.get("forms.home.phomeselect.buttons.all.name", phomeslist.size());
                String categoryallicon = lang.get("forms.home.phomeselect.buttons.all.icon");
                switch (categoryallicon.split(";")[0].toUpperCase()) {
                    case "URL":
                        builder.button(categoryallname, FormImage.Type.URL, categoryallicon.substring(4));
                        break;
                    case "PATH":
                        builder.button(categoryallname, FormImage.Type.PATH, categoryallicon.substring(5));
                        break;
                    default:
                        builder.button(categoryallname);
                        break;
                }
                builder.validResultHandler((form, response) -> {
                    int id = response.clickedButtonId();
                    if (id > buttonKeys.size()-1) {
                        new HomePhomelistForm(player, lang, "all").open();
                    } else {
                        String key = buttonKeys.get(id);
                        new HomePhomelistForm(player, lang, key).open();
                    }
                });

				builder.closedOrInvalidResultHandler((form, response) -> {
					new HomeMainForm(player, lang).open();
				});

                FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
            }));
    }
}