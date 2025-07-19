package cn.popcraft.weatherevent.effects;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 雨天减速效果
 * 在下雨时给玩家施加缓慢效果
 */
public class RainSlowEffect extends BaseWeatherEffect {
    
    /**
     * 创建一个雨天减速效果
     * @param plugin 插件实例
     * @param config 效果配置
     */
    public RainSlowEffect(Plugin plugin, ConfigurationSection config) {
        super(plugin, "rain_slow", config);
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        // 如果没有配置药水效果，添加默认的缓慢效果
        if (getPotionEffects().isEmpty() && isEnabled()) {
            int slowLevel = config != null ? config.getInt("slow-level", 0) : 0;
            int duration = 5 * 20; // 5秒 * 20刻 = 100刻
            addPotionEffect(
                PotionEffectType.SLOW, 
                duration, 
                slowLevel
            );
        }
    }
    
    @Override
    public void apply(Player player, World world) {
        if (!enabled || !isApplicable(world)) return;
        
        // 只有当玩家在室外时才应用效果
        if (player.getLocation().getBlock().getLightFromSky() > 4) {
            // 应用药水效果
            for (PotionEffect effect : getPotionEffects()) {
                player.addPotionEffect(effect);
            }
            
            // 尝试应用随机效果
            tryApplyRandomEffects(player);
            
            // 尝试执行命令
            tryExecuteCommands(player);
        }
    }
    
    @Override
    public boolean isApplicable(World world) {
        // 只在下雨但不打雷的情况下应用
        return world.hasStorm() && !world.isThundering();
    }
    
    @Override
    public String getDescription() {
        return "在雨天时减慢移动速度";
    }
}