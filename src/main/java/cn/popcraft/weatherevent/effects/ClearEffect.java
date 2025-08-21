package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;

/**
 * 晴天效果
 * 在晴天时应用的效果
 */
public class ClearEffect extends BaseWeatherEffect {
    
    public ClearEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "clear", config);
    }
    
    // 移除 @Override 注解，因为父类没有此方法
    public void loadFromConfig(ConfigurationSection config) {
        // 移除 super.loadFromConfig(config); 调用，因为父类没有此方法
        // 可以直接加载配置或使用父类字段
        
        // 添加默认效果，确保字段不为空
        if (potionEffects == null || potionEffects.isEmpty()) {
            addPotionEffect(PotionEffectType.SPEED, 100, 0);
            addPotionEffect(PotionEffectType.JUMP, 100, 0);
        }
        
        // 添加默认随机效果，确保字段不为空
        if (randomEffects == null || !randomEffects.containsKey("effects") || ((List<?>) randomEffects.get("effects")).isEmpty()) {
            addRandomEffect(PotionEffectType.REGENERATION, 100, 0);
        }
        
        // 设置默认命令，确保字段不为空
        if (commands == null || !commands.containsKey("list") || ((List<?>) commands.get("list")).isEmpty()) {
            addCommand("playsound minecraft:entity.experience_orb.pickup master %player% ~ ~ ~ 100 1");
            addCommand("tellraw %player% {\"text\":\"天气真好！\",\"color\":\"green\"}");
        }
    }
    
    @Override
    public boolean isApplicable(World world) {
        return !world.hasStorm() && !world.isThundering();
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return super.getPotionEffects();
    }

    private boolean enabled;
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}