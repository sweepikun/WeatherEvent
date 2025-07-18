package cn.popcraft.weatherevent.effects;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础天气效果实现类
 * 提供了天气效果的通用实现
 */
public abstract class BaseWeatherEffect extends WeatherEffect {
    
    protected final Plugin plugin;
    protected final String effectName;
    protected final ConfigurationSection config;
    protected final boolean enabled;
    protected List<PotionEffect> potionEffects;
    protected Map<String, Object> randomEffects;
    protected Map<String, Object> commands;
    
    /**
     * 构造一个基础天气效果
     * @param plugin 插件实例
     * @param effectName 效果名称
     * @param config 效果配置
     */
    public BaseWeatherEffect(Plugin plugin, String effectName, ConfigurationSection config) {
        this.plugin = plugin;
        this.effectName = effectName;
        this.config = config;
        this.enabled = config != null && config.getBoolean("enabled", false);
        
        loadPotionEffects();
        loadRandomEffects();
        loadCommands();
    }
    
    /**
     * 从配置中加载药水效果
     */
    protected void loadPotionEffects() {
        potionEffects = new ArrayList<>();
        if (config == null) return;
        
        ConfigurationSection potionSection = config.getConfigurationSection("potion-effects");
        if (potionSection == null) return;
        
        for (Map<?, ?> effectMap : potionSection.getMapList("")) {
            String type = (String) effectMap.get("type");
            int level = (int) effectMap.get("level");
            int duration = (int) effectMap.get("duration");
            
            PotionEffectType effectType = PotionEffectType.getByName(type);
            if (effectType != null) {
                potionEffects.add(new PotionEffect(effectType, duration, level));
            }
        }
    }
    
    /**
     * 从配置中加载随机效果
     */
    protected void loadRandomEffects() {
        randomEffects = new HashMap<>();
        if (config == null) return;
        
        ConfigurationSection randomSection = config.getConfigurationSection("random-effects");
        if (randomSection == null) return;
        
        randomEffects.put("chance", randomSection.getDouble("chance", 0.0));
        
        List<Map<String, Object>> effects = new ArrayList<>();
        for (Map<?, ?> effectMap : randomSection.getMapList("effects")) {
            Map<String, Object> effect = new HashMap<>();
            effect.put("type", effectMap.get("type"));
            effect.put("level", effectMap.get("level"));
            effect.put("duration", effectMap.get("duration"));
            effects.add(effect);
        }
        randomEffects.put("effects", effects);
    }
    
    /**
     * 从配置中加载命令
     */
    protected void loadCommands() {
        commands = new HashMap<>();
        if (config == null) return;
        
        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection == null) return;
        
        commands.put("chance", commandsSection.getDouble("chance", 0.0));
        commands.put("list", commandsSection.getStringList("list"));
    }
    
    @Override
    public void apply(Player player, World world) {
        if (!enabled || !isApplicable(world)) return;
        
        // 应用药水效果
        for (PotionEffect effect : getPotionEffects()) {
            player.addPotionEffect(effect);
        }
        
        // 尝试应用随机效果
        tryApplyRandomEffects(player);
        
        // 尝试执行命令
        tryExecuteCommands(player);
    }
    
    @Override
    public void remove(Player player, World world) {
        // 移除药水效果
        for (PotionEffect effect : getPotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    @Override
    public String getName() {
        return effectName;
    }
    
    @Override
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }
    
    @Override
    public Map<String, Object> getRandomEffects() {
        return randomEffects;
    }
    
    @Override
    public Map<String, Object> getCommands() {
        return commands;
    }
}