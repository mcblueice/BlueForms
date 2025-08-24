package net.mcblueice.blueforms.forms.residence;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceTeleportForm {
    private final Player player;
    private final ConfigManager lang;

    public ResidenceTeleportForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        List<String> residenceList = new ArrayList<>();
        String currentResidence = ResidenceUtils.getCurrentResidenceName(player);
        boolean hasCurrentResidence = currentResidence != null && !currentResidence.isEmpty();
        if (hasCurrentResidence) residenceList.add(lang.get("forms.residence.teleport.current", currentResidence));
        residenceList.add(lang.get("forms.residence.teleport.manual"));
        residenceList.addAll(ResidenceUtils.getPlayerResListNames(player));

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.teleport.title"))
            .dropdown(lang.get("forms.residence.teleport.dropdown"), residenceList)
            .input(lang.get("forms.residence.teleport.input1"), lang.get("forms.residence.teleport.input2"));

        builder.validResultHandler((form, response) -> {
            int dropdown = response.asDropdown(0);
            String input = response.asInput(1);
            String residence = null;
            int offset = hasCurrentResidence ? 2 : 1;
            if (hasCurrentResidence && dropdown == 0) residence = currentResidence;
            if ((hasCurrentResidence && dropdown == 1) || (!hasCurrentResidence && dropdown == 0)) residence = input.trim();
            if ((hasCurrentResidence && dropdown >= 2) || (!hasCurrentResidence && dropdown >= 1)) residence = ResidenceUtils.getPlayerResListNames(player).get(dropdown - offset);

            if (residence == null || residence.isEmpty()) {
                player.sendMessage(lang.get("forms.residence.teleport.invalid"));
                return;
            }
            Bukkit.dispatchCommand(player, "residence:residence tp " + residence);
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
