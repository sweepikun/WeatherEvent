package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 日出效果类
 */
public class SunriseEffect extends BaseWeatherEffect {

    /**
     * 构造一个日出效果
     * @param plugin 插件实例
     * @param config 配置部分
     */
    public SunriseEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "sunrise", config);
        
        if (config == null) return;
        
        // 如果没有配置药水效果，添加默认的效果
        if (getPotionEffects().isEmpty() && isEnabled()) {
            addPotionEffect(PotionEffectType.REGENERATION, 100, 0);
        }
    }
    
    @Override
    public void loadFromConfig(ConfigurationSection config) {
        // 从配置加载设置
        super.loadFromConfig(config);
    }

    /**
     * 检查效果是否适用于当前世界状态
     * @param world 目标世界
     * @return 是否适用
     */
    @Override
    public boolean isApplicable(World world) {
        long time = world.getTime();
        return time >= 0 && time < 1000;
    }
}