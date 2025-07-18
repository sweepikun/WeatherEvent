package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

/**
 * 晴天效果
 * 在晴天时应用的效果
 */
public class ClearEffect extends BaseEffect {
    
    public ClearEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "clear", "晴天时的效果");
        loadFromConfig(config);
    }
    
    @Override
    public void loadFromConfig(ConfigurationSection config) {
        super.loadFromConfig(config);
        
        // 添加默认效果
        if (potionEffects.isEmpty()) {
            addPotionEffect(PotionEffectType.SPEED, 100, 0);
            addPotionEffect(PotionEffectType.JUMP, 100, 0);
        }
        
        // 添加默认随机效果
        if (randomEffects.isEmpty()) {
            addRandomEffect(PotionEffectType.REGENERATION, 100, 0);
        }
        
        // 设置默认命令
        if (commands.isEmpty()) {
            addCommand("title %player% subtitle {\"text\":\"天气真好！\",\"color\":\"green\"}");
        }
    }
    
    @Override
    public boolean isApplicable(World world) {
        return !world.hasStorm() && !world.isThundering();
    }
}