package net.mcblueice.blueforms.forms.residence.create;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.*;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.selection.SelectionManager;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.forms.residence.ResidenceMainForm;

public class ResidenceCreateMainForm {
    private final Player player;
    private final ConfigManager lang;
    private final Residence residence = Residence.getInstance();

    public ResidenceCreateMainForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        SimpleForm.Builder builder = SimpleForm.builder()
            .title(lang.get("forms.residence.create.main.title"))
            .content(generateContent());

        List<String> buttonKeys = new ArrayList<>(lang.getSectionKeys("forms.residence.create.main.buttons"));
        for (String key : buttonKeys) {
            String name = lang.get("forms.residence.create.main.buttons." + key + ".name");
            String icon = lang.get("forms.residence.create.main.buttons." + key + ".icon");
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
                case "auto":
                    Bukkit.dispatchCommand(player, "residence:residence select auto");
                    break;
                case "select":
                    new ResidenceCreateSelectForm(player, lang).open();
                    break;
                case "manual":
                    new ResidenceCreateManualForm(player, lang).open();
                    break;
                case "expand":
                    new ResidenceCreateExpandForm(player, lang).open();
                    break;
                case "confirm":
                    new ResidenceCreateConfirmForm(player, lang).open();
                    break;
                default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    break;
            }
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private String generateContent() {
        PermissionGroup group = residence.getPlayerManager().getResidencePlayer(player).getGroup();
        SelectionManager.Selection selection = residence.getSelectionManager().getSelection(player);

        Location loc1 = selection.getBaseLoc1();
        Location loc2 = selection.getBaseLoc2();

        if (loc1 == null || loc2 == null) {
            return lang.get("forms.residence.create.main.content.not-selected");
        }

        String rawWorldName = (loc1.getWorld() != null) ? loc1.getWorld().getName() : null;
        String worldName = lang.get("forms.etc.unknownworld");
        if (rawWorldName != null) {
            String worldLang = lang.get("forms.etc.worlds." + rawWorldName);
            if (worldLang != null && !worldLang.isEmpty() && !worldLang.equals("forms.etc.worlds." + rawWorldName)) {
                worldName = worldLang;
            }
        }

        int xSize = selection.getBaseArea().getXSize();
        int ySize = selection.getBaseArea().getYSize();
        int zSize = selection.getBaseArea().getZSize();
        int Size = xSize * ySize * zSize;
        double cost = selection.getBaseArea().getCost(group);

        return lang.get("forms.residence.create.main.content.template",
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
            String.valueOf(Size),
            String.valueOf(cost)
        );
    }
}