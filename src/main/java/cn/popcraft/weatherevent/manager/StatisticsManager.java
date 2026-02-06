package cn.popcraft.weatherevent.manager;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 统计管理器
 * 用于记录效果触发统计和玩家受到效果的频率统计
 */
public class StatisticsManager {
    private final WeatherEvent plugin;
    private final Logger logger;
    private final File statisticsFile;
    private YamlConfiguration statisticsConfig;
    
    // 效果触发统计
    private final Map<String, EffectStatistics> effectStatistics;
    
    // 玩家效果统计
    private final Map<UUID, PlayerStatistics> playerStatistics;
    
    public StatisticsManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.statisticsFile = new File(plugin.getDataFolder(), "statistics.yml");
        this.effectStatistics = new ConcurrentHashMap<>();
        this.playerStatistics = new ConcurrentHashMap<>();
        loadStatistics();
    }
    
    /**
     * 加载统计信息
     */
    private void loadStatistics() {
        if (!statisticsFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                statisticsFile.createNewFile();
            } catch (IOException e) {
                logger.warning("无法创建统计文件: " + e.getMessage());
            }
        }
        
        statisticsConfig = YamlConfiguration.loadConfiguration(statisticsFile);
        
        // 加载效果统计
        loadEffectStatistics();
        
        // 加载玩家统计
        loadPlayerStatistics();
    }
    
    /**
     * 加载效果统计
     */
    private void loadEffectStatistics() {
        // 从配置文件加载效果统计信息
        // 这里简化处理，实际应用中可以根据需要扩展
    }
    
    /**
     * 加载玩家统计
     */
    private void loadPlayerStatistics() {
        // 从配置文件加载玩家统计信息
        // 这里简化处理，实际应用中可以根据需要扩展
    }
    
    /**
     * 保存统计信息
     */
    public void saveStatistics() {
        try {
            statisticsConfig.save(statisticsFile);
        } catch (IOException e) {
            logger.warning("无法保存统计信息: " + e.getMessage());
        }
    }
    
    /**
     * 记录效果触发
     * @param effectId 效果ID
     * @param player 玩家（可选）
     */
    public void recordEffectTrigger(String effectId, Player player) {
        // 更新效果统计
        EffectStatistics effectStats = effectStatistics.computeIfAbsent(
            effectId, 
            id -> new EffectStatistics(id)
        );
        effectStats.incrementTriggerCount();
        
        // 更新玩家统计
        if (player != null) {
            UUID playerId = player.getUniqueId();
            PlayerStatistics playerStats = playerStatistics.computeIfAbsent(
                playerId, 
                id -> new PlayerStatistics(playerId, player.getName())
            );
            playerStats.incrementEffectCount(effectId);
        }
    }
    
    /**
     * 记录效果成功应用
     * @param effectId 效果ID
     * @param player 玩家（可选）
     */
    public void recordEffectSuccess(String effectId, Player player) {
        // 更新效果统计
        EffectStatistics effectStats = effectStatistics.get(effectId);
        if (effectStats != null) {
            effectStats.incrementSuccessCount();
        }
        
        // 更新玩家统计
        if (player != null) {
            UUID playerId = player.getUniqueId();
            PlayerStatistics playerStats = playerStatistics.get(playerId);
            if (playerStats != null) {
                playerStats.incrementEffectSuccess(effectId);
            }
        }
    }
    
    /**
     * 获取效果统计信息
     * @param effectId 效果ID
     * @return 效果统计信息
     */
    public EffectStatistics getEffectStatistics(String effectId) {
        return effectStatistics.get(effectId);
    }
    
    /**
     * 获取玩家统计信息
     * @param playerId 玩家ID
     * @return 玩家统计信息
     */
    public PlayerStatistics getPlayerStatistics(UUID playerId) {
        return playerStatistics.get(playerId);
    }
    
    /**
     * 获取所有效果统计信息
     * @return 效果统计映射
     */
    public Map<String, EffectStatistics> getAllEffectStatistics() {
        return new HashMap<>(effectStatistics);
    }
    
    /**
     * 获取所有玩家统计信息
     * @return 玩家统计映射
     */
    public Map<UUID, PlayerStatistics> getAllPlayerStatistics() {
        return new HashMap<>(playerStatistics);
    }
    
    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        effectStatistics.clear();
        playerStatistics.clear();
    }
    
    /**
     * 效果统计信息类
     */
    public static class EffectStatistics {
        private final String effectId;
        private long triggerCount;  // 触发次数
        private long successCount;  // 成功应用次数
        
        public EffectStatistics(String effectId) {
            this.effectId = effectId;
            this.triggerCount = 0;
            this.successCount = 0;
        }
        
        public void incrementTriggerCount() {
            triggerCount++;
        }
        
        public void incrementSuccessCount() {
            successCount++;
        }
        
        public String getEffectId() {
            return effectId;
        }
        
        public long getTriggerCount() {
            return triggerCount;
        }
        
        public long getSuccessCount() {
            return successCount;
        }
        
        public double getSuccessRate() {
            if (triggerCount == 0) {
                return 0.0;
            }
            return (double) successCount / triggerCount;
        }
    }
    
    /**
     * 玩家统计信息类
     */
    public static class PlayerStatistics {
        private final UUID playerId;
        private final String playerName;
        private final Map<String, Long> effectCounts;      // 每种效果的触发次数
        private final Map<String, Long> effectSuccesses;   // 每种效果的成功次数
        
        public PlayerStatistics(UUID playerId, String playerName) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.effectCounts = new HashMap<>();
            this.effectSuccesses = new HashMap<>();
        }
        
        public void incrementEffectCount(String effectId) {
            effectCounts.put(effectId, effectCounts.getOrDefault(effectId, 0L) + 1);
        }
        
        public void incrementEffectSuccess(String effectId) {
            effectSuccesses.put(effectId, effectSuccesses.getOrDefault(effectId, 0L) + 1);
        }
        
        public UUID getPlayerId() {
            return playerId;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public Map<String, Long> getEffectCounts() {
            return new HashMap<>(effectCounts);
        }
        
        public Map<String, Long> getEffectSuccesses() {
            return new HashMap<>(effectSuccesses);
        }
        
        public long getTotalEffectCount() {
            return effectCounts.values().stream().mapToLong(Long::longValue).sum();
        }
        
        public long getTotalEffectSuccess() {
            return effectSuccesses.values().stream().mapToLong(Long::longValue).sum();
        }
    }
}