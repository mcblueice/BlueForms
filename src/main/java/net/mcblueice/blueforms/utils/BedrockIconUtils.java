package net.mcblueice.blueforms.utils;

import org.bukkit.Material;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.william278.huskhomes.position.Home;

/**
 * BedrockIconUtils 工具類
 * 用於將物品/方塊名稱解析為 Bedrock 對應圖示路徑。
 * resolve("minecraft:stone") / resolve("minecraft:stone_sword") 取得對應 PATH 圖片描述。
 * 流程：去 namespace -> 查映射 -> 無映射判斷是否 block -> blocks 或 items 路徑。
 */
public final class BedrockIconUtils {
	/**
	 * key = javaId (無 namespace, 小寫)
	 * value = 最終返回字串 (含 PATH; 開頭)
	 */
	private static final Map<String,String> MAP = new ConcurrentHashMap<>();

	/** 工具類禁止實例化 */
	private BedrockIconUtils() {}

	/**
	 * 註冊/覆寫映射。
	 * @param javaId 例如 "stone" (不含 namespace) 可大小寫
	 * @param finalResult 例如 "PATH;textures/items/my_stone.png" 或 "URL;https://..."
	 */
	public static void register(String javaId, String finalResult) {
		if (javaId == null || finalResult == null || javaId.isBlank() || finalResult.isBlank()) return;
		MAP.put(javaId.toLowerCase(Locale.ROOT), finalResult);
	}

	/**
	 * 清空所有映射
	 */
	public static void clear() { MAP.clear(); }

	/**
	 * 從配置載入映射
	 * @param sectionMap 配置節點
	 */
	public static void load(Map<String, Object> sectionMap) {
		if (sectionMap == null) return;
		for (Map.Entry<String,Object> e : sectionMap.entrySet()) {
			String k = e.getKey();
			Object v = e.getValue();
			if (k == null || v == null) continue;
			String val = String.valueOf(v).trim();
			if (val.isEmpty()) continue;
			// 僅接受已含前綴 PATH; 或 URL;，否則自動判斷 blocks/items? 這裡先保留原樣直接登記
			register(k, val);
		}
	}

	/**
	 * 解析輸入字串。
	 * @param raw 例如 "minecraft:stone" / "stone_sword"
	 * @return PATH;/URL; 開頭的描述；若無法解析回傳 null。
	 */
	/**
	 * 解析輸入字串，取得對應圖示路徑
	 * @param raw 例如 "minecraft:stone" / "stone_sword"
	 * @return PATH;/URL; 開頭的描述；若無法解析回傳 null。
	 */
	public static String resolve(String raw) {
		if (raw == null || raw.isBlank()) return null;
		String id = raw.trim();
		int colon = id.indexOf(':');
	if (colon >= 0) id = id.substring(colon + 1); // 去 namespace
		String lower = id.toLowerCase(Locale.ROOT);

	// 1) 映射表直接命中 → 直接返回（允許 PATH; 或 URL; 自由配置）
		String mapped = MAP.get(lower);
		if (mapped != null) return mapped;

	// 2) 嘗試轉為 Material
		Material mat = Material.matchMaterial(lower.toUpperCase(Locale.ROOT));
	if (mat == null) return null; // 非合法 Material

	// 3) 判斷是否方塊
		if (lower.endsWith("_spawn_egg")) {
			String mob = lower.substring(0, lower.length() - "_spawn_egg".length());
			return "PATH;textures/items/spawn_eggs/spawn_egg_" + mob + ".png";
		}
		if (mat.isBlock()) return "PATH;textures/blocks/" + lower + ".png";
		return "PATH;textures/items/" + lower + ".png";
	}

	/**
	 * 根據 HuskHomes Home Meta 的 huskhomesgui:icon 標籤解析圖示。
	 * 回傳格式同 resolve："PATH;..." 或 "URL;..."；若無合適圖示則回傳 "NONE"。
	 * @param home Home 物件
	 * @return 圖示描述字串或 "NONE"
	 */
	public static String resolveHomeIcon(Home home) {
		if (home == null) return "NONE";
		try {
			Map<String, String> tags = home.getMeta() != null ? home.getMeta().getTags() : null;
			if (tags == null || tags.isEmpty()) return "NONE";
			String raw = tags.get("huskhomesgui:icon");
			if (raw == null || raw.isBlank()) return "NONE";
			String res = resolve(raw.trim());
			return res != null ? res : "NONE";
		} catch (Exception ignored) {
			return "NONE";
		}
	}
}