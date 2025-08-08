package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 生物群系天气效果类
 * 根据不同的生物群系和天气组合应用不同的效果
 */
public class BiomeWeatherEffect extends BaseWeatherEffect {

    // 存储不同生物群系和天气组合的效果配置
    private final Map<String, Map<String, BaseWeatherEffect>> biomeWeatherEffects;
    
    /**
     * 构造一个生物群系天气效果
     * @param plugin 插件实例
     * @param config 效果配置
     */
    public BiomeWeatherEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "biome_weather", config);
        this.biomeWeatherEffects = new HashMap<>();
        loadBiomeWeatherEffects();
    }
    
    /**
     * 从配置中加载生物群系天气效果
     */
    private void loadBiomeWeatherEffects() {
        if (config == null) return;
        
        ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
        if (biomesSection == null) return;
        
        // 遍历所有配置的生物群系
        for (String biomeName : biomesSection.getKeys(false)) {
            ConfigurationSection biomeSection = biomesSection.getConfigurationSection(biomeName);
            if (biomeSection == null) continue;
            
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
                        weatherEffects.put(weatherType, effect);
                        plugin.getLogger().info("已加载生物群系 " + biomeName + " 的 " + weatherType + " 天气效果");
                    }
                }
            }
            
            biomeWeatherEffects.put(biomeName.toUpperCase(), weatherEffects);
        }
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
    
    @Override
    public void apply(Player player, World world) {
        if (!isEnabled() || !isApplicable(world)) return;
        
        // 获取玩家所在的生物群系
        Biome playerBiome = player.getLocation().getBlock().getBiome();
        String biomeName = playerBiome.name();
        
        // 检查生物群系条件
        if (condition.getBiomes() != null && !condition.getBiomes().isEmpty()) {
            if (!condition.getBiomes().contains(biomeName)) {
                return;
            }
        }
        
        // 获取当前世界的天气状态
        String weatherType = getWeatherType(world);
        
        // 查找对应的生物群系天气效果
        Map<String, BaseWeatherEffect> weatherEffects = biomeWeatherEffects.get(biomeName);
        if (weatherEffects != null) {
            BaseWeatherEffect effect = weatherEffects.get(weatherType);
            if (effect != null && effect.isEnabled()) {
                // 应用对应的效果
                effect.apply(player, world);
            }
        }
    }
    
    @Override
    public void remove(Player player, World world) {
        // 移除所有可能应用的效果
        for (Map<String, BaseWeatherEffect> weatherEffects : biomeWeatherEffects.values()) {
            for (BaseWeatherEffect effect : weatherEffects.values()) {
                effect.remove(player, world);
            }
        }
    }
    
    @Override
    public boolean isApplicable(World world) {
        // 检查天气条件
        if (condition.getWeatherTypes() != null && !condition.getWeatherTypes().isEmpty()) {
            String currentWeather = getCurrentWeatherType(world);
            if (!condition.getWeatherTypes().contains(currentWeather)) {
                return false;
            }
        }
        
        // 生物群系天气效果总是适用，具体判断在apply方法中进行
        return true;
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
    
    @Override
    public String getDescription() {
        return "根据不同生物群系和天气组合应用不同的效果";
    }
}