package cn.popcraft.weatherevent.manager;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.effects.BaseWeatherEffect;
import cn.popcraft.weatherevent.effects.BiomeWeatherEffect;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * 生物群系天气管理器
 * 负责加载和管理生物群系天气效果
 */
public class BiomeWeatherManager {
    private final WeatherEvent plugin;
    private final Map<String, BiomeWeatherEffect> biomeWeatherEffects;
    private boolean enabled;
    
    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public BiomeWeatherManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.biomeWeatherEffects = new HashMap<>();
        this.enabled = false;
    }
    
    /**
     * 加载生物群系天气效果配置
     */
    public void loadConfig() throws IllegalArgumentException {
        // 清除现有效果
        biomeWeatherEffects.clear();
        
        // 获取配置文件
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (config == null) {
            plugin.getLogger().warning("config.yml 不是一个有效的配置文件");
            return;
        }
        
        // 检查是否启用
        enabled = config.getBoolean("enabled", false);
        if (!enabled) {
            plugin.getLogger().info("生物群系天气效果系统已禁用");
            return;
        }
        
        // 加载生物群系天气效果
        ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
        if (biomesSection == null) {
            plugin.getLogger().warning("未找到生物群系配置");
            return;
        }
        
        for (String biomeName : biomesSection.getKeys(false)) {
            try {
                ConfigurationSection biomeSection = biomesSection.getConfigurationSection(biomeName);
                if (biomeSection == null) {
                    plugin.getLogger().warning("生物群系 " + biomeName + " 的配置格式错误");
                    continue;
                }
                
                BiomeWeatherEffect effect = new BiomeWeatherEffect(plugin, biomeSection);
                
                biomeWeatherEffects.put(biomeName, effect);
                plugin.getLogger().info("已加载生物群系 " + biomeName + " 的天气效果");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "加载生物群系 " + biomeName + " 的天气效果时出错", e);
            }
        }
    }



    private void loadBiomeEffects() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
        if (biomesSection == null) return;

        for (String biomeName : biomesSection.getKeys(false)) {
            try {
                ConfigurationSection biomeSection = biomesSection.getConfigurationSection(biomeName);
                if (biomeSection == null) {
                    plugin.getLogger().warning("生物群系 " + biomeName + " 的配置格式错误");
                    continue;
                }
                
                BiomeWeatherEffect effect = new BiomeWeatherEffect(plugin, biomeSection);
                
                biomeWeatherEffects.put(biomeName, effect);
                plugin.getLogger().info("已加载生物群系 " + biomeName + " 的天气效果");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "加载生物群系 " + biomeName + " 的天气效果时出错", e);
            }
        }
        
        plugin.getLogger().info("已加载 " + biomeWeatherEffects.size() + " 个生物群系天气效果");
    }
    
    /**
     * 应用生物群系天气效果
     * @param player 玩家
     * @param world 世界
     */
    public void applyEffects(Player player, World world) {
        if (!enabled) return;
        
        // 获取玩家所在的生物群系
        String biomeName = player.getLocation().getBlock().getBiome().name();
        
        // 查找对应的生物群系天气效果
        BiomeWeatherEffect effect = biomeWeatherEffects.get(biomeName);
        if (effect != null && effect.isEnabled()) {
            // 应用效果
            effect.apply(player, world);
        }
    }
    
    /**
     * 更新所有玩家的生物群系天气效果
     */
    public void updateEffects() {
        if (!enabled) return;
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            World world = player.getWorld();
            applyEffects(player, world);
        }
    }
    
    /**
     * 检查生物群系天气效果系统是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取生物群系天气效果
     * @param biomeName 生物群系名称
     * @return 生物群系天气效果
     */
    public BiomeWeatherEffect getBiomeWeatherEffect(String biomeName) {
        return biomeWeatherEffects.get(biomeName);
    }
    
    /**
     * 获取所有生物群系天气效果
     * @return 生物群系天气效果映射
     */
    public Map<String, BiomeWeatherEffect> getBiomeWeatherEffects() {
        return biomeWeatherEffects;
    }
}