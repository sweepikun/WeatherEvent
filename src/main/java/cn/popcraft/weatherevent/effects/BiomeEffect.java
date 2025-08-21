package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * 生物群系效果类
 * 在特定生物群系中为玩家提供特殊效果
 */
public class BiomeEffect extends BaseWeatherEffect {

    private final String targetBiome;
    
    /**
     * 构造一个生物群系效果
     * @param plugin 插件实例
     * @param biomeName 目标生物群系名称
     * @param config 效果配置
     */
    public BiomeEffect(WeatherEvent plugin, String biomeName, ConfigurationSection config) {
        super(plugin, "biome_" + biomeName.toLowerCase(), config);
        this.targetBiome = biomeName.toUpperCase();
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        // 从配置加载特定于生物群系效果的设置
        if (config != null) {
            // 基础配置已在父类中加载
            super.loadFromConfig(config);
        }
    }

    /**
     * 检查效果是否适用于当前玩家位置
     * @param player 目标玩家
     * @param world 目标世界
     * @return 是否适用
     */
    public boolean isApplicable(Player player, World world) {
        // 检查玩家是否在目标生物群系中
        Biome playerBiome = player.getLocation().getBlock().getBiome();
        boolean biomeMatch = playerBiome.name().equals(targetBiome);
        
        // 检查天气条件
        if (condition.getWeatherTypes() != null && !condition.getWeatherTypes().isEmpty()) {
            String currentWeather = getCurrentWeatherType(world);
            return biomeMatch && condition.getWeatherTypes().contains(currentWeather);
        }
        
        return biomeMatch;
    }

    /**
     * 检查效果是否适用于当前世界状态
     * 生物群系效果不依赖于世界状态，而是依赖于玩家位置
     * @param world 目标世界
     * @return 始终返回true，具体判断在apply方法中进行
     */
    @Override
    public boolean isApplicable(World world) {
        // 生物群系效果不依赖于世界状态，而是依赖于玩家位置
        // 因此这里返回true，具体判断在apply方法中进行
        return true;
    }
    
    /**
     * 应用效果到玩家
     * @param player 目标玩家
     * @param world 目标世界
     */
    @Override
    public void apply(Player player, World world) {
        // 只有当玩家在目标生物群系中时才应用效果
        if (isApplicable(player, world)) {
            super.apply(player, world);
        }
    }

    @Override
    public String getDescription() {
        return "在" + targetBiome + "生物群系中提供特殊效果";
    }
}