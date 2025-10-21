package net.mcblueice.blueforms.utils;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import net.mcblueice.blueforms.ConfigManager;

/**
 * ResidenceUtils 工具類
 * 提供領地相關查詢、權限、旗標等輔助方法
 */
public class ResidenceUtils {
    /**
     * 取得領地旗標狀態
     * @param player 玩家
     * @param residence 領地
     * @return 旗標狀態對應表
     */
    public static HashMap<String, FlagPermissions.FlagState> getResidenceFlags(Player player, ClaimedResidence residence) {
        if (residence == null) return new HashMap<>();
        List<String> flags = residence.getPermissions().getPosibleFlags(player, true, false);
        Map<String, Boolean> resFlags = new HashMap<>();
        Map<String, FlagPermissions.FlagState> TempPermMap = new LinkedHashMap<>();
        Map<String, Boolean> globalFlags = Residence.getInstance().getPermissionManager().getAllFlags().getFlags();
        for (Map.Entry<String, Boolean> one : residence.getPermissions().getFlags().entrySet()) {
            if (flags.contains(one.getKey())) resFlags.put(one.getKey(), one.getValue());
        }
        for (Map.Entry<String, Boolean> one : globalFlags.entrySet()) {
            String name = one.getKey();
            Flags flag = Flags.getFlag(name);
            if (flag != null && !flag.isGlobalyEnabled()) continue;
            if (!flags.contains(name)) continue;
            if (resFlags.containsKey(name)) {
                TempPermMap.put(name, resFlags.get(name) ? FlagPermissions.FlagState.TRUE : FlagPermissions.FlagState.FALSE);
            } else {
                TempPermMap.put(name, FlagPermissions.FlagState.NEITHER);
            }
        }
        TempPermMap.remove("admin");
        return new HashMap<>(TempPermMap);
    }

    /**
     * 取得特定玩家在領地的旗標狀態
     */
    public static HashMap<String, FlagPermissions.FlagState> getResidencePlayerFlags(Player player, String targetPlayer, ClaimedResidence residence) {
        if (residence == null) return new HashMap<>();
        Map<String, Boolean> globalFlags = new HashMap<>();
        for (Flags oneFlag : Flags.values()) {
            globalFlags.put(oneFlag.toString(), oneFlag.isEnabled());
        }
        List<String> flags = residence.getPermissions().getPosibleFlags(player, false, false);
        Map<String, Boolean> resFlags = new HashMap<>();
        for (Map.Entry<String, Boolean> one : residence.getPermissions().getFlags().entrySet()) {
            if (flags.contains(one.getKey())) resFlags.put(one.getKey(), one.getValue());
        }
        Set<String> PossibleResPFlags = FlagPermissions.getAllPosibleFlags();
        Map<String, Boolean> temp = new HashMap<>();
        for (String one : PossibleResPFlags) {
            if (globalFlags.containsKey(one)) temp.put(one, globalFlags.get(one));
        }
        globalFlags = temp;
        Map<String, Boolean> pFlags = residence.getPermissions().getPlayerFlags(targetPlayer);
        if (pFlags != null) resFlags.putAll(pFlags);
        LinkedHashMap<String, FlagPermissions.FlagState> TempPermMap = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> one : globalFlags.entrySet()) {
            if (!flags.contains(one.getKey())) continue;
            if (resFlags.containsKey(one.getKey())) {
                TempPermMap.put(one.getKey(), resFlags.get(one.getKey()) ? FlagPermissions.FlagState.TRUE : FlagPermissions.FlagState.FALSE);
            } else {
                TempPermMap.put(one.getKey(), FlagPermissions.FlagState.NEITHER);
            }
        }
        return new HashMap<>(TempPermMap);
    }

    /**
     * 旗標狀態與 int 互轉
     */
    public static int flagToInt(FlagPermissions.FlagState flag) {
        if (flag.equals(FlagPermissions.FlagState.FALSE)) return 0;
        if (flag.equals(FlagPermissions.FlagState.NEITHER)) return 1;
        if (flag.equals(FlagPermissions.FlagState.TRUE)) return 2;
        return 1;
    }
    public static FlagPermissions.FlagState intToFlag(int flag) {
        if (flag == 0) return FlagPermissions.FlagState.FALSE;
        if (flag == 1) return FlagPermissions.FlagState.NEITHER;
        if (flag == 2) return FlagPermissions.FlagState.TRUE;
        return FlagPermissions.FlagState.NEITHER;
    }

    /**
     * 取得玩家擁有、租用、受信任的所有領地
     */
    public static Map<String, ClaimedResidence> getPlayerResList(Player player) {
        Residence residence = Residence.getInstance();
        TreeMap<String, ClaimedResidence> OwnedResidences = residence.getPlayerManager().getResidencesMap(player.getName(), true, false, null); //擁有
        OwnedResidences.putAll(residence.getRentManager().getRentsMap(player.getName(), false, null)); //租用
        OwnedResidences.putAll(residence.getPlayerManager().getTrustedResidencesMap(player.getName(), true, false, null)); //受信任

        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        Map<String, ClaimedResidence> rentMap = residence.getRentManager().getRentsMap(playerName, false, null);

        OwnedResidences.entrySet().removeIf(e -> {
            String name = e.getKey();
            ClaimedResidence resObj = e.getValue();
            if (resObj == null) return true;
            ClaimedResidence realRes = Residence.getInstance().getResidenceManager().getByName(name);
            if (realRes == null) return true;
            boolean isOwner = realRes.isOwner(uuid);
            boolean isRenter = rentMap != null && rentMap.containsKey(name);
            // 每個領地分別檢查信任名單
            boolean isTrusted = false;
            Collection<com.bekvon.bukkit.residence.containers.ResidencePlayer> trustedPlayers = realRes.getTrustedPlayers();
            if (trustedPlayers != null) {
                for (com.bekvon.bukkit.residence.containers.ResidencePlayer rp : trustedPlayers) {
                    if (uuid.toString().equals(rp.getName()) || playerName.equalsIgnoreCase(rp.getName())) {
                        isTrusted = true;
                        break;
                    }
                }
            }
            return !(isOwner || isRenter || isTrusted);
        });
        return OwnedResidences;
    }
    public static List<String> getPlayerResListNames(Player player) {
        return List.copyOf(getPlayerResList(player).keySet());
    }

    /**
     * 取得玩家可管理的所有領地
     */
    public static HashMap<String, ClaimedResidence> getPlayerAdminResList(Player player) {
        HashMap<String, ClaimedResidence> hashMap = new HashMap<>();
        for (Map.Entry<String, ClaimedResidence> entry : getPlayerResList(player).entrySet()) {
            if (hasManagePermission(player, entry.getValue())) hashMap.put(entry.getKey(), entry.getValue());
        }
        return hashMap;
    }
    public static List<String> getPlayerAdminResListNames(Player player) {
        return List.copyOf(getPlayerAdminResList(player).keySet());
    }

    /**
     * 判斷玩家是否有管理權限
     */
    public static boolean hasManagePermission(Player player, ClaimedResidence residence) {
        return residence.isOwner(player.getUniqueId()) || residence.getPermissions().playerHas(player, Flags.admin, false);
    }

    /**
     * 綜合檢查：擁有 residence.admin 或在該領地具有管理權限
     */
    public static boolean canManage(Player player, ClaimedResidence residence) {
        return residence != null && (player.hasPermission("residence.admin") || hasManagePermission(player, residence));
    }
    public static boolean isOwner(Player player, ClaimedResidence residence) {
        return residence != null && (player.hasPermission("residence.admin") || residence.isOwner(player.getUniqueId()));
    }
    /**
     * 嘗試將 ResidencePlayer 內部儲存的字串轉為顯示名稱：若為 UUID 轉為玩家名，否則直接使用原字串
     */
    private static String resolveResidencePlayerName(ResidencePlayer rp) {
        if (rp == null) return null;
        String raw = rp.getName();
        if (raw == null) return null;
        try {
            UUID uuid = UUID.fromString(raw);
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            return name != null ? name : raw; // 若 OfflinePlayer 沒名字（從未上線）則 fallback 原字串
        } catch (IllegalArgumentException e) {
            // 非 UUID 格式，直接當作玩家名
            return raw;
        }
    }

    /**
     * 取得領地所有受信任玩家（不過濾非 UUID 條目）
     */
    public static List<ResidencePlayer> getResTrustedPlayer(ClaimedResidence residence) {
        if (residence == null) return Collections.emptyList();
        // 直接複製一份，並使用可解析名稱或原始值排序；不再因解析不到名稱而丟棄，避免「新增後沒顯示」的錯覺。
        List<ResidencePlayer> list = new ArrayList<>(residence.getTrustedPlayers());
        list.sort(Comparator.comparing(
            rp -> {
                String resolved = resolveResidencePlayerName(rp);
                return (resolved != null && !resolved.isEmpty()) ? resolved : rp.getName();
            },
            String.CASE_INSENSITIVE_ORDER
        ));
        return list;
    }

    /**
     * 取得領地所有受信任玩家名稱（含非 UUID）
     */
    public static List<String> getResTrustedPlayerName(ClaimedResidence residence) {
        return getResTrustedPlayer(residence).stream()
            .map(rp -> {
                String resolved = resolveResidencePlayerName(rp);
                return (resolved != null && !resolved.isEmpty()) ? resolved : rp.getName();
            })
            .filter(Objects::nonNull)
            .distinct() // 避免同 UUID 與名稱同時存在造成重複顯示
            .toList();
    }

    /**
     * 取得領地所有有管理權限的玩家 ResidencePlayer 物件（含非 UUID）
     */
    public static List<ResidencePlayer> getResAdminPlayer(ClaimedResidence residence) {
        if (residence == null) return Collections.emptyList();
        List<ResidencePlayer> adminlist = new ArrayList<>();
        for (ResidencePlayer rp : getResTrustedPlayer(residence)) {
            String name = resolveResidencePlayerName(rp);
            if (name == null) continue;
            if (residence.getPermissions().playerHas(name, Flags.admin, false)) adminlist.add(rp);
        }
        adminlist.sort(Comparator.comparing(rp -> resolveResidencePlayerName(rp), String.CASE_INSENSITIVE_ORDER));
        return adminlist;
    }

    /**
     * 取得領地所有有管理權限的玩家名稱
     */
    public static List<String> getResAdminPlayerName(ClaimedResidence residence) {
        return getResAdminPlayer(residence).stream()
            .map(ResidenceUtils::resolveResidencePlayerName)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 取得指定領地內目前線上玩家 Player 物件清單
     */
    public static List<Player> getInResidencePlayer(ClaimedResidence residence) {
        if (residence == null) return Collections.emptyList();
        List<Player> playerlist = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || player.getName() == null) continue;
            ClaimedResidence at = Residence.getInstance().getResidenceManager().getByLoc(player.getLocation());
            if (at != null && at.equals(residence)) playerlist.add(player);
        }
        playerlist.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return playerlist;
    }

    /**
     * 取得指定領地內目前線上玩家名稱清單（委派給上方 Player 版本）
     */
    public static List<String> getInResidencePlayerNames(ClaimedResidence residence) {
        return getInResidencePlayer(residence).stream()
            .map(Player::getName)
            .toList();
    }

    /**
     * 取得目前在領地內玩家的顯示名稱(含顏色)，顏色優先順序: Owner > Admin > Trusted > InRes
     * 回傳 LinkedHashMap 以保留玩家列表順序 (依 getInResidencePlayer 排序)
     * key: 原始玩家名稱  value: 加上顏色後的顯示字串 (結尾補 §r)
     */
    public static LinkedHashMap<String, String> getInResidencePlayerColoredMap(ClaimedResidence residence, ConfigManager lang) {
        LinkedHashMap<String, String> ordered = new LinkedHashMap<>();
        if (residence == null) return ordered;

        // 角色集合
        String owner = residence.getOwner();
        Set<String> adminSet = new HashSet<>(getResAdminPlayerName(residence));
        Set<String> trustedSet = new HashSet<>(getResTrustedPlayerName(residence));

        // 顏色設定
        String ownerColor = lang.get("forms.residence.colors.OwnerColor");
        String adminColor = lang.get("forms.residence.colors.AdminColor");
        String trustedColor = lang.get("forms.residence.colors.TrustedColor");
        String inResColor = lang.get("forms.residence.colors.OnlineColor");

        // 取得目前在領地內的玩家 (已按名稱排序)
        List<Player> inside = getInResidencePlayer(residence);

        // 分類列表
        List<String> ownerList = new ArrayList<>();
        List<String> adminList = new ArrayList<>();
        List<String> trustedList = new ArrayList<>();
        List<String> otherList = new ArrayList<>();

        for (Player player : inside) {
            String name = player.getName();
            if (name.equalsIgnoreCase(owner)) {
                ownerList.add(name);
            } else if (adminSet.contains(name)) {
                adminList.add(name);
            } else if (trustedSet.contains(name)) {
                trustedList.add(name);
            } else {
                otherList.add(name);
            }
        }

        // 各分類內再做一次 A-Z (忽略大小寫) 以保險 (inside 可能已排序但分類拆分後再排序避免意外)
        Comparator<String> cmp = String.CASE_INSENSITIVE_ORDER;
        adminList.sort(cmp);
        trustedList.sort(cmp);
        otherList.sort(cmp);
        // owner 只有一個，不需排序；若未來支援多 owner 再排序

        // 按分類順序加入 Map
        for (String n : ownerList) {
            ordered.put(n, ownerColor + n + "§r");
        }
        for (String n : adminList) {
            ordered.put(n, adminColor + n + "§r");
        }
        for (String n : trustedList) {
            ordered.put(n, trustedColor + n + "§r");
        }
        for (String n : otherList) {
            ordered.put(n, inResColor + n + "§r");
        }

        return ordered;
    }

    /**
     * 只取顏色顯示字串列表 (保留順序)
     */
    public static List<String> getInResidencePlayerColoredDisplays(ClaimedResidence residence, ConfigManager lang) {
        return new ArrayList<>(getInResidencePlayerColoredMap(residence, lang).values());
    }

    /**
     * 踢出玩家
     */
    public static void kickPlayer(String targetPlayer, ClaimedResidence residence) {
        Player player = Bukkit.getPlayer(targetPlayer);
        if (player == null) return;
        if (!residence.getPlayersInResidence().contains(player)) return;
        player.closeInventory();
        residence.kickFromResidence(player);
    }

    /**
     * 清除某玩家在指定領地的所有自訂旗標(含 trusted / admin)
     * executor 可為觸發此操作的玩家(用於需要 executor 參數的 API)，若無可傳入 null
     */
    public static void clearPlayerAllFlags(String targetPlayer, ClaimedResidence residence) {
        if (residence == null || targetPlayer == null || targetPlayer.isEmpty()) return;
        if (targetPlayer.equals(residence.getOwner())) return; // 不處理擁有者

        // 移除 trusted
        residence.getPermissions().setPlayerFlag(targetPlayer, "trusted", FlagPermissions.FlagState.NEITHER);
        // 移除 admin
        residence.getPermissions().setPlayerFlag(targetPlayer, "admin", FlagPermissions.FlagState.NEITHER);
        // 將目前所有該玩家的專屬旗標設為 NEITHER
        Map<String, Boolean> pf = residence.getPermissions().getPlayerFlags(targetPlayer);
        if (pf != null) {
            for (String flag : pf.keySet()) {
                residence.getPermissions().setPlayerFlag(targetPlayer, flag, FlagPermissions.FlagState.NEITHER);
            }
        }
    }

    /**
     * 由名稱取得 ClaimedResidence
     */
    public static ClaimedResidence parseClaimedResidence(String name) {
        if (name == null || name.isEmpty()) return null;
        ClaimedResidence currentResidence = Residence.getInstance().getResidenceManager().getByName(name);
        return currentResidence;
    }
    /**
     * 取得玩家目前所在領地名稱
     */
    public static String getCurrentResidenceName(Player player) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(player.getLocation());
        if (residence != null) return residence.getName();
        return null;
    }
    
    /**
     * 將 "x y z" 轉成對應世界的方塊座標 Location
     * 傳回 null 表示格式或數字解析失敗
     */
    public static Location stringToBlockLoc(World world, String string) {
        if (world == null || string == null) return null;
        String[] args = string.trim().split("\\s+");
        if (args.length != 3) {
            return null;
        }
        try {
            return new Location(
                world,
                Double.parseDouble(args[0]),
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2])
            );
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
