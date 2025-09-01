package net.mcblueice.blueforms.forms.residence.search;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskScheduler;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceSearchShowForm {
    private final Player player;
    private final ConfigManager lang;

    public ResidenceSearchShowForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        List<String> residenceList = new ArrayList<>();
        String currentResidence = ResidenceUtils.getCurrentResidenceName(player);
        boolean hasCurrentResidence = currentResidence != null && !currentResidence.isEmpty();
        if (hasCurrentResidence) residenceList.add(lang.get("forms.residence.search.show.current", currentResidence));
        residenceList.add(lang.get("forms.residence.search.show.manual"));
        residenceList.addAll(ResidenceUtils.getPlayerResListNames(player));

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.search.show.title"))
            .dropdown(lang.get("forms.residence.search.show.dropdown"), residenceList)
            .input(lang.get("forms.residence.search.show.input1"), lang.get("forms.residence.search.show.input2"));

        builder.validResultHandler((form, response) -> {
            int dropdown = response.asDropdown(0);
            String input = response.asInput(1);
            String residence = null;
            int offset = hasCurrentResidence ? 2 : 1;
            if (hasCurrentResidence && dropdown == 0) residence = currentResidence;
            if ((hasCurrentResidence && dropdown == 1) || (!hasCurrentResidence && dropdown == 0)) residence = input.trim();
            if ((hasCurrentResidence && dropdown >= 2) || (!hasCurrentResidence && dropdown >= 1)) residence = ResidenceUtils.getPlayerResListNames(player).get(dropdown - offset);

            if (residence == null || residence.isEmpty()) {
                player.sendMessage(lang.get("forms.residence.search.show.invalid"));
                return;
            }
            TaskScheduler.dispatchCommand(player, Bukkit.getPluginManager().getPlugin("BlueForms"), "residence:residence show " + residence);
            player.sendMessage(lang.get("forms.residence.search.show.message", residence));
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceSearchMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
