package net.mcblueice.blueforms.forms.residence.create;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;

import java.util.Arrays;
import java.util.List;

public class ResidenceCreateExpandForm {
    private final Player player;
    private final ConfigManager lang;

    public ResidenceCreateExpandForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        double yaw = player.getLocation().getYaw() + 180;
        double pitch = player.getLocation().getPitch() + 90;
        String facing = lang.get("forms.etc.unknownfacing");
        if ((yaw >= 315 && yaw <= 360) || (yaw >= 0 && yaw < 45)) facing = lang.get("forms.etc.north");
        if ( yaw >= 45 && yaw < 135) facing = lang.get("forms.etc.east");
        if ( yaw >= 135 && yaw < 225) facing = lang.get("forms.etc.south");
        if ( yaw >= 225 && yaw < 315) facing = lang.get("forms.etc.west");
        if ( pitch < 5 ) facing = lang.get("forms.etc.up");
        if ( pitch > 175 ) facing = lang.get("forms.etc.down");

        List<String> directions = Arrays.asList(
            lang.get("forms.residence.create.expand.currentfacing", facing),
            lang.get("forms.etc.north"),
            lang.get("forms.etc.east"),
            lang.get("forms.etc.south"),
            lang.get("forms.etc.west"),
            lang.get("forms.etc.up"),
            lang.get("forms.etc.down")
        );

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.create.expand.title"));

        builder.dropdown(lang.get("forms.residence.create.expand.dropdown"), directions)
            .input(lang.get("forms.residence.create.expand.input1"), lang.get("forms.residence.create.expand.input2"));

        builder.validResultHandler((form, response) -> {
            int directionIndex = response.asDropdown(0);
            String input1 = response.asInput(1);
            if (!input1.matches("-?\\d+(\\.\\d+)?")) {
                player.sendMessage(lang.get("forms.etc.unknownpos"));
                return;
            }
            int amount = Integer.parseInt(input1);
            if (amount == 0) {
                player.sendMessage(lang.get("forms.etc.unknownpos"));
                return;
            }
            Location originalLocation = player.getLocation();
            Location loc = player.getLocation();
            switch (directionIndex) {
                case 0: break; // 目前面向
                case 1: loc.setYaw(180f); loc.setPitch(0f); player.teleport(loc); break; // 北
                case 2: loc.setYaw(-90f); loc.setPitch(0f); player.teleport(loc); break; // 東
                case 3: loc.setYaw(0f); loc.setPitch(0f); player.teleport(loc); break; // 南
                case 4: loc.setYaw(90f); loc.setPitch(0f); player.teleport(loc); break; // 西
                case 5: loc.setPitch(-90f); player.teleport(loc); break; // 上
                case 6: loc.setPitch(90f); player.teleport(loc); break; // 下
                default: break;
            }
            if (amount > 0) {
                Bukkit.dispatchCommand(player, "residence:residence select expand " + amount);
            } else {
                amount = -amount;
                Bukkit.dispatchCommand(player, "residence:residence select contract " + amount);
            }
            player.teleport(originalLocation);
            new ResidenceCreateMainForm(player, lang).open();
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceCreateMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
