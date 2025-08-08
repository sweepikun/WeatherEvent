package cn.popcraft.weatherevent.config;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
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
        
        return true;
    }
}