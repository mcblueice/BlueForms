package net.mcblueice.blueforms.forms.residence.manage.admin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskScheduler;
import net.mcblueice.blueforms.utils.ResidenceUtils;

import java.util.Arrays;
import java.util.List;

public class ResidenceManageAdminExpandForm {
    private final Player player;
    private final ConfigManager lang;
    private final ClaimedResidence claimedResidence;

    public ResidenceManageAdminExpandForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
        this.player = player;
        this.lang = lang;
        this.claimedResidence = claimedResidence;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.canManage(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.admin.expand.nopermissionmessage", claimedResidence.getName()));
			return;
		}

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
            lang.get("forms.residence.manage.admin.expand.currentfacing", facing),
            lang.get("forms.etc.north"),
            lang.get("forms.etc.east"),
            lang.get("forms.etc.south"),
            lang.get("forms.etc.west"),
            lang.get("forms.etc.up"),
            lang.get("forms.etc.down")
        );

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.manage.admin.expand.title"));

        builder
            .dropdown(generateContent() + "\n" + lang.get("forms.residence.manage.admin.expand.dropdown"), directions)
            .input(lang.get("forms.residence.manage.admin.expand.input1"), lang.get("forms.residence.manage.admin.expand.input2"));

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
                TaskScheduler.dispatchCommand(player, Bukkit.getPluginManager().getPlugin("BlueForms"), "residence:residence expand " + amount);
            } else {
                amount = -amount;
                TaskScheduler.dispatchCommand(player, Bukkit.getPluginManager().getPlugin("BlueForms"), "residence:residence contract " + amount);
            }
            player.teleport(originalLocation);
            new ResidenceManageAdminMainForm(player, lang, claimedResidence).open();
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceManageAdminMainForm(player, lang, claimedResidence).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private String generateContent() {

        Location loc1 = claimedResidence.getMainArea().getLowLocation();
        Location loc2 = claimedResidence.getMainArea().getHighLocation();
        String rawWorldName = claimedResidence.getMainArea().getWorldName();
        String worldName = lang.get("forms.etc.unknownworld");
        if (rawWorldName != null) {
            String worldLang = lang.get("forms.etc.worlds." + rawWorldName);
            if (worldLang != null && !worldLang.isEmpty() && !worldLang.equals("forms.etc.worlds." + rawWorldName)) {
                worldName = worldLang;
            }
        }

        int xSize = claimedResidence.getMainArea().getXSize();
        int ySize = claimedResidence.getMainArea().getYSize();
        int zSize = claimedResidence.getMainArea().getZSize();
        int Size = xSize * ySize * zSize;

        return lang.get("forms.residence.manage.admin.expand.content.template",
            claimedResidence.getName(),
            worldName,
            loc1.getBlockX(),
            loc1.getBlockY(),
            loc1.getBlockZ(),
            loc2.getBlockX(),
            loc2.getBlockY(),
            loc2.getBlockZ(),
            String.valueOf(xSize),
            String.valueOf(ySize),
            String.valueOf(zSize),
            String.valueOf(Size)
        );
    }
}
