package net.mcblueice.blueforms.forms.residence.create;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;

public class ResidenceCreateSelectForm {
    private final Player player;
    private final ConfigManager lang;

    public ResidenceCreateSelectForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        double yaw = player.getLocation().getYaw() + 180;
        String facing = lang.get("forms.etc.unknownfacing");
        if ((yaw >= 315 && yaw <= 360) || (yaw >= 0 && yaw < 45)) facing = lang.get("forms.etc.north");
        if ( yaw >= 45 && yaw < 135) facing = lang.get("forms.etc.east");
        if ( yaw >= 135 && yaw < 225) facing = lang.get("forms.etc.south");
        if ( yaw >= 225 && yaw < 315) facing = lang.get("forms.etc.west");

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.create.select.title"))
            .input(lang.get("forms.residence.create.select.content", facing) + "\n" + lang.get("forms.residence.create.select.inputX1"), lang.get("forms.residence.create.select.inputX2"))
            .input(lang.get("forms.residence.create.select.inputZ1"), lang.get("forms.residence.create.select.inputZ2"))
            .input(lang.get("forms.residence.create.select.inputY1"), lang.get("forms.residence.create.select.inputY2"));

        builder.validResultHandler((form, response) -> {
            String X = response.asInput(0);
            String Z = response.asInput(1);
            String Y = response.asInput(2);
            if (X == null || !X.matches("\\d+") ||
                Y == null || !Y.matches("\\d+") ||
                Z == null || !Z.matches("\\d+")) {
                player.sendMessage(lang.get("forms.etc.unknownpos"));
                return;
            }
            Bukkit.dispatchCommand(player, "residence:residence select " + X + " " + Y + " " + Z);
            new ResidenceCreateMainForm(player, lang).open();
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceCreateMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
