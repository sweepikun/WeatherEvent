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
public abstract class BaseWeatherEffect implements WeatherEffect {
    
    protected final Plugin plugin;
    protected final String effectName;
    protected final ConfigurationSection config;
    protected boolean enabled;
    protected List<PotionEffect> potionEffects; // 药水效果列表
    protected Map<String, Object> randomEffects; // 随机效果映射
    protected Map<String, Object> commands; // 命令映射
    
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
        
        // 注意：getMapList 返回 List<Map<?, ?>>，可能需要泛型处理
        for (Map<?, ?> effectMap : potionSection.getMapList("effects")) { // 修正了可能的空路径问题
            String type = (String) effectMap.get("type");
            int level = ((Number) effectMap.get("level")).intValue(); // 处理可能的 Number 类型
            int duration = ((Number) effectMap.get("duration")).intValue();
            
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
    
    // 移除 @Override
    public String getName() {
        return effectName;
    }
    
    // 移除 @Override
    @Override
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }
    
    // 移除 @Override
    public Map<String, Object> getRandomEffects() {
        return randomEffects;
    }
    
    // 移除 @Override
    public Map<String, Object> getCommands() {
        return commands;
    }
    
    // 移除 @Override
    public boolean isApplicable(World world) {
        return true; // 默认适用于所有世界，子类可以覆盖此方法
    }
    
    @SuppressWarnings("unchecked") // 抑制未经检查操作警告
    protected void tryApplyRandomEffects(Player player) {
        // 实现随机效果应用逻辑
        if (randomEffects == null || randomEffects.isEmpty()) return;
        
        double chance = (double) randomEffects.getOrDefault("chance", 0.0);
        if (Math.random() > chance) return;
        
        List<Map<String, Object>> effects = (List<Map<String, Object>>) randomEffects.get("effects");
        if (effects != null && !effects.isEmpty()) {
            Map<String, Object> effect = effects.get((int) (Math.random() * effects.size()));
            PotionEffectType type = PotionEffectType.getByName((String) effect.get("type"));
            if (type != null) {
                player.addPotionEffect(new PotionEffect(
                    type,
                    ((Number) effect.get("duration")).intValue(), // 处理可能的 Number 类型
                    ((Number) effect.get("level")).intValue()
                ));
            }
        }
    }
    
    @SuppressWarnings("unchecked") // 抑制未经检查操作警告
    protected void tryExecuteCommands(Player player) {
        // 实现命令执行逻辑
        if (commands == null || commands.isEmpty()) return;
        
        double chance = (double) commands.getOrDefault("chance", 0.0);
        if (Math.random() > chance) return;
        
        List<String> commandList = (List<String>) commands.get("list");
        if (commandList != null) {
            for (String cmd : commandList) {
                String processedCmd = cmd.replace("%player%", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCmd);
            }
        }
    }
    
    // 移除 @Override
    public String getId() {
        return effectName;
    }
    
    // 移除 @Override
    public String getDescription() {
        return config != null ? config.getString("description", "") : "";
    }
    
    // 移除 @Override
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    protected void addPotionEffect(PotionEffectType type, int duration, int level) {
        potionEffects.add(new PotionEffect(type, duration, level));
    }
    
    protected void addRandomEffect(PotionEffectType type, int duration, int level) {
        Map<String, Object> effect = new HashMap<>();
        effect.put("type", type.getName());
        effect.put("duration", duration);
        effect.put("level", level);
        
        if (randomEffects == null) {
            randomEffects = new HashMap<>();
            randomEffects.put("chance", 0.5);
            randomEffects.put("effects", new ArrayList<Map<String, Object>>());
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> effectsList = (List<Map<String, Object>>) randomEffects.get("effects");
        effectsList.add(effect);
    }
    
    protected void addCommand(String command) {
        if (commands == null) {
            commands = new HashMap<>();
            commands.put("chance", 1.0);
            commands.put("list", new ArrayList<String>());
        }
        
        @SuppressWarnings("unchecked")
        List<String> commandList = (List<String>) commands.get("list");
        commandList.add(command);
    }

    @Override
    public void loadFromConfig(ConfigurationSection config) {
        loadPotionEffects();
        loadRandomEffects();
        loadCommands();
    }
}