
# BlueForms

一個用於 Spigot Floodgate Form 的基岩板選單插件

## 功能特色

- Residence 選單:
	- 領地傳送
	- 領地查詢
	- 領地創建
	- 公共權限設置
	- 玩家權限設置
	- 信任玩家/管理員設定
	- 多項原版 Residence 沒有的可視化功能
- HuskHomes TP 選單:
	- 接受傳送
	- 拒絕傳送
	- 下拉選單請求傳送至當前線上玩家/全部分流玩家 (需安裝 BlueCrossServer)
- HuskHomes Home 選單:
	- 傳送/管理自身 Home 點
	- 傳送/查詢公共 Home 點
	- 支援 HuskHomesGUI 圖標顯示
	- 新增 Home 點分類功能
- Message 選單:
	- 下拉選單傳送訊息至當前線上玩家/全部分流玩家 (需安裝 BlueCrossServer)
	- 可自訂傳送訊息指令


## 指令列表

| 指令 | 用途 | 權限節點 |
|------|------|------|
| `/blueforms reload` | 重新載入設定檔 | `blueforms.reload` |

| `/blueforms <選單ID> [選填ID]` | 為自己或他人開啟指定的選單 | `blueforms.use.<選單ID>` |

### 選單ID與權限

| 選單ID | 權限節點 |
|----------|-------------------------------|
| residence | `blueforms.use.residence` |
| home      | `blueforms.use.home`      |
| tp        | `blueforms.use.tp`        |
| message   | `blueforms.use.message`   |

## 授權 License

本專案採用 MIT License  
