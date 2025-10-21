package net.mcblueice.blueforms.forms.residence.manage.admin;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManageAdminGiveForm {
	private final Player player;
	private final ConfigManager lang;
	private final ClaimedResidence claimedResidence;

	public ResidenceManageAdminGiveForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
		this.player = player;
		this.lang = lang;
		this.claimedResidence = claimedResidence;
	}

	public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.isOwner(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.admin.give.nopermissionmessage", claimedResidence.getName()));
			return;
		}

		List<String> adminList = new ArrayList<>();
		if (claimedResidence != null) {
			List<String> admins = ResidenceUtils.getResAdminPlayerName(claimedResidence);
			if (admins != null) adminList.addAll(admins);
		}
		adminList.sort(String.CASE_INSENSITIVE_ORDER);

		List<String> trustedList = new ArrayList<>();
		if (claimedResidence != null) {
			List<String> trusted = ResidenceUtils.getResTrustedPlayerName(claimedResidence);
			if (trusted != null) trustedList.addAll(trusted);
		}
		trustedList.sort(String.CASE_INSENSITIVE_ORDER);

		List<String> inResList = new ArrayList<>(ResidenceUtils.getInResidencePlayerNames(claimedResidence));
		inResList.sort(String.CASE_INSENSITIVE_ORDER);

		List<String> OnlineList = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player == null || player.getName() == null) continue;
			OnlineList.add(player.getName());
		}
		OnlineList.sort(String.CASE_INSENSITIVE_ORDER);

		LinkedHashMap<String, String> displayByRaw = new LinkedHashMap<>();

		for (String n : adminList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.AdminColor") + n + "§r");
		for (String n : trustedList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.TrustedColor") + n + "§r");
		for (String n : inResList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.InResColor") + n + "§r");
		for (String n : OnlineList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.OnlineColor") + n + "§r");

		List<String> candidates = new ArrayList<>(displayByRaw.keySet());
		List<String> options = new ArrayList<>(displayByRaw.values());

		CustomForm.Builder builder = CustomForm.builder()
			.title(lang.get("forms.residence.manage.pset.select.title", claimedResidence.getName()))
			.dropdown(generateContent() + "\n" + lang.get("forms.residence.manage.pset.select.dropdown"), options);

		builder.validResultHandler((form, response) -> {
			int dropdown = response.asDropdown(0);
			String targetPlayer = null;
            if (dropdown >= 0 && dropdown < candidates.size()) targetPlayer = candidates.get(dropdown);

			if (targetPlayer == null || targetPlayer.isEmpty()) {
				player.sendMessage(lang.get("forms.residence.manage.pset.select.invalid"));
				return;
			}
            Player target = Bukkit.getPlayerExact(targetPlayer);
            if (target == null || !target.isOnline()) {
                player.sendMessage(lang.get("forms.residence.manage.pset.select.playeroffline", targetPlayer));
                return;
            }
            openConfirm(targetPlayer);
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

        return lang.get("forms.residence.manage.admin.give.content.template",
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

    private void openConfirm(String targetPlayer) {
        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.manage.admin.give.confirm.title"))
            .input(lang.get("forms.residence.manage.admin.give.confirm.content", claimedResidence.getName()) + "\n" + lang.get("forms.residence.manage.admin.give.confirm.input1"), lang.get("forms.residence.manage.admin.give.confirm.input2"));

        builder.validResultHandler((form, response) -> {
            String input = response.asInput(0);
            if (input.trim().equals("give")) {
                player.sendMessage(lang.get("forms.residence.manage.admin.give.confirm.success", claimedResidence.getName()));
                Residence.getInstance().getResidenceManager().giveResidence(player, targetPlayer, claimedResidence, false, false);
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
