package cn.popcraft.weatherevent.manager;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理器
 * 用于管理不同玩家组的效果配置和权限
 */
public class PermissionManager {
    private final WeatherEvent plugin;
    private final Map<String, GroupEffectConfig> groupConfigs;
    
    public PermissionManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.groupConfigs = new HashMap<>();
        loadGroupConfigs();
    }
    
    /**
     * 加载玩家组配置
     */
    private void loadGroupConfigs() {
        ConfigurationSection groupsSection = plugin.getConfig().getConfigurationSection("permissions.groups");
        if (groupsSection == null) return;
        
        for (String groupName : groupsSection.getKeys(false)) {
            ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
            if (groupSection != null) {
                GroupEffectConfig config = new GroupEffectConfig(groupSection);
                groupConfigs.put(groupName, config);
            }
        }
    }
    
    /**
     * 检查玩家是否属于指定组
     * @param player 玩家
     * @param groupName 组名
     * @return 是否属于该组
     */
    public boolean isPlayerInGroup(Player player, String groupName) {
        // 检查权限节点
        if (player.hasPermission("weatherevent.group." + groupName)) {
            return true;
        }
        
        // 检查Vault权限组（如果可用）
        // 注意：这需要Vault作为依赖项，这里仅作为示例
        /*
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager()
                .getRegistration(Permission.class);
            if (rsp != null) {
                Permission perms = rsp.getProvider();
                return perms.getPlayerGroups(player).contains(groupName);
            }
        }
        */
        
        return false;
    }
    
    /**
     * 获取玩家的组配置
     * @param player 玩家
     * @return 组配置，如果没有找到则返回默认配置
     */
    public GroupEffectConfig getPlayerGroupConfig(Player player) {
        for (Map.Entry<String, GroupEffectConfig> entry : groupConfigs.entrySet()) {
            if (isPlayerInGroup(player, entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 返回默认配置
        return groupConfigs.getOrDefault("default", new GroupEffectConfig());
    }
    
    /**
     * 获取所有组名
     * @return 组名集合
     */
    public Set<String> getGroupNames() {
        return groupConfigs.keySet();
    }
    
    /**
     * 玩家组效果配置类
     */
    public static class GroupEffectConfig {
        private boolean bypassEffects; // 是否免疫所有效果
        private double effectMultiplier; // 效果强度乘数
        private Map<String, Boolean> effectPermissions; // 特定效果的启用/禁用状态
        
        public GroupEffectConfig() {
            this.bypassEffects = false;
            this.effectMultiplier = 1.0;
            this.effectPermissions = new HashMap<>();
        }
        
        public GroupEffectConfig(ConfigurationSection config) {
            this.bypassEffects = config.getBoolean("bypass-effects", false);
            this.effectMultiplier = config.getDouble("effect-multiplier", 1.0);
            
            this.effectPermissions = new HashMap<>();
            ConfigurationSection effectsSection = config.getConfigurationSection("effects");
            if (effectsSection != null) {
                for (String effectName : effectsSection.getKeys(false)) {
                    effectPermissions.put(effectName, effectsSection.getBoolean(effectName, true));
                }
            }
        }
        
        /**
         * 检查是否应该跳过指定效果
         * @param effectName 效果名称
         * @return 是否跳过
         */
        public boolean shouldSkipEffect(String effectName) {
            // 如果玩家免疫所有效果，返回true
            if (bypassEffects) {
                return true;
            }
            
            // 检查特定效果权限
            return effectPermissions.containsKey(effectName) && !effectPermissions.get(effectName);
        }
        
        /**
         * 获取效果强度乘数
         * @return 强度乘数
         */
        public double getEffectMultiplier() {
            return effectMultiplier;
        }
        
        /**
         * 检查是否免疫所有效果
         * @return 是否免疫所有效果
         */
        public boolean isBypassEffects() {
            return bypassEffects;
        }
    }
}