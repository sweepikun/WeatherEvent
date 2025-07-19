package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List; // 导入 List 接口，用于 getPotionEffects() 方法

/**
 * 日落效果类
 */
public class SunsetEffect extends BaseWeatherEffect {

    /**
     * 构造一个日落效果
     * @param plugin 插件实例
     */
    private boolean enabled;
    
    public SunsetEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "sunset", config);
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        if (config != null) {
            this.enabled = config.getBoolean("enabled", true);
            
            // 如果没有配置药水效果，添加默认的效果
            if (getPotionEffects().isEmpty() && isEnabled()) {
                addPotionEffect(PotionEffectType.REGENERATION, 100, 0);
                addPotionEffect(PotionEffectType.JUMP, 100, 1);
            }
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
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
    
    // 新添加的方法：修复返回类型不兼容的错误
    // 假设 WeatherEffect 或 BaseEffect 定义了 getPotionEffects() 返回 List<PotionEffect>
    // 这里直接返回 potionEffects 字段，确保兼容性
    @Override
    public List<PotionEffect> getPotionEffects() {
        return potionEffects; // 返回 potionEffects 列表，如果是空列表，也能正常工作
    }
}