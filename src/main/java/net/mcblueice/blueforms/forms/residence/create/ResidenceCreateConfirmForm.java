package net.mcblueice.blueforms.forms.residence.create;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.selection.SelectionManager;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.TaskScheduler;

public class ResidenceCreateConfirmForm {
    private final Player player;
    private final ConfigManager lang;
    private final Residence residence = Residence.getInstance();

    public ResidenceCreateConfirmForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.create.confirm.title"))
            .input(generateContent() + "\n\n" + lang.get("forms.residence.create.confirm.input1"), lang.get("forms.residence.create.confirm.input2"));

        builder.validResultHandler((form, response) -> {
            String input1 = response.asInput(0);
            if (input1 == null || !input1.matches("[A-Za-z0-9_-]+") || input1.length() > 32) {
                player.sendMessage(lang.get("forms.residence.create.confirm.invalid"));
                return;
            }
            TaskScheduler.dispatchCommand(player, Bukkit.getPluginManager().getPlugin("BlueForms"), "residence:residence create " + input1);
            return;
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceCreateMainForm(player, lang).open();
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
