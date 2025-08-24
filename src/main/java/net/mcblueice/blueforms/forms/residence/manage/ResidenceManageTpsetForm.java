package net.mcblueice.blueforms.forms.residence.manage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManageTpsetForm {
	private final Player player;
	private final ConfigManager lang;
	private final ClaimedResidence claimedResidence;

    public ResidenceManageTpsetForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
        this.player = player;
        this.lang = lang;
        this.claimedResidence = claimedResidence;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.canManage(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.tpset.nopermissionmessage", claimedResidence.getName()));
			return;
		}
        if (ResidenceUtils.getCurrentResidenceName(player) != claimedResidence.getName()) {
            player.sendMessage(lang.get("forms.residence.manage.tpset.inres"));
            return;
        }

        String ResWorld = claimedResidence.getMainArea().getWorldName();
        int Resloc1X = claimedResidence.getMainArea().getLowLocation().getBlockX();
        int Resloc1Y = claimedResidence.getMainArea().getLowLocation().getBlockY();
        int Resloc1Z = claimedResidence.getMainArea().getLowLocation().getBlockZ();
        int Resloc2X = claimedResidence.getMainArea().getHighLocation().getBlockX();
        int Resloc2Y = claimedResidence.getMainArea().getHighLocation().getBlockY();
        int Resloc2Z = claimedResidence.getMainArea().getHighLocation().getBlockZ();
        String worldName = lang.get("forms.etc.unknownworld");
        String worldLang = lang.get("forms.etc.worlds." + ResWorld);
        if (worldLang != null && !worldLang.isEmpty() && !worldLang.equals("forms.etc.worlds." + ResWorld)) worldName = worldLang;

        int minX = Math.min(Resloc1X, Resloc2X);
        int maxX = Math.max(Resloc1X, Resloc2X);
        int minY = Math.min(Resloc1Y, Resloc2Y);
        int maxY = Math.max(Resloc1Y, Resloc2Y);
        int minZ = Math.min(Resloc1Z, Resloc2Z);
        int maxZ = Math.max(Resloc1Z, Resloc2Z);

        String playerWorld = player.getLocation().getWorld().getName();
        Location playerloc = player.getLocation();

        List<String> tpsetlist = new ArrayList<>();
        tpsetlist.add(lang.get("forms.residence.manage.tpset.current", playerloc.getBlockX(), playerloc.getBlockY(), playerloc.getBlockZ()));
        tpsetlist.add(lang.get("forms.residence.manage.tpset.manual"));

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.manage.tpset.title", claimedResidence.getName()))
            .dropdown(lang.get("forms.residence.manage.tpset.content.template",
            claimedResidence.getName(),
            worldName,
            Resloc1X,
            Resloc1Y,
            Resloc1Z,
            Resloc2X,
            Resloc2Y,
            Resloc2Z
            ) + "\n" + lang.get("forms.residence.manage.tpset.dropdown"), tpsetlist)
            .input(lang.get("forms.residence.manage.tpset.input1"), lang.get("forms.residence.manage.tpset.input2"));

        builder.validResultHandler((form, response) -> {
            int dropdown = response.asDropdown(0);
            String input = response.asInput(1);
            if (ResidenceUtils.getCurrentResidenceName(player) != claimedResidence.getName()) {
                 player.sendMessage(lang.get("forms.residence.manage.tpset.inres"));
                return;
            }
            Location tpsetpos;
            Location targetLoc;
            int x;
            int y;
            int z;
            switch (dropdown) {
            case 0:
                x = playerloc.getBlockX();
                y = playerloc.getBlockY();
                z = playerloc.getBlockZ();
                targetLoc = playerloc;
                break;
            case 1:
                tpsetpos = ResidenceUtils.stringToBlockLoc(player.getWorld(), input);
                if (tpsetpos == null) {
                    player.sendMessage(lang.get("forms.etc.unknownpos"));
                    return;
                }
                x = tpsetpos.getBlockX();
                y = tpsetpos.getBlockY();
                z = tpsetpos.getBlockZ();
                targetLoc = tpsetpos;
                break;
            default:
                player.sendMessage(lang.get("forms.etc.unknownoption"));
                return;
            }

            if (!(ResWorld.equals(playerWorld) && x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ)) {
                player.sendMessage(lang.get("forms.residence.manage.tpset.invalid"));
                return;
            }

            Location originalLoc = player.getLocation();
            float originalyaw = player.getLocation().getYaw();
            float originalpitch = player.getLocation().getPitch();
            Location centered = new Location(
                targetLoc.getWorld(),
                targetLoc.getBlockX() + 0.5,
                targetLoc.getBlockY(),
                targetLoc.getBlockZ() + 0.5,
                originalyaw,
                originalpitch
            );

            player.teleport(centered);
            claimedResidence.setTpLoc(player, false);
            player.teleport(originalLoc);
            player.sendMessage(lang.get("forms.residence.manage.tpset.success", targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ()));
            return;
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceManageMainForm(player, lang, claimedResidence).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
