package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.config.ChainEffect;
import cn.popcraft.weatherevent.condition.ConditionChecker;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * 基础效果类，提供通用的效果实现
 */
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.potion.PotionEffect;

public abstract class BaseEffect implements WeatherEffect {
    protected final WeatherEvent plugin;
    protected final String id;
    protected final String description;
    protected boolean enabled;
    protected List<ChainEffect> chainEffects;
    protected List<PotionEffect> potionEffects;
    protected List<PotionEffect> randomEffects;
    protected double randomEffectChance;
    protected List<String> commands;
    protected double commandChance;
    protected final Random random;

    /**
     * 构造一个基础效果
     * @param plugin 插件实例
     * @param id 效果ID
     * @param description 效果描述
     */
    public BaseEffect(WeatherEvent plugin, String id, String description) {
        this.plugin = plugin;
        this.id = id;
        this.description = description;
        this.random = new Random();
        this.potionEffects = new ArrayList<>();
        this.randomEffects = new ArrayList<>();
        this.chainEffects = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.randomEffectChance = 0.0;
        this.commandChance = 0.0;
        this.enabled = false;
    }

    /**
     * 从配置中加载效果
     * @param config 配置部分
     */
    @Override
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) {
            this.enabled = false;
            return;
        }

        this.enabled = config.getBoolean("enabled", false);
        
        // 加载药水效果
        loadPotionEffects(config.getConfigurationSection("potion-effects"));
        
        // 加载随机效果
        loadRandomEffects(config.getConfigurationSection("random-effects"));
        
        // 加载命令
        loadCommands(config.getConfigurationSection("commands"));
    }

    /**
     * 加载药水效果
     * @param config 药水效果配置部分
     */
    protected void loadPotionEffects(ConfigurationSection config) {
        potionEffects.clear();
        if (config == null) return;
        
        for (int i = 0; i < config.getList("").size(); i++) {
            ConfigurationSection effectConfig = config.getConfigurationSection(String.valueOf(i));
            if (effectConfig == null) continue;
            
            String type = effectConfig.getString("type");
            if (type == null) continue;
            
            PotionEffectType effectType = PotionEffectType.getByName(type);
            if (effectType == null) continue;
            
            int level = effectConfig.getInt("level", 0);
            int duration = effectConfig.getInt("duration", 100);
            
            potionEffects.add(new PotionEffect(effectType, duration, level));
        }
    }

    /**
     * 加载随机效果
     * @param config 随机效果配置部分
     */
    protected void loadRandomEffects(ConfigurationSection config) {
        randomEffects.clear();
        randomEffectChance = 0.0;
        if (config == null) return;
        
        randomEffectChance = config.getDouble("chance", 0.0);
        ConfigurationSection effectsConfig = config.getConfigurationSection("effects");
        if (effectsConfig == null) return;
        
        for (int i = 0; i < effectsConfig.getList("").size(); i++) {
            ConfigurationSection effectConfig = effectsConfig.getConfigurationSection(String.valueOf(i));
            if (effectConfig == null) continue;
            
            String type = effectConfig.getString("type");
            if (type == null) continue;
            
            PotionEffectType effectType = PotionEffectType.getByName(type);
            if (effectType == null) continue;
            
            int level = effectConfig.getInt("level", 0);
            int duration = effectConfig.getInt("duration", 100);
            
            randomEffects.add(new PotionEffect(effectType, duration, level));
        }
    }

    /**
     * 加载命令和连锁效果
     * @param config 命令配置部分
     */
    protected void loadCommands(ConfigurationSection config) {
        commands.clear();
        chainEffects.clear();
        
        if (config == null) return;
        
        commandChance = config.getDouble("chance", 0.0);
        commands = config.getStringList("list");
        
        // 加载连锁效果
        if (config.isConfigurationSection("chain-effects")) {
            ConfigurationSection chainSection = config.getConfigurationSection("chain-effects");
            // 支持两种配置格式
            if (chainSection.getList("") != null) {
                List<Map<String, Object>> listData = (List<Map<String, Object>>) chainSection.getList("");
                chainEffects = ChainEffect.fromConfig(listData);
            } else {
                Map<String, Object> mapData = new HashMap<>();
                for (String key : chainSection.getKeys(false)) {
                    mapData.put(key, chainSection.get(key));
                }
                // 包装mapData以符合fromConfigMap的参数要求
                Map<String, Map<String, Object>> wrappedMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : mapData.entrySet()) {
                    if (entry.getValue() instanceof ConfigurationSection) {
                        Map<String, Object> innerMap = new HashMap<>();
                        ConfigurationSection innerSection = (ConfigurationSection) entry.getValue();
                        for (String innerKey : innerSection.getKeys(true)) {
                            innerMap.put(innerKey, innerSection.get(innerKey));
                        }
                        wrappedMap.put(entry.getKey(), innerMap);
                    }
                }
                chainEffects = ChainEffect.fromConfigMap(wrappedMap);
            }
        }
    }

    /**
     * 应用效果到玩家
     * @param player 目标玩家
     * @param world 目标世界
     */
    @Override
    public void apply(Player player, World world) {
        if (!enabled) return;
        
        // 应用常规药水效果
        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }
        
        // 随机应用额外效果
        if (!randomEffects.isEmpty() && random.nextDouble() < randomEffectChance) {
            PotionEffect randomEffect = randomEffects.get(random.nextInt(randomEffects.size()));
            player.addPotionEffect(randomEffect);
        }
        
        // 随机执行命令
        if (!commands.isEmpty() && random.nextDouble() < commandChance) {
            String command = commands.get(random.nextInt(commands.size()));
            executeCommand(player, command);
        }
    }
    
    /**
     * 移除玩家的效果
     * @param player 目标玩家
     * @param world 目标世界
     */
    @Override
    public void remove(Player player, World world) {
        if (!enabled) return;
        
        // 移除药水效果
        for (PotionEffect effect : potionEffects) {
            player.removePotionEffect(effect.getType());
        }
        
        // 移除随机效果
        for (PotionEffect effect : randomEffects) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * 执行命令并检查是否需要触发连锁效果
     * @param player 玩家
     * @param command 命令
     */
    protected void executeCommand(Player player, String command) {
        String processedCommand = command.replace("%player%", player.getName());
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
        
        // 触发连锁效果
        triggerChainEffects(player);
    }

    /**
     * 触发连锁效果
     * @param player 玩家
     */
    protected void triggerChainEffects(Player player) {
        for (ChainEffect chainEffect : chainEffects) {
            // 检查触发几率
            if (Math.random() > chainEffect.getChance()) {
                continue;
            }
            
            // 检查条件
            if (chainEffect.getConditions() != null && 
                !ConditionChecker.checkPrerequisites(player, chainEffect.getConditions())) {
                continue;
            }
            
            // 检查延迟
            int delay = chainEffect.getDelay();
            if (delay > 0) {
                // 延迟触发
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getSharedEffectManager().applySharedEffect(player, chainEffect.getEffectId());
                    }
                }.runTaskLater(plugin, delay);
            } else {
                // 立即触发
                plugin.getSharedEffectManager().applySharedEffect(player, chainEffect.getEffectId());
            }
        }
    }

    /**
     * 获取效果ID
     * @return 效果ID
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * 获取效果描述
     * @return 效果描述
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * 检查效果是否启用
     * @return 是否启用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置效果是否启用
     * @param enabled 是否启用
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    @Override
    public Map<String, Object> getRandomEffects() {
        Map<String, Object> map = new HashMap<>();
        map.put("chance", randomEffectChance);
        map.put("effects", randomEffects);
        return map;
    }

    /**
     * 获取命令配置
     * @return 命令配置映射
     */
    public Map<String, Object> getCommands() {
        Map<String, Object> map = new HashMap<>();
        map.put("chance", commandChance);
        map.put("list", commands);
        
        if (!chainEffects.isEmpty()) {
            List<Map<String, Object>> chainEffectMaps = new ArrayList<>();
            for (ChainEffect effect : chainEffects) {
                // 创建一个表示ChainEffect的映射
                Map<String, Object> effectMap = new HashMap<>();
                effectMap.put("chance", effect.getChance());
                effectMap.put("effect-id", effect.getEffectId());
                effectMap.put("delay", effect.getDelay());
                effectMap.put("conditions", effect.getConditions());
                chainEffectMaps.add(effectMap);
            }
            map.put("chain-effects", chainEffectMaps);
        }
        
        return map;
    }

    /**
     * 检查效果是否适用于当前世界状态
     * @param world 目标世界
     * @return 是否适用
     */
    @Override
    public abstract boolean isApplicable(World world);
}