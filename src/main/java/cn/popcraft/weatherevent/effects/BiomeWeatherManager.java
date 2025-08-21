package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 生物群系天气管理器
 * 负责管理不同生物群系和天气组合的效果
 */
public class BiomeWeatherManager {
    
    private final WeatherEvent plugin;
    private final Map<String, Map<String, BaseWeatherEffect>> biomeWeatherEffects;
    private final Map<String, AdvancedBiomeWeatherEffect> advancedBiomeWeatherEffects;
    private boolean enabled;
    
    /**
     * 构造一个生物群系天气管理器
     * @param plugin 插件实例
     */
    public BiomeWeatherManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.biomeWeatherEffects = new HashMap<>();
        this.advancedBiomeWeatherEffects = new HashMap<>();
        this.enabled = false;
    }
    
    /**
     * 从配置中加载生物群系天气效果
     * @param config 配置部分
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) return;
        
        this.enabled = config.getBoolean("enabled", false);
        if (!enabled) return;
        
        // 清除现有效果
        biomeWeatherEffects.clear();
        advancedBiomeWeatherEffects.clear();
        
        ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
        if (biomesSection == null) return;
        
        // 遍历所有配置的生物群系
        for (String biomeName : biomesSection.getKeys(false)) {
            ConfigurationSection biomeSection = biomesSection.getConfigurationSection(biomeName);
            if (biomeSection == null) continue;
            
            // 创建高级生物群系天气效果
            AdvancedBiomeWeatherEffect advancedEffect = new AdvancedBiomeWeatherEffect(plugin, biomeName, biomeSection);
            if (advancedEffect.isEnabled()) {
                advancedBiomeWeatherEffects.put(biomeName.toUpperCase(), advancedEffect);
                plugin.getLogger().info("已加载生物群系 " + biomeName + " 的高级天气效果");
            }
            
            // 保留原有的简单效果加载逻辑
            Map<String, BaseWeatherEffect> weatherEffects = new HashMap<>();
            
            // 加载该生物群系下的不同天气效果
            ConfigurationSection weathersSection = biomeSection.getConfigurationSection("weather");
            if (weathersSection != null) {
                for (String weatherType : weathersSection.getKeys(false)) {
                    ConfigurationSection weatherSection = weathersSection.getConfigurationSection(weatherType);
                    if (weatherSection == null) continue;
                    
                    // 创建对应天气类型的效果
                    BaseWeatherEffect effect = createWeatherEffect(weatherType, weatherSection);
                    if (effect != null) {
                        weatherEffects.put(weatherType.toLowerCase(), effect);
                        plugin.getLogger().info("已加载生物群系 " + biomeName + " 的 " + weatherType + " 天气效果");
                    }
                }
            }
            
            if (!weatherEffects.isEmpty()) {
                biomeWeatherEffects.put(biomeName.toUpperCase(), weatherEffects);
            }
        }
        
        plugin.getLogger().info("已加载 " + biomeWeatherEffects.size() + " 个生物群系的天气效果");
        plugin.getLogger().info("已加载 " + advancedBiomeWeatherEffects.size() + " 个生物群系的高级天气效果");
    }
    
    /**
     * 根据天气类型创建对应的效果
     * @param weatherType 天气类型
     * @param config 效果配置
     * @return 创建的效果
     */
    private BaseWeatherEffect createWeatherEffect(String weatherType, ConfigurationSection config) {
        switch (weatherType.toLowerCase()) {
            case "clear":
                return new ClearEffect(plugin, config);
            case "rain":
                return new RainEffect(plugin, config);
            case "thunder":
                return new ThunderEffect(plugin, config);
            default:
                plugin.getLogger().warning("未知的天气类型: " + weatherType);
                return null;
        }
    }
    
    /**
     * 应用效果到玩家
     * @param player 目标玩家
     * @param world 目标世界
     */
    public void applyEffects(Player player, World world) {
        if (!enabled) return;
        
        // 获取玩家所在的生物群系
        Biome playerBiome = player.getLocation().getBlock().getBiome();
        String biomeName = playerBiome.name();
        
        // 获取当前世界的天气状态
        String weatherType = getWeatherType(world);
        
        // 应用高级生物群系天气效果
        AdvancedBiomeWeatherEffect advancedEffect = advancedBiomeWeatherEffects.get(biomeName);
        if (advancedEffect != null) {
            advancedEffect.apply(player, world);
        }
        
        // 应用传统的生物群系天气效果
        Map<String, BaseWeatherEffect> weatherEffects = biomeWeatherEffects.get(biomeName);
        if (weatherEffects != null) {
            BaseWeatherEffect effect = weatherEffects.get(weatherType);
            if (effect != null && effect.isEnabled()) {
                // 应用对应的效果
                effect.apply(player, world);
            }
        }
    }
    
    /**
     * 移除玩家的效果
     * @param player 目标玩家
     * @param world 目标世界
     */
    public void removeEffects(Player player, World world) {
        if (!enabled) return;
        
        // 移除所有可能应用的效果
        for (Map<String, BaseWeatherEffect> weatherEffects : biomeWeatherEffects.values()) {
            for (BaseWeatherEffect effect : weatherEffects.values()) {
                effect.remove(player, world);
            }
        }
    }
    
    /**
     * 获取当前世界的天气类型
     * @param world 目标世界
     * @return 天气类型（"clear", "rain", "thunder"）
     */
    private String getWeatherType(World world) {
        if (world.isThundering()) {
            return "thunder";
        } else if (world.hasStorm()) {
            return "rain";
        } else {
            return "clear";
        }
    }
    
    /**
     * 检查是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
}