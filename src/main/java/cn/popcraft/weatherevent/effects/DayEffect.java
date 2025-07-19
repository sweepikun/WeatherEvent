package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 白天效果类
 */
public class DayEffect extends BaseWeatherEffect {

    /**
     * 构造一个白天效果
     * @param plugin 插件实例
     */
    public DayEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "day", config);
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        // 从配置加载特定于白天效果的设置
        if (config != null) {
            // 如果没有配置药水效果，添加默认的效果
            if (getPotionEffects().isEmpty() && isEnabled()) {
                addPotionEffect(PotionEffectType.FAST_DIGGING, 100, 0);
            }
        }
    }

    /**
     * 检查效果是否适用于当前世界状态
     * @param world 目标世界
     * @return 是否适用
     */
    @Override
    public boolean isApplicable(World world) {
        long time = world.getTime();
        return time >= 1000 && time < 13000;
    }
}