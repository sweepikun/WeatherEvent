package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 日落效果类
 */
public class SunsetEffect extends BaseEffect {

    /**
     * 构造一个日落效果
     * @param plugin 插件实例
     */
    public SunsetEffect(WeatherEvent plugin) {
        super(plugin, "sunset", "日落效果：增加跳跃能力");
    }

    /**
     * 从配置中加载效果
     * @param config 配置部分
     */
    @Override
    public void loadFromConfig(ConfigurationSection config) {
        super.loadFromConfig(config);
        
        if (config == null) return;
        
        // 如果没有配置药水效果，添加默认的效果
        if (potionEffects.isEmpty() && enabled) {
            potionEffects.add(new PotionEffect(PotionEffectType.JUMP, 100, 1));
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
        return time >= 13000 && time < 14000;
    }
}