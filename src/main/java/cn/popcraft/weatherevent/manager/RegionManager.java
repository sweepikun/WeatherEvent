package cn.popcraft.weatherevent.manager;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 区域管理器
 * 用于管理基于世界或区域的效果配置
 */
public class RegionManager {
    private final WeatherEvent plugin;
    private final Map<String, WorldConfig> worldConfigs;
    private final Map<String, RegionConfig> regionConfigs;
    
    public RegionManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.worldConfigs = new HashMap<>();
        this.regionConfigs = new HashMap<>();
        loadConfigs();
    }
    
    /**
     * 加载世界和区域配置
     */
    private void loadConfigs() {
        // 加载世界配置
        ConfigurationSection worldsSection = plugin.getConfig().getConfigurationSection("worlds");
        if (worldsSection != null) {
            for (String worldName : worldsSection.getKeys(false)) {
                ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
                if (worldSection != null) {
                    WorldConfig config = new WorldConfig(worldSection);
                    worldConfigs.put(worldName, config);
                }
            }
        }
        
        // 加载区域配置
        ConfigurationSection regionsSection = plugin.getConfig().getConfigurationSection("regions");
        if (regionsSection != null) {
            for (String regionName : regionsSection.getKeys(false)) {
                ConfigurationSection regionSection = regionsSection.getConfigurationSection(regionName);
                if (regionSection != null) {
                    RegionConfig config = new RegionConfig(regionSection);
                    regionConfigs.put(regionName, config);
                }
            }
        }
    }
    
    /**
     * 检查世界是否启用天气效果
     * @param world 世界
     * @return 是否启用
     */
    public boolean isWorldEnabled(World world) {
        WorldConfig config = worldConfigs.get(world.getName());
        return config == null || config.isEnabled(); // 默认启用
    }
    
    /**
     * 获取世界的效果乘数
     * @param world 世界
     * @return 效果乘数
     */
    public double getWorldEffectMultiplier(World world) {
        WorldConfig config = worldConfigs.get(world.getName());
        return config != null ? config.getEffectMultiplier() : 1.0;
    }
    
    /**
     * 检查效果在世界中是否启用
     * @param world 世界
     * @param effectName 效果名称
     * @return 是否启用
     */
    public boolean isEffectEnabledInWorld(World world, String effectName) {
        WorldConfig config = worldConfigs.get(world.getName());
        return config == null || config.isEffectEnabled(effectName);
    }
    
    /**
     * 获取玩家所在区域的配置
     * @param player 玩家
     * @return 区域配置，如果没有找到则返回null
     */
    public RegionConfig getPlayerRegionConfig(Player player) {
        Location location = player.getLocation();
        
        // 检查所有区域配置
        for (RegionConfig config : regionConfigs.values()) {
            if (config.isInRegion(location)) {
                return config;
            }
        }
        
        return null;
    }
    
    /**
     * 检查效果在区域中是否启用
     * @param player 玩家
     * @param effectName 效果名称
     * @return 是否启用
     */
    public boolean isEffectEnabledInRegion(Player player, String effectName) {
        RegionConfig config = getPlayerRegionConfig(player);
        return config == null || config.isEffectEnabled(effectName);
    }
    
    /**
     * 获取区域的效果乘数
     * @param player 玩家
     * @return 效果乘数
     */
    public double getRegionEffectMultiplier(Player player) {
        RegionConfig config = getPlayerRegionConfig(player);
        return config != null ? config.getEffectMultiplier() : 1.0;
    }
    
    /**
     * 世界配置类
     */
    public static class WorldConfig {
        private boolean enabled;
        private double effectMultiplier;
        private Map<String, Boolean> effectPermissions;
        
        public WorldConfig() {
            this.enabled = true;
            this.effectMultiplier = 1.0;
            this.effectPermissions = new HashMap<>();
        }
        
        public WorldConfig(ConfigurationSection config) {
            this.enabled = config.getBoolean("enabled", true);
            this.effectMultiplier = config.getDouble("effect-multiplier", 1.0);
            
            this.effectPermissions = new HashMap<>();
            ConfigurationSection effectsSection = config.getConfigurationSection("effects");
            if (effectsSection != null) {
                for (String effectName : effectsSection.getKeys(false)) {
                    effectPermissions.put(effectName, effectsSection.getBoolean(effectName, true));
                }
            }
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public double getEffectMultiplier() {
            return effectMultiplier;
        }
        
        public boolean isEffectEnabled(String effectName) {
            return effectPermissions.getOrDefault(effectName, true);
        }
    }
    
    /**
     * 区域配置类
     */
    public static class RegionConfig {
        private String worldName;
        private int minX, minY, minZ;
        private int maxX, maxY, maxZ;
        private double effectMultiplier;
        private Map<String, Boolean> effectPermissions;
        
        public RegionConfig() {
            this.worldName = "";
            this.minX = this.minY = this.minZ = 0;
            this.maxX = this.maxY = this.maxZ = 0;
            this.effectMultiplier = 1.0;
            this.effectPermissions = new HashMap<>();
        }
        
        public RegionConfig(ConfigurationSection config) {
            this.worldName = config.getString("world", "");
            this.effectMultiplier = config.getDouble("effect-multiplier", 1.0);
            
            // 加载区域坐标
            ConfigurationSection areaSection = config.getConfigurationSection("area");
            if (areaSection != null) {
                this.minX = areaSection.getInt("min-x", 0);
                this.minY = areaSection.getInt("min-y", 0);
                this.minZ = areaSection.getInt("min-z", 0);
                this.maxX = areaSection.getInt("max-x", 0);
                this.maxY = areaSection.getInt("max-y", 0);
                this.maxZ = areaSection.getInt("max-z", 0);
            }
            
            // 确保坐标顺序正确
            if (minX > maxX) {
                int temp = minX;
                minX = maxX;
                maxX = temp;
            }
            
            if (minY > maxY) {
                int temp = minY;
                minY = maxY;
                maxY = temp;
            }
            
            if (minZ > maxZ) {
                int temp = minZ;
                minZ = maxZ;
                maxZ = temp;
            }
            
            this.effectPermissions = new HashMap<>();
            ConfigurationSection effectsSection = config.getConfigurationSection("effects");
            if (effectsSection != null) {
                for (String effectName : effectsSection.getKeys(false)) {
                    effectPermissions.put(effectName, effectsSection.getBoolean(effectName, true));
                }
            }
        }
        
        /**
         * 检查位置是否在区域内
         * @param location 位置
         * @return 是否在区域内
         */
        public boolean isInRegion(Location location) {
            // 检查世界
            if (!worldName.isEmpty() && !location.getWorld().getName().equals(worldName)) {
                return false;
            }
            
            // 检查坐标范围
            return location.getBlockX() >= minX && location.getBlockX() <= maxX &&
                   location.getBlockY() >= minY && location.getBlockY() <= maxY &&
                   location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
        }
        
        public double getEffectMultiplier() {
            return effectMultiplier;
        }
        
        public boolean isEffectEnabled(String effectName) {
            return effectPermissions.getOrDefault(effectName, true);
        }
    }
}