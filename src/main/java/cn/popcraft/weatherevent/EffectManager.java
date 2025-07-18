package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.effects.*;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 效果管理器，负责管理和应用所有效果
 */
public class EffectManager {
    private final WeatherEvent plugin;
    private final Map<String, WeatherEffect> effects;
    private BukkitTask effectTask;
    private int checkInterval;
    private boolean enabled;
    private List<String> enabledWorlds;

    /**
     * 构造一个效果管理器
     * @param plugin 插件实例
     */
    public EffectManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.effects = new HashMap<>();
        this.checkInterval = 100;
        this.enabled = true;
        this.enabledWorlds = new ArrayList<>();
        
        // 注册所有效果
        registerEffects();
    }

    /**
     * 注册所有效果
     */
    private void registerEffects() {
        // 注册天气效果
        registerEffect(new RainEffect(plugin));
        registerEffect(new ThunderEffect(plugin));
        registerEffect(new ClearEffect(plugin));
        
        // 注册时间效果
        registerEffect(new SunriseEffect(plugin));
        registerEffect(new DayEffect(plugin));
        registerEffect(new SunsetEffect(plugin));
        registerEffect(new NightEffect(plugin));
    }

    /**
     * 注册一个效果
     * @param effect 效果实例
     */
    public void registerEffect(WeatherEffect effect) {
        effects.put(effect.getId(), effect);
    }

    /**
     * 从配置中加载效果设置
     * @param config 配置部分
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) return;
        
        // 加载全局设置
        enabled = config.getBoolean("enabled", true);
        checkInterval = config.getInt("check-interval", 100);
        enabledWorlds = config.getStringList("enabled-worlds");
        
        // 加载各个效果的设置
        ConfigurationSection effectsConfig = config.getConfigurationSection("effects");
        if (effectsConfig == null) return;
        
        for (String effectId : effects.keySet()) {
            WeatherEffect effect = effects.get(effectId);
            ConfigurationSection effectConfig = effectsConfig.getConfigurationSection(effectId);
            effect.loadFromConfig(effectConfig);
        }
    }

    /**
     * 启动效果应用任务
     */
    public void startEffectTask() {
        // 取消已有的任务
        stopEffectTask();
        
        // 创建新任务
        effectTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::applyEffects, 20L, checkInterval);
    }

    /**
     * 停止效果应用任务
     */
    public void stopEffectTask() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
            effectTask = null;
        }
    }

    /**
     * 应用效果到所有玩家
     */
    public void applyEffects() {
        if (!enabled) return;
        
        // 遍历所有世界
        for (World world : plugin.getServer().getWorlds()) {
            // 检查世界是否启用
            if (!isWorldEnabled(world.getName())) continue;
            
            // 获取适用于当前世界的效果
            List<WeatherEffect> applicableEffects = getApplicableEffects(world);
            
            // 应用效果到世界中的所有玩家
            for (Player player : world.getPlayers()) {
                for (WeatherEffect effect : applicableEffects) {
                    effect.applyEffect(player);
                }
            }
        }
    }

    /**
     * 获取适用于当前世界的效果
     * @param world 目标世界
     * @return 适用的效果列表
     */
    public List<WeatherEffect> getApplicableEffects(World world) {
        List<WeatherEffect> applicableEffects = new ArrayList<>();
        
        for (WeatherEffect effect : effects.values()) {
            if (effect.isEnabled() && effect.isApplicable(world)) {
                applicableEffects.add(effect);
            }
        }
        
        return applicableEffects;
    }

    /**
     * 检查世界是否启用效果
     * @param worldName 世界名称
     * @return 是否启用
     */
    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.isEmpty() || enabledWorlds.contains(worldName);
    }

    /**
     * 获取所有效果
     * @return 效果映射
     */
    public Map<String, WeatherEffect> getEffects() {
        return effects;
    }

    /**
     * 获取指定ID的效果
     * @param id 效果ID
     * @return 效果实例，如果不存在则返回null
     */
    public WeatherEffect getEffect(String id) {
        return effects.get(id);
    }

    /**
     * 检查效果管理器是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置效果管理器是否启用
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
        if (enabled) {
            startEffectTask();
        } else {
            stopEffectTask();
        }
    }
}