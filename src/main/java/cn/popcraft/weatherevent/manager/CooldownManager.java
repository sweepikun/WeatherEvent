package cn.popcraft.weatherevent.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 冷却管理器
 * 用于管理效果的冷却时间，防止过于频繁触发
 */
public class CooldownManager {
    private final Map<String, Map<UUID, Long>> cooldowns;
    
    public CooldownManager() {
        this.cooldowns = new HashMap<>();
    }
    
    /**
     * 设置玩家特定效果的冷却时间
     * @param player 玩家
     * @param effectId 效果ID
     * @param cooldownMillis 冷却时间（毫秒）
     */
    public void setCooldown(Player player, String effectId, long cooldownMillis) {
        UUID playerId = player.getUniqueId();
        long expireTime = System.currentTimeMillis() + cooldownMillis;
        
        cooldowns.computeIfAbsent(effectId, k -> new HashMap<>()).put(playerId, expireTime);
    }
    
    /**
     * 检查玩家特定效果是否在冷却中
     * @param player 玩家
     * @param effectId 效果ID
     * @return 是否在冷却中
     */
    public boolean isOnCooldown(Player player, String effectId) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Long> effectCooldowns = cooldowns.get(effectId);
        
        if (effectCooldowns == null) {
            return false;
        }
        
        Long expireTime = effectCooldowns.get(playerId);
        if (expireTime == null) {
            return false;
        }
        
        // 检查是否已过期
        if (System.currentTimeMillis() > expireTime) {
            // 冷却已过期，移除记录
            effectCooldowns.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取玩家特定效果的剩余冷却时间
     * @param player 玩家
     * @param effectId 效果ID
     * @return 剩余冷却时间（毫秒），如果没有冷却则返回0
     */
    public long getRemainingCooldown(Player player, String effectId) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Long> effectCooldowns = cooldowns.get(effectId);
        
        if (effectCooldowns == null) {
            return 0;
        }
        
        Long expireTime = effectCooldowns.get(playerId);
        if (expireTime == null) {
            return 0;
        }
        
        long remainingTime = expireTime - System.currentTimeMillis();
        if (remainingTime <= 0) {
            // 冷却已过期，移除记录
            effectCooldowns.remove(playerId);
            return 0;
        }
        
        return remainingTime;
    }
    
    /**
     * 清除玩家的所有冷却时间
     * @param player 玩家
     */
    public void clearCooldowns(Player player) {
        UUID playerId = player.getUniqueId();
        for (Map<UUID, Long> effectCooldowns : cooldowns.values()) {
            effectCooldowns.remove(playerId);
        }
    }
    
    /**
     * 清除特定效果的所有冷却时间
     * @param effectId 效果ID
     */
    public void clearEffectCooldowns(String effectId) {
        cooldowns.remove(effectId);
    }
    
    /**
     * 清除所有冷却时间
     */
    public void clearAllCooldowns() {
        cooldowns.clear();
    }
}