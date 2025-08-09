package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * 洞穴效果类
 * 在地下环境中为玩家提供特殊效果
 */
public class CaveEffect extends BaseWeatherEffect {

    /**
     * 构造一个洞穴效果
     * @param plugin 插件实例
     * @param config 效果配置
     */
    public CaveEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "cave", config);
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        // 从配置加载特定于洞穴效果的设置
        if (config != null) {
            // 如果没有配置药水效果，添加默认的效果
            if (getPotionEffects().isEmpty() && isEnabled()) {
                addPotionEffect(PotionEffectType.NIGHT_VISION, 400, 0);
                addPotionEffect(PotionEffectType.FAST_DIGGING, 200, 0);
            }
        }
    }

    /**
     * 检查效果是否适用于当前玩家位置
     * @param player 目标玩家
     * @param world 目标世界
     * @return 是否适用
     */
    public boolean isApplicable(Player player, World world) {
        // 检查玩家是否在洞穴环境中（高度和光照条件）
        int playerY = player.getLocation().getBlockY();
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        
        // 使用条件对象中的高度和光照限制
        boolean heightCheck = playerY >= condition.getMinHeight() && playerY <= condition.getMaxHeight();
        boolean lightCheck = lightLevel >= condition.getMinLight() && lightLevel <= condition.getMaxLight();
        
        return heightCheck && lightCheck;
    }

    /**
     * 检查效果是否适用于当前世界状态
     * 洞穴效果不依赖于世界状态，而是依赖于玩家位置
     * @param world 目标世界
     * @return 始终返回true，具体判断在apply方法中进行
     */
    @Override
    public boolean isApplicable(World world) {
        // 洞穴效果不依赖于世界状态，而是依赖于玩家位置
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
        // 只有当玩家在洞穴环境中时才应用效果
        if (isApplicable(player, world)) {
            super.apply(player, world);
        }
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }
    
    /**
     * 添加药水效果
     * @param type 药水效果类型
     * @param duration 持续时间（tick）
     * @param amplifier 效果等级
     */
    protected void addPotionEffect(PotionEffectType type, int duration, int amplifier) {
        potionEffects.add(new PotionEffect(type, duration, amplifier, false, false, true));
    }
}