package cn.popcraft.weatherevent.manager;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.effects.WeatherEffect;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * 天气效果管理器
 * 负责管理和应用所有的天气效果
 */
public class WeatherEffectManager {
    
    private final WeatherEvent plugin;
    private final List<WeatherEffect> registeredEffects;
    private final Map<UUID, List<WeatherEffect>> activeEffects;
    private BukkitTask effectTask;
    
    public WeatherEffectManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.registeredEffects = new ArrayList<>();
        this.activeEffects = new HashMap<>();
    }
    
    /**
     * 注册一个天气效果
     * @param effect 要注册的效果
     */
    public void registerEffect(WeatherEffect effect) {
        registeredEffects.add(effect);
        plugin.getLogger().info("已注册天气效果: " + effect.getDescription());
    }
    
    /**
     * 启动效果应用任务
     */
    public void startEffectTask() {
        // 取消之前的任务（如果有）
        if (effectTask != null) {
            effectTask.cancel();
        }
        
        // 创建新任务，每秒检查一次
        effectTask = Bukkit.getScheduler().runTaskTimer(plugin, this::applyEffects, 20L, 20L);
    }
    
    /**
     * 停止效果应用任务
     */
    public void stopEffectTask() {
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
        
        // 移除所有活跃效果
        removeAllEffects();
    }
    
    /**
     * 应用所有适用的天气效果
     */
    private void applyEffects() {
        // 遍历所有在线玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            // 检查玩家是否有免疫权限
            if (player.hasPermission("weatherevent.bypass")) {
                // 如果玩家有免疫权限，移除所有效果
                List<WeatherEffect> playerActiveEffects = activeEffects.get(player.getUniqueId());
                if (playerActiveEffects != null && !playerActiveEffects.isEmpty()) {
                    for (WeatherEffect effect : new ArrayList<>(playerActiveEffects)) {
                        effect.remove(player, player.getWorld());
                    }
                    playerActiveEffects.clear();
                }
                continue;
            }
            
            World world = player.getWorld();
            
            // 检查是否只在主世界应用效果
            if (plugin.getConfig().getBoolean("main-world-only", true) && 
                !world.getName().equals(plugin.getConfig().getString("main-world-name", "world"))) {
                continue;
            }
            
            // 获取玩家当前的活跃效果
            List<WeatherEffect> playerActiveEffects = activeEffects.computeIfAbsent(
                player.getUniqueId(), k -> new ArrayList<>());
            
            // 检查每个注册的效果
            for (WeatherEffect effect : registeredEffects) {
                boolean shouldApply = effect.isApplicable(world);
                boolean isActive = playerActiveEffects.contains(effect);
                
                if (shouldApply && !isActive) {
                    // 应用新效果
                    effect.apply(player, world);
                    playerActiveEffects.add(effect);
                } else if (!shouldApply && isActive) {
                    // 移除不再适用的效果
                    effect.remove(player, world);
                    playerActiveEffects.remove(effect);
                } else if (shouldApply) {
                    // 刷新现有效果
                    effect.apply(player, world);
                }
            }
        }
        
        // 清理已离线玩家的效果记录
        activeEffects.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }
    
    /**
     * 移除所有活跃效果
     */
    private void removeAllEffects() {
        for (Map.Entry<UUID, List<WeatherEffect>> entry : activeEffects.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                for (WeatherEffect effect : entry.getValue()) {
                    effect.remove(player, player.getWorld());
                }
            }
        }
        activeEffects.clear();
    }
    
    /**
     * 当天气变化时更新效果
     * @param world 天气变化的世界
     */
    public void onWeatherChange(World world) {
        // 立即应用效果，而不是等待下一个任务周期
        for (Player player : world.getPlayers()) {
            // 检查玩家是否有免疫权限
            if (player.hasPermission("weatherevent.bypass")) {
                // 如果玩家有免疫权限，移除所有效果
                List<WeatherEffect> playerActiveEffects = activeEffects.get(player.getUniqueId());
                if (playerActiveEffects != null && !playerActiveEffects.isEmpty()) {
                    for (WeatherEffect effect : new ArrayList<>(playerActiveEffects)) {
                        effect.remove(player, world);
                    }
                    playerActiveEffects.clear();
                }
                continue;
            }
            
            List<WeatherEffect> playerActiveEffects = activeEffects.computeIfAbsent(
                player.getUniqueId(), k -> new ArrayList<>());
            
            // 移除所有当前效果
            for (WeatherEffect effect : new ArrayList<>(playerActiveEffects)) {
                effect.remove(player, world);
                playerActiveEffects.remove(effect);
            }
            
            // 应用新的适用效果
            for (WeatherEffect effect : registeredEffects) {
                if (effect.isApplicable(world)) {
                    effect.apply(player, world);
                    playerActiveEffects.add(effect);
                }
            }
        }
    }
}