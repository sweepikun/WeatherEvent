package cn.popcraft.weatherevent.config;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.condition.ConditionChecker;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 共享效果管理器
 * 用于管理和应用可重用的共享效果
 */
public class SharedEffectManager {
    private final WeatherEvent plugin;
    private final Map<String, ConfigurationSection> sharedEffects;
    
    public SharedEffectManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.sharedEffects = new HashMap<>();
        loadSharedEffects();
    }
    
    /**
     * 从配置中加载共享效果
     */
    private void loadSharedEffects() {
        ConfigurationSection sharedSection = plugin.getConfig().getConfigurationSection("shared-effects");
        if (sharedSection == null) return;
        
        for (String key : sharedSection.getKeys(false)) {
            if (sharedSection.isConfigurationSection(key)) {
                sharedEffects.put(key, sharedSection.getConfigurationSection(key));
            }
        }
    }
    
    /**
     * 获取共享效果配置
     * @param effectId 效果ID
     * @return 效果配置，如果不存在则返回null
     */
    public ConfigurationSection getSharedEffect(String effectId) {
        return sharedEffects.get(effectId);
    }
    
    /**
     * 应用共享效果
     * @param player 玩家
     * @param effectId 效果ID
     * @return 是否成功应用
     */
    public boolean applySharedEffect(Player player, String effectId) {
        ConfigurationSection effectConfig = getSharedEffect(effectId);
        if (effectConfig == null) return false;
        
        // 检查前置条件
        if (effectConfig.isConfigurationSection("conditions")) {
            ConfigurationSection conditionsSection = effectConfig.getConfigurationSection("conditions");
            Map<String, Object> conditions = new HashMap<>();
            for (String key : conditionsSection.getKeys(true)) {
                conditions.put(key, conditionsSection.get(key));
            }
            
            if (!ConditionChecker.checkPrerequisites(player, conditions)) {
                return false; // 条件不满足，不应用效果
            }
        }
        
        // 处理命令
        ConfigurationSection commandsSection = effectConfig.getConfigurationSection("commands");
        if (commandsSection != null) {
            for (String command : commandsSection.getStringList("list")) {
                String processedCmd = command.replace("%player%", player.getName())
                        .replace("%player_name%", player.getName())
                        .replace("%player_x%", String.valueOf(player.getLocation().getX()))
                        .replace("%player_y%", String.valueOf(player.getLocation().getY()))
                        .replace("%player_z%", String.valueOf(player.getLocation().getZ()));
                
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCmd);
            }
        }
        
        // 处理伤害
        ConfigurationSection damageSection = effectConfig.getConfigurationSection("damage");
        if (damageSection != null && damageSection.getBoolean("enabled", false)) {
            double chance = damageSection.getDouble("chance", 0.0);
            if (Math.random() <= chance) {
                double amount = damageSection.getDouble("amount", 1.0);
                player.damage(amount);
            }
        }
        
        // 处理连锁效果
        ConfigurationSection chainSection = effectConfig.getConfigurationSection("chain-effects");
        if (chainSection != null) {
            // 支持两种配置格式
            if (chainSection.getList("") != null) {
                List<Map<String, Object>> listData = (List<Map<String, Object>>) chainSection.getList("");
                for (ChainEffect chainEffect : ChainEffect.fromConfig(listData)) {
                    triggerChainEffect(player, chainEffect);
                }
            } else {
                Map<String, Object> mapData = new HashMap<>();
                for (String key : chainSection.getKeys(false)) {
                    mapData.put(key, chainSection.get(key));
                }
                // 包装mapData以符合fromConfigMap的参数要求
                Map<String, Map<String, Object>> wrappedMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : mapData.entrySet()) {
                    if (entry.getValue() instanceof ConfigurationSection) {
                        Map<String, Object> innerMap = new HashMap<>();
                        ConfigurationSection innerSection = (ConfigurationSection) entry.getValue();
                        for (String innerKey : innerSection.getKeys(true)) {
                            innerMap.put(innerKey, innerSection.get(innerKey));
                        }
                        wrappedMap.put(entry.getKey(), innerMap);
                    }
                }
                for (ChainEffect chainEffect : ChainEffect.fromConfigMap(wrappedMap)) {
                    triggerChainEffect(player, chainEffect);
                }
            }
        }
        
        return true;
    }
    
    /**
     * 触发连锁效果
     * @param player 玩家
     * @param chainEffect 连锁效果配置
     */
    private void triggerChainEffect(Player player, ChainEffect chainEffect) {
        // 检查触发几率
        if (Math.random() > chainEffect.getChance()) {
            return;
        }
        
        // 检查条件
        if (chainEffect.getConditions() != null && 
            !ConditionChecker.checkPrerequisites(player, chainEffect.getConditions())) {
            return;
        }
        
        // 检查延迟
        int delay = chainEffect.getDelay();
        if (delay > 0) {
            // 延迟触发
            new BukkitRunnable() {
                @Override
                public void run() {
                    applySharedEffect(player, chainEffect.getEffectId());
                }
            }.runTaskLater(plugin, delay);
        } else {
            // 立即触发
            applySharedEffect(player, chainEffect.getEffectId());
        }
    }
}