package cn.popcraft.weatherevent.effects;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * 雷暴伤害效果
 * 在雷暴天气时，有几率对暴露在外的玩家造成伤害和负面效果
 */
public class ThunderDamageEffect extends BaseWeatherEffect {
    
    private final double damageChance;
    private final double damageAmount;
    private final boolean applyWeakness;
    private final Random random;
    
    /**
     * 创建一个雷暴伤害效果
     * @param plugin 插件实例
     * @param config 效果配置
     */
    public ThunderDamageEffect(Plugin plugin, ConfigurationSection config) {
        super(plugin, "thunder_damage", config);
        
        // 从配置中读取参数
        this.damageChance = config != null ? config.getDouble("damage-chance", 0.1) : 0.1;
        this.damageAmount = config != null ? config.getDouble("damage-amount", 2.0) : 2.0;
        this.applyWeakness = config != null && config.getBoolean("apply-weakness", true);
        this.random = new Random();
        
        // 如果配置了虚弱效果，添加到药水效果列表
        if (applyWeakness && potionEffects.isEmpty()) {
            int duration = 10 * 20; // 10秒 * 20刻 = 200刻
            potionEffects.add(new PotionEffect(
                PotionEffectType.WEAKNESS, 
                duration, 
                0, 
                false, 
                true
            ));
        }
    }
    
    @Override
    public void apply(Player player, World world) {
        if (!enabled || !isApplicable(world)) return;
        
        // 只有当玩家在室外时才应用效果
        if (player.getLocation().getBlock().getLightFromSky() > 4) {
            // 根据几率决定是否造成伤害
            if (random.nextDouble() < damageChance) {
                // 播放雷击音效但不实际生成闪电（避免破坏地形）
                world.playSound(player.getLocation(), "entity.lightning_bolt.thunder", 1.0f, 1.0f);
                
                // 造成伤害
                if (damageAmount > 0) {
                    player.damage(damageAmount);
                }
                
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
    }
    
    @Override
    public boolean isApplicable(World world) {
        // 只在雷暴天气时应用
        return world.isThundering();
    }
    
    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder("在雷暴天气时有");
        desc.append(String.format("%.1f%%", damageChance * 100));
        desc.append("几率受到");
        desc.append(String.format("%.1f", damageAmount));
        desc.append("点伤害");
        
        if (applyWeakness) {
            desc.append("并获得虚弱效果");
        }
        
        return desc.toString();
    }
}