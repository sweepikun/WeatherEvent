package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

/**
 * 雨天效果
 * 在下雨时应用的效果
 */
public class RainEffect extends BaseEffect {
    
    public RainEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "rain", "下雨时的效果");
        loadFromConfig(config);
    }
    
    @Override
    public void loadFromConfig(ConfigurationSection config) {
        super.loadFromConfig(config);
        
        // 添加默认效果
        if (potionEffects.isEmpty()) {
            addPotionEffect(PotionEffectType.SLOW, 100, 0);
            addPotionEffect(PotionEffectType.WEAKNESS, 100, 0);
        }
        
        // 添加默认随机效果
        if (randomEffects.isEmpty()) {
            addRandomEffect(PotionEffectType.HUNGER, 200, 0);
        }
        
        // 设置默认命令
        if (commands.isEmpty()) {
            addCommand("title %player% subtitle {\"text\":\"感觉有点冷...\",\"color\":\"blue\"}");
        }
    }
    
    @Override
    public boolean isApplicable(World world) {
        return world.hasStorm() && !world.isThundering();
    }
}