package net.mcblueice.blueforms.forms.residence.manage.admin;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.*;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManageAdminRemoveForm {
    private final Player player;
    private final ConfigManager lang;
    private final ClaimedResidence claimedResidence;

    public ResidenceManageAdminRemoveForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
        this.player = player;
        this.lang = lang;
        this.claimedResidence = claimedResidence;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.isOwner(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.admin.remove.nopermissionmessage", claimedResidence.getName()));
			return;
		}

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.residence.manage.admin.remove.title"))
            .content(generateContent());

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.residence.manage.admin.remove.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.residence.manage.admin.remove.buttons." + key + ".name");
            String icon = lang.get("forms.residence.manage.admin.remove.buttons." + key + ".icon");
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

        builder.validResultHandler((form, response) -> {
            int id = response.clickedButtonId();
            String key = buttonKeys.get(id);
            switch (key) {
                case "confirm":
                    openConfirm();
                    break;
                case "cancel":
                    new ResidenceManageAdminMainForm(player, lang, claimedResidence).open();
                    break;
                default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    break;
            }
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

        String name = claimedResidence.getName();
        String owner = claimedResidence.getOwner();
        long createTime = claimedResidence.getCreateTime();
        String createDate = createTime > 0
            ? java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .format(java.time.Instant.ofEpochMilli(createTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime())
            : lang.get("forms.etc.unknowndate");

        List<String> adminNames = ResidenceUtils.getResAdminPlayerName(claimedResidence);
        String adminList = adminNames.isEmpty() ? lang.get("forms.etc.none") : String.join(", ", adminNames);
        List<String> trustedNames = ResidenceUtils.getResTrustedPlayerName(claimedResidence);
        String trustedList = trustedNames.isEmpty() ? lang.get("forms.etc.none") : String.join(", ", trustedNames);
        List<String> inResColored = new ArrayList<>(ResidenceUtils.getInResidencePlayerColoredMap(claimedResidence, lang).values());
        String inResList = inResColored.isEmpty() ? lang.get("forms.etc.none") : String.join(", ", inResColored);

        return lang.get("forms.residence.manage.admin.remove.content.template",
            name,
            createDate,
            owner,
            adminList,
            trustedList,
            inResList,
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

    private void openConfirm() {
        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.manage.admin.remove.confirm.title"))
            .input(lang.get("forms.residence.manage.admin.remove.confirm.content", claimedResidence.getName()) + "\n" + lang.get("forms.residence.manage.admin.remove.confirm.input1"), lang.get("forms.residence.manage.admin.remove.confirm.input2"));

        builder.validResultHandler((form, response) -> {
            String input = response.asInput(0);
            if (input.trim().equals("delete")) {
                player.sendMessage(lang.get("forms.residence.manage.admin.remove.confirm.success", claimedResidence.getName()));
                Residence.getInstance().getResidenceManager().removeResidence(claimedResidence);
                return;
            } else {
                new ResidenceManageAdminMainForm(player, lang, claimedResidence).open();
            }
            
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceManageAdminMainForm(player, lang, claimedResidence).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}