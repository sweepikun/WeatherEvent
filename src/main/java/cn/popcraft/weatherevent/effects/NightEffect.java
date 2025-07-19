package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List; // 导入 List 接口，用于 getPotionEffects() 方法

/**
 * 夜晚效果类
 */
public class NightEffect extends BaseWeatherEffect {

    /**
     * 构造一个夜晚效果
     * @param plugin 插件实例
     */
    public NightEffect(WeatherEvent plugin, String name, ConfigurationSection config) {
        super(plugin, name, config);
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        // 从配置加载特定于夜晚效果的设置
        if (config != null) {
            // 如果没有配置药水效果，添加默认的效果
            if (getPotionEffects().isEmpty() && isEnabled()) {
                addPotionEffect(PotionEffectType.NIGHT_VISION, 400, 0);
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
        return time >= 14000 || time < 0; // 注意：time < 0 可能表示世界重置或其他特殊情况
    }
    
    // 新添加的方法：修复返回类型不兼容的错误
    // 假设 WeatherEffect 或 BaseEffect 定义了 getPotionEffects() 返回 List<PotionEffect>
    // 这里直接返回 potionEffects 字段，确保兼容性
    @Override
    public List<PotionEffect> getPotionEffects() {
        return potionEffects; // 返回 potionEffects 列表，如果是空列表，也能正常工作
    }
}