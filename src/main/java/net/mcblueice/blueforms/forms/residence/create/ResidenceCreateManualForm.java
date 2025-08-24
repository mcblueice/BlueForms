package net.mcblueice.blueforms.forms.residence.create;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.selection.SelectionManager;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceCreateManualForm {
    private final Player player;
    private final ConfigManager lang;
    private final SelectionManager selectionManager = Residence.getInstance().getSelectionManager();

    public ResidenceCreateManualForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.create.manual.title"))
            .input(lang.get("forms.residence.create.manual.input1"), lang.get("forms.residence.create.manual.input2"))
            .input(lang.get("forms.residence.create.manual.input3"), lang.get("forms.residence.create.manual.input4"));

        builder.validResultHandler((form, response) -> {
            String input1 = response.asInput(0);
            String input2 = response.asInput(1);

            Location loc1 = ResidenceUtils.stringToBlockLoc(player.getWorld(), input1), loc2 = ResidenceUtils.stringToBlockLoc(player.getWorld(), input2);
            if (loc1 == null || loc2 == null) {
                player.sendMessage(lang.get("forms.etc.unknownpos"));
                return;
            }
            selectionManager.clearSelection(player);
            selectionManager.getSelection(player).setBaseLoc1(loc1);
            selectionManager.getSelection(player).setBaseLoc2(loc2);
            new ResidenceCreateMainForm(player, lang).open();
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceCreateMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
