package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.config.ChainEffect;
import cn.popcraft.weatherevent.config.DynamicParameter;
import cn.popcraft.weatherevent.config.EffectCondition;
import cn.popcraft.weatherevent.config.SharedEffectManager;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
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
    
    protected final WeatherEvent plugin;
    protected final String effectName;
    protected final ConfigurationSection config;
    protected boolean enabled;
    protected List<PotionEffect> potionEffects; // 药水效果列表
    protected Map<String, Object> randomEffects; // 随机效果映射
    protected Map<String, Object> commands; // 命令映射
    protected Map<String, Object> title; // 标题映射
    protected Map<String, Object> actionBar; // 动作栏映射
    protected Map<String, Object> sound; // 声音映射
    protected Map<String, Object> message; // 消息映射
    protected EffectCondition condition; // 效果触发条件
    protected List<ChainEffect> chainEffects; // 连锁效果列表
    
    /**
     * 构造一个基础天气效果
     * @param plugin 插件实例
     * @param effectName 效果名称
     * @param config 效果配置
     */
    public BaseWeatherEffect(WeatherEvent plugin, String effectName, ConfigurationSection config) {
        this.plugin = plugin;
        this.effectName = effectName;
        this.config = config;
        this.enabled = config != null && config.getBoolean("enabled", false);
        this.condition = new EffectCondition();
        
        loadConditions();
        loadPotionEffects();
        loadRandomEffects();
        loadCommands();
        loadTitle();
        loadActionBar();
        loadSound();
        loadMessage();
        loadChainEffects();
    }
    
    /**
     * 从配置中加载触发条件
     */
    protected void loadConditions() {
        if (config == null) return;
        
        // 加载生物群系限制
        if (config.isList("biomes")) {
            condition.setBiomes(config.getStringList("biomes"));
        }
        
        // 加载高度限制
        if (config.isSet("min-height")) {
            condition.setMinHeight(config.getInt("min-height", Integer.MIN_VALUE));
        }
        if (config.isSet("max-height")) {
            condition.setMaxHeight(config.getInt("max-height", Integer.MAX_VALUE));
        }
        
        // 加载光照限制
        if (config.isSet("min-light")) {
            condition.setMinLight(config.getInt("min-light", 0));
        }
        if (config.isSet("max-light")) {
            condition.setMaxLight(config.getInt("max-light", 15));
        }
        
        // 加载天气类型限制
        if (config.isList("weather-types")) {
            condition.setWeatherTypes(config.getStringList("weather-types"));
        }
    }
    
    /**
     * 从配置中加载药水效果
     */
    protected void loadPotionEffects() {
        potionEffects = new ArrayList<>();
        if (config == null) return;
        
        ConfigurationSection potionSection = config.getConfigurationSection("potion-effects");
        if (potionSection == null) return;
        
        // 支持两种格式：列表格式和映射格式
        if (potionSection.isList("effects")) {
            // 列表格式
            for (Map<?, ?> effectMap : potionSection.getMapList("effects")) {
                String type = (String) effectMap.get("type");
                Object levelObj = effectMap.get("level");
                Object durationObj = effectMap.get("duration");
                
                // 支持范围格式 [min, max]
                int level = DynamicParameter.parseIntParameter(levelObj);
                int duration = DynamicParameter.parseIntParameter(durationObj);
                
                PotionEffectType effectType = PotionEffectType.getByName(type);
                if (effectType != null) {
                    potionEffects.add(new PotionEffect(effectType, duration, level));
                }
            }
        } else {
            // 映射格式（每个效果一个配置节）
            for (String key : potionSection.getKeys(false)) {
                if (potionSection.isConfigurationSection(key)) {
                    ConfigurationSection effectSection = potionSection.getConfigurationSection(key);
                    String type = effectSection.getString("type");
                    Object levelObj = effectSection.get("level");
                    Object durationObj = effectSection.get("duration");
                    
                    // 支持范围格式 [min, max]
                    int level = DynamicParameter.parseIntParameter(levelObj);
                    int duration = DynamicParameter.parseIntParameter(durationObj);
                    
                    PotionEffectType effectType = PotionEffectType.getByName(type);
                    if (effectType != null) {
                        potionEffects.add(new PotionEffect(effectType, duration, level));
                    }
                }
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
        
        // 加载前置条件
        if (randomSection.isConfigurationSection("prerequisites")) {
            ConfigurationSection prerequisitesSection = randomSection.getConfigurationSection("prerequisites");
            Map<String, Object> prerequisites = new HashMap<>();
            prerequisites.put("type", prerequisitesSection.getString("type"));
            prerequisites.put("effect_type", prerequisitesSection.getString("effect_type"));
            prerequisites.put("level", prerequisitesSection.getInt("level", 0));
            randomEffects.put("prerequisites", prerequisites);
        }
        
        List<Map<String, Object>> effects = new ArrayList<>();
        for (Map<?, ?> effectMap : randomSection.getMapList("effects")) {
            Map<String, Object> effect = new HashMap<>();
            effect.put("type", effectMap.get("type"));
            
            // 支持范围格式 [min, max]
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
    
    /**
     * 从配置中加载标题配置
     */
    protected void loadTitle() {
        title = new HashMap<>();
        if (config == null) return;
        
        ConfigurationSection titleSection = config.getConfigurationSection("title");
        if (titleSection == null) return;
        
        title.put("enabled", titleSection.getBoolean("enabled", true));
        title.put("text", titleSection.getString("text", ""));
        title.put("subtitle", titleSection.getString("subtitle", ""));
        title.put("color", titleSection.getString("color", "white"));
        title.put("fadeIn", titleSection.getInt("fadeIn", 10));
        title.put("stay", titleSection.getInt("stay", 70));
        title.put("fadeOut", titleSection.getInt("fadeOut", 20));
        title.put("chance", titleSection.getDouble("chance", 1.0));
    }
    
    /**
     * 从配置中加载动作栏配置
     */
    protected void loadActionBar() {
        actionBar = new HashMap<>();
        if (config == null) return;
        
        ConfigurationSection actionBarSection = config.getConfigurationSection("action-bar");
        if (actionBarSection == null) return;
        
        actionBar.put("enabled", actionBarSection.getBoolean("enabled", true));
        actionBar.put("text", actionBarSection.getString("text", ""));
        actionBar.put("color", actionBarSection.getString("color", "white"));
        actionBar.put("chance", actionBarSection.getDouble("chance", 1.0));
    }
    
    /**
     * 从配置中加载声音配置
     */
    protected void loadSound() {
        sound = new HashMap<>();
        if (config == null) return;
        
        ConfigurationSection soundSection = config.getConfigurationSection("sound");
        if (soundSection == null) return;
        
        sound.put("enabled", soundSection.getBoolean("enabled", true));
        sound.put("resource", soundSection.getString("resource", ""));
        sound.put("volume", soundSection.getDouble("volume", 1.0));
        sound.put("pitch", soundSection.getDouble("pitch", 1.0));
        sound.put("chance", soundSection.getDouble("chance", 1.0));
    }
    
    /**
     * 从配置中加载消息配置
     */
    protected void loadMessage() {
        message = new HashMap<>();
        if (config == null) return;
        
        ConfigurationSection messageSection = config.getConfigurationSection("message");
        if (messageSection == null) return;
        
        message.put("enabled", messageSection.getBoolean("enabled", true));
        message.put("text", messageSection.getString("text", ""));
        message.put("color", messageSection.getString("color", "white"));
        message.put("chance", messageSection.getDouble("chance", 1.0));
    }
    
    /**
     * 从配置中加载连锁效果
     */
    protected void loadChainEffects() {
        if (config == null) return;
        
        ConfigurationSection chainSection = config.getConfigurationSection("chain-effects");
        if (chainSection == null) return;
        
        List<Map<String, Object>> chainEffectsConfig = new ArrayList<>();
        for (String key : chainSection.getKeys(false)) {
            if (chainSection.isConfigurationSection(key)) {
                ConfigurationSection effectSection = chainSection.getConfigurationSection(key);
                Map<String, Object> chainEffectMap = new HashMap<>();
                chainEffectMap.put("chance", effectSection.getDouble("chance", 0.0));
                chainEffectMap.put("effect-id", effectSection.getString("effect-id"));
                chainEffectsConfig.add(chainEffectMap);
            }
        }
        
        this.chainEffects = ChainEffect.fromConfig(chainEffectsConfig);
    }
    
    @Override
    public void apply(Player player, World world) {
        if (!enabled || !isApplicable(world)) return;
        
        // 检查条件
        if (!checkConditions(player)) return;
        
        // 应用药水效果
        for (PotionEffect effect : getPotionEffects()) {
            player.addPotionEffect(effect);
        }
        
        // 尝试应用随机效果
        tryApplyRandomEffects(player);
        
        // 尝试执行命令
        tryExecuteCommands(player);
        
        // 尝试发送标题
        trySendTitle(player);
        
        // 尝试发送动作栏消息
        trySendActionBar(player);
        
        // 尝试播放声音
        tryPlaySound(player);
        
        // 尝试发送消息
        trySendMessage(player);
    }
    
    /**
     * 检查效果触发条件
     * @param player 玩家
     * @return 是否满足所有条件
     */
    protected boolean checkConditions(Player player) {
        // 检查生物群系条件
        if (condition.getBiomes() != null && !condition.getBiomes().isEmpty()) {
            String biomeName = player.getLocation().getBlock().getBiome().name().toLowerCase();
            boolean biomeMatch = condition.getBiomes().stream()
                    .anyMatch(b -> b.equalsIgnoreCase(biomeName));
            if (!biomeMatch) return false;
        }
        
        // 检查天气条件
        if (condition.getWeatherTypes() != null && !condition.getWeatherTypes().isEmpty()) {
            String currentWeather = getCurrentWeatherType(player.getWorld());
            if (!condition.getWeatherTypes().contains(currentWeather)) {
                return false;
            }
        }
        
        // 检查高度条件
        int y = player.getLocation().getBlockY();
        if (condition.getMinHeight() != null && y < condition.getMinHeight()) return false;
        if (condition.getMaxHeight() != null && y > condition.getMaxHeight()) return false;
        
        // 检查光照条件
        int light = player.getLocation().getBlock().getLightLevel();
        if (condition.getMinLight() != null && light < condition.getMinLight()) return false;
        if (condition.getMaxLight() != null && light > condition.getMaxLight()) return false;
        
        return true;
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
    
    @Override
    public Map<String, Object> getTitle() {
        return title;
    }
    
    @Override
    public Map<String, Object> getActionBar() {
        return actionBar;
    }
    
    @Override
    public Map<String, Object> getSound() {
        return sound;
    }
    
    @Override
    public Map<String, Object> getMessage() {
        return message;
    }
    
    // 移除 @Override
    public boolean isApplicable(World world) {
        // 检查天气条件
        if (condition.getWeatherTypes() != null && !condition.getWeatherTypes().isEmpty()) {
            String currentWeather = getCurrentWeatherType(world);
            if (!condition.getWeatherTypes().contains(currentWeather)) {
                return false;
            }
        }
        
        return true; // 默认适用于所有世界，子类可以覆盖此方法
    }
    
    /**
     * 获取当前世界的天气类型
     * @param world 目标世界
     * @return 天气类型（"clear", "rain", "thunder"）
     */
    protected String getCurrentWeatherType(World world) {
        if (world.isThundering()) {
            return "thunder";
        } else if (world.hasStorm()) {
            return "rain";
        } else {
            return "clear";
        }
    }
    
    @SuppressWarnings("unchecked") // 抑制未经检查操作警告
    protected void tryApplyRandomEffects(Player player) {
        // 实现随机效果应用逻辑
        if (randomEffects == null || randomEffects.isEmpty()) return;
        
        // 检查前置条件
        Map<String, Object> prerequisites = (Map<String, Object>) randomEffects.get("prerequisites");
        if (prerequisites != null && !prerequisites.isEmpty()) {
            String type = (String) prerequisites.get("type");
            if ("has_potion_effect".equals(type)) {
                String effectTypeName = (String) prerequisites.get("effect_type");
                int level = ((Number) prerequisites.getOrDefault("level", 0)).intValue();
                
                PotionEffectType effectType = PotionEffectType.getByName(effectTypeName);
                if (effectType == null) return;
                
                PotionEffect activeEffect = player.getPotionEffect(effectType);
                if (activeEffect == null || activeEffect.getAmplifier() != level) return;
            }
        }
        
        double chance = (double) randomEffects.getOrDefault("chance", 0.0);
        if (Math.random() > chance) return;
        
        List<Map<String, Object>> effects = (List<Map<String, Object>>) randomEffects.get("effects");
        if (effects != null && !effects.isEmpty()) {
            Map<String, Object> effect = effects.get((int) (Math.random() * effects.size()));
            PotionEffectType type = PotionEffectType.getByName((String) effect.get("type"));
            if (type != null) {
                // 处理动态参数
                Object levelObj = effect.get("level");
                Object durationObj = effect.get("duration");
                
                int level = DynamicParameter.parseIntParameter(levelObj);
                int duration = DynamicParameter.parseIntParameter(durationObj);
                
                player.addPotionEffect(new PotionEffect(type, duration, level));
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
                String processedCmd = cmd.replace("%player%", player.getName())
                        .replace("%player_name%", player.getName())
                        .replace("%player_x%", String.valueOf(player.getLocation().getX()))
                        .replace("%player_y%", String.valueOf(player.getLocation().getY()))
                        .replace("%player_z%", String.valueOf(player.getLocation().getZ()))
                        .replace("%player_health%", String.valueOf(player.getHealth()))
                        .replace("%player_food%", String.valueOf(player.getFoodLevel()));
                
                // 检查是否是title命令，如果是，则使用API直接发送
                if (processedCmd.startsWith("title ") && processedCmd.contains(" title ")) {
                    // 提取玩家名称和标题文本
                    String[] parts = processedCmd.split(" title ", 2);
                    String playerName = parts[0].substring(6).trim();
                    String titleText = parts[1];
                    
                    // 查找目标玩家
                    Player targetPlayer = plugin.getServer().getPlayerExact(playerName);
                    if (targetPlayer != null) {
                        targetPlayer.sendTitle(titleText, "", 10, 70, 20);
                    }
                } else if (processedCmd.startsWith("playsound ")) {
                    // 提取声音信息
                    String[] parts = processedCmd.split(" ");
                    if (parts.length >= 6) {
                        String soundName = parts[1];
                        String playerName = parts[2];
                        float volume = 1.0f;
                        float pitch = 1.0f;
                        
                        try {
                            if (parts.length >= 7) volume = Float.parseFloat(parts[6]);
                            if (parts.length >= 8) pitch = Float.parseFloat(parts[7]);
                        } catch (NumberFormatException ignored) {}
                        
                        // 查找目标玩家
                        Player targetPlayer = plugin.getServer().getPlayerExact(playerName);
                        if (targetPlayer != null) {
                            try {
                                Sound sound = Sound.valueOf(soundName.toUpperCase().replace(".", "_").replace(":", "_"));
                                targetPlayer.playSound(targetPlayer.getLocation(), sound, volume, pitch);
                            } catch (IllegalArgumentException e) {
                                // 如果声音名称无效，尝试使用字符串形式
                                targetPlayer.playSound(targetPlayer.getLocation(), soundName, volume, pitch);
                            }
                        }
                    }
                } else {
                    // 执行原始命令
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCmd);
                }
            }
        }
        
        // 处理连锁效果
        if (chainEffects != null && !chainEffects.isEmpty()) {
            for (ChainEffect chainEffect : chainEffects) {
                if (Math.random() <= chainEffect.getChance()) {
                    // 获取共享效果管理器
                    SharedEffectManager sharedEffectManager = plugin.getSharedEffectManager();
                    if (sharedEffectManager != null) {
                        sharedEffectManager.applySharedEffect(player, chainEffect.getEffectId());
                    }
                }
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

    /**
     * 尝试发送标题
     * @param player 玩家
     */
    protected void trySendTitle(Player player) {
        if (title == null || title.isEmpty() || !(boolean) title.getOrDefault("enabled", false)) return;
        
        double chance = (double) title.getOrDefault("chance", 1.0);
        if (Math.random() > chance) return;
        
        String titleText = (String) title.getOrDefault("text", "");
        String subtitle = (String) title.getOrDefault("subtitle", "");
        int fadeIn = ((Number) title.getOrDefault("fadeIn", 10)).intValue();
        int stay = ((Number) title.getOrDefault("stay", 70)).intValue();
        int fadeOut = ((Number) title.getOrDefault("fadeOut", 20)).intValue();
        
        // 替换占位符
        titleText = titleText.replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_health%", String.valueOf(player.getHealth()))
                .replace("%player_food%", String.valueOf(player.getFoodLevel()));
        
        subtitle = subtitle.replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_health%", String.valueOf(player.getHealth()))
                .replace("%player_food%", String.valueOf(player.getFoodLevel()));
        
        player.sendTitle(titleText, subtitle, fadeIn, stay, fadeOut);
    }
    
    /**
     * 尝试发送动作栏消息
     * @param player 玩家
     */
    protected void trySendActionBar(Player player) {
        if (actionBar == null || actionBar.isEmpty() || !(boolean) actionBar.getOrDefault("enabled", false)) return;
        
        double chance = (double) actionBar.getOrDefault("chance", 1.0);
        if (Math.random() > chance) return;
        
        String text = (String) actionBar.getOrDefault("text", "");
        
        // 替换占位符
        text = text.replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_health%", String.valueOf(player.getHealth()))
                .replace("%player_food%", String.valueOf(player.getFoodLevel()));
        
        // 使用Spigot API发送动作栏消息
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(text));
    }
    
    /**
     * 尝试播放声音
     * @param player 玩家
     */
    protected void tryPlaySound(Player player) {
        if (sound == null || sound.isEmpty() || !(boolean) sound.getOrDefault("enabled", false)) return;
        
        double chance = (double) sound.getOrDefault("chance", 1.0);
        if (Math.random() > chance) return;
        
        String resource = (String) sound.getOrDefault("resource", "entity.player.levelup");
        float volume = ((Number) sound.getOrDefault("volume", 1.0f)).floatValue();
        float pitch = ((Number) sound.getOrDefault("pitch", 1.0f)).floatValue();
        
        try {
            Sound soundEnum = Sound.valueOf(resource.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), soundEnum, volume, pitch);
        } catch (IllegalArgumentException e) {
            // 如果声音名称无效，尝试使用字符串形式
            player.playSound(player.getLocation(), resource, volume, pitch);
        }
    }
    
    /**
     * 尝试发送消息
     * @param player 玩家
     */
    protected void trySendMessage(Player player) {
        if (message == null || message.isEmpty() || !(boolean) message.getOrDefault("enabled", false)) return;
        
        double chance = (double) message.getOrDefault("chance", 1.0);
        if (Math.random() > chance) return;
        
        String text = (String) message.getOrDefault("text", "");
        
        // 替换占位符
        text = text.replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_health%", String.valueOf(player.getHealth()))
                .replace("%player_food%", String.valueOf(player.getFoodLevel()));
        
        player.sendMessage(text);
    }
    
    @Override
    public void loadFromConfig(ConfigurationSection config) {
        loadConditions();
        loadPotionEffects();
        loadRandomEffects();
        loadCommands();
        loadTitle();
        loadActionBar();
        loadSound();
        loadMessage();
        loadChainEffects();
    }
}