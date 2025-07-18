package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

/**
 * 雷暴天气效果
 * 在雷暴时应用的效果
 */
public class ThunderEffect extends BaseEffect {
    
    public ThunderEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "thunder", "雷暴时的效果");
        loadFromConfig(config);
    }
    
    @Override
    public void loadFromConfig(ConfigurationSection config) {
        super.loadFromConfig(config);
        
        // 添加默认效果
        if (potionEffects.isEmpty()) {
            addPotionEffect(PotionEffectType.BLINDNESS, 100, 0);
            addPotionEffect(PotionEffectType.SLOW, 120, 1);
        }
        
        // 添加默认随机效果
        if (randomEffects.isEmpty()) {
            addRandomEffect(PotionEffectType.CONFUSION, 100, 0);
            addRandomEffect(PotionEffectType.WEAKNESS, 120, 1);
        }
        
        // 设置默认命令
        if (commands.isEmpty()) {
            addCommand("playsound minecraft:entity.lightning_bolt.thunder master %player% ~ ~ ~ 100 1");
            addCommand("title %player% subtitle {\"text\":\"雷声轰鸣！\",\"color\":\"red\"}");
        }
    }
    
    @Override
    public boolean isApplicable(World world) {
        return world.isThundering();
    }
}