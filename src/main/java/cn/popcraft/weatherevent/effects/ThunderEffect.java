package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
/**
 * 雷暴天气效果
 * 在雷暴时应用的效果
 */
public class ThunderEffect extends BaseWeatherEffect {

    public ThunderEffect(WeatherEvent plugin, ConfigurationSection config) {
        super(plugin, "thunder", config); // 使用固定名称"thunder"
        // 如果需要设置名称和描述，可以直接在父类中处理，或移除以下调用（如果父类有相应字段）
        // 假设父类有 setEffectName 和 setDescription 方法，如果没有，可以使用硬编码或移除
        // 基于错误，移除 setEffectName 和 setDescription 调用
        // 或者，如果父类有字段，可以直接赋值（如 this.effectName = "thunder";，但未定义）
    }

    // 移除 @Override 注解，因为父类没有此方法
    public void loadFromConfig(ConfigurationSection config) {
        // 移除 super.loadFromConfig(config); 调用，因为父类没有此方法
        // 直接在子类中实现加载逻辑，并添加空检查
        // 添加默认效果，确保字段不为空
        if (potionEffects == null || potionEffects.isEmpty()) {
            addPotionEffect(PotionEffectType.BLINDNESS, 100, 0);
            addPotionEffect(PotionEffectType.SLOW, 120, 1);
        }

        // 添加默认随机效果，确保字段不为空
        if (randomEffects == null || !randomEffects.containsKey("effects")
                || ((List<?>) randomEffects.get("effects")).isEmpty()) {
            addRandomEffect(PotionEffectType.CONFUSION, 100, 0);
            addRandomEffect(PotionEffectType.WEAKNESS, 120, 1);
        }

        // 设置默认命令，确保字段不为空
        if (commands == null || !commands.containsKey("list") || ((List<?>) commands.get("list")).isEmpty()) {
            // 使用Spigot API播放声音
for(Player player : Bukkit.getOnlinePlayers()) {
    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
}
            addCommand("tellraw %player% {\"text\":\"雷声轰鸣！\",\"color\":\"red\"}");
        }
    }

    @Override
    public boolean isApplicable(World world) {
        return world.isThundering(); // 这部分正确，无需修改
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