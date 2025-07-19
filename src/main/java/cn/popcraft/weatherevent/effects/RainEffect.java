package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

/**
 * 雨天效果
 * 在下雨时应用的效果
 */
public class RainEffect extends BaseWeatherEffect {
    
    public RainEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "rain", config);
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        // 添加默认效果
        if (getPotionEffects().isEmpty() && isEnabled()) {
            addPotionEffect(PotionEffectType.SLOW, 100, 0);
            addPotionEffect(PotionEffectType.WEAKNESS, 100, 0);
        }
        
        // 添加默认随机效果
        if (getRandomEffects().isEmpty() && isEnabled()) {
            addRandomEffect(PotionEffectType.HUNGER, 200, 0);
        }
        
        // 设置默认命令
        if (getCommands().isEmpty() && isEnabled()) {
            addCommand("title %player% subtitle {\"text\":\"感觉有点冷...\",\"color\":\"blue\"}");
        }
    }
    
    @Override
    public boolean isApplicable(World world) {
        return world.hasStorm() && !world.isThundering();
    }
}