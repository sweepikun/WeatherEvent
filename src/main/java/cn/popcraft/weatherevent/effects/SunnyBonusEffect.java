package cn.popcraft.weatherevent.effects;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

/**
 * 晴天奖励效果
 * 在晴天时给玩家提供速度和跳跃提升效果
 */
public class SunnyBonusEffect extends BaseWeatherEffect {
    
    private final boolean giveSpeed;
    private final boolean giveJumpBoost;
    
    /**
     * 创建一个晴天奖励效果
     * @param plugin 插件实例
     * @param config 效果配置
     */
    public SunnyBonusEffect(Plugin plugin, ConfigurationSection config) {
        super(plugin, "sunny_bonus", config);
        
        // 从配置中读取是否提供速度和跳跃提升效果
        this.giveSpeed = config != null && config.getBoolean("give-speed", true);
        this.giveJumpBoost = config != null && config.getBoolean("give-jump-boost", true);
        
        // 如果没有配置药水效果，添加默认效果
        if (potionEffects.isEmpty()) {
            int effectLevel = config != null ? config.getInt("effect-level", 0) : 0;
            int duration = 5 * 20; // 5秒 * 20刻 = 100刻
            
            if (giveSpeed) {
                potionEffects.add(new PotionEffect(
                    PotionEffectType.SPEED, 
                    duration, 
                    effectLevel, 
                    false, // 不显示粒子效果
                    true   // 显示图标
                ));
            }
            
            if (giveJumpBoost) {
                potionEffects.add(new PotionEffect(
                    PotionEffectType.JUMP, 
                    duration, 
                    effectLevel, 
                    false, // 不显示粒子效果
                    true   // 显示图标
                ));
            }
        }
    }
    
    @Override
    public void apply(Player player, World world) {
        if (!enabled || !isApplicable(world)) return;
        
        // 只有当玩家在室外且是白天时才应用效果
        if (player.getLocation().getBlock().getLightFromSky() > 4 && (world.getTime() < 12300 || world.getTime() > 23850)) {
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
        // 只在晴天时应用
        return !world.hasStorm() && !world.isThundering();
    }
    
    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder("在晴天时提供");
        if (giveSpeed) {
            desc.append("速度提升");
            if (giveJumpBoost) {
                desc.append("和");
            }
        }
        if (giveJumpBoost) {
            desc.append("跳跃提升");
        }
        return desc.toString();
    }
}