package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.manager.BiomeCacheManager;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 高级生物群系天气效果类
 * 支持为不同生物群系定义独特的天气类型和动态效果
 */
public class AdvancedBiomeWeatherEffect {
    
    private final WeatherEvent plugin;
    private final String biomeName;
    private final Map<String, BiomeWeatherCondition> weatherConditions;
    private boolean enabled;
    
    /**
     * 构造一个高级生物群系天气效果
     * @param plugin 插件实例
     * @param biomeName 生物群系名称
     * @param config 配置部分
     */
    public AdvancedBiomeWeatherEffect(WeatherEvent plugin, String biomeName, ConfigurationSection config) {
        this.plugin = plugin;
        this.biomeName = biomeName.toUpperCase();
        this.weatherConditions = new HashMap<>();
        this.enabled = false;
        
        loadFromConfig(config);
    }
    
    /**
     * 从配置中加载效果
     * @param config 配置部分
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) return;
        
        this.enabled = config.getBoolean("enabled", false);
        if (!enabled) return;
        
        // 加载天气条件
        ConfigurationSection weatherSection = config.getConfigurationSection("weather");
        if (weatherSection != null) {
            for (String weatherType : weatherSection.getKeys(false)) {
                ConfigurationSection weatherConfig = weatherSection.getConfigurationSection(weatherType);
                if (weatherConfig != null) {
                    BiomeWeatherCondition condition = new BiomeWeatherCondition();
                    condition.loadFromConfig(weatherConfig);
                    weatherConditions.put(weatherType.toLowerCase(), condition);
                }
            }
        }
    }
    
    /**
     * 应用效果到玩家
     * @param player 目标玩家
     * @param world 目标世界
     */
    public void apply(Player player, World world) {
        if (!enabled) return;
        
        // 检查玩家是否在目标生物群系中（使用缓存管理器）
        Biome playerBiome = plugin.getEffectManager().getBiomeCacheManager().getPlayerBiome(player);
        if (!playerBiome.name().equals(biomeName)) {
            return;
        }
        
        // 获取当前世界的天气状态
        String currentWeather = getWeatherType(world);
        String timePeriod = getTimePeriod(world);
        
        // 检查特定天气类型的效果
        checkAndApplyWeatherEffect(player, world, currentWeather);
        
        // 检查时间相关的效果
        checkAndApplyWeatherEffect(player, world, timePeriod);
        
        // 检查"any"类型的效果（适用于任何天气）
        checkAndApplyWeatherEffect(player, world, "any");
    }
    
    /**
     * 检查并应用特定天气效果
     * @param player 玩家
     * @param world 世界
     * @param weatherType 天气类型
     */
    private void checkAndApplyWeatherEffect(Player player, World world, String weatherType) {
        BiomeWeatherCondition condition = weatherConditions.get(weatherType);
        if (condition != null && condition.isEnabled()) {
            // 检查触发几率
            if (Math.random() > condition.getChance()) {
                return;
            }
            
            // 应用效果
            applyWeatherCondition(player, world, condition);
        }
    }
    
    /**
     * 应用天气条件效果
     * @param player 玩家
     * @param world 世界
     * @param condition 天气条件
     */
    private void applyWeatherCondition(Player player, World world, BiomeWeatherCondition condition) {
        // 应用药水效果
        for (PotionEffect effect : condition.getPotionEffects()) {
            player.addPotionEffect(effect, true);
        }
        
        // 执行命令
        for (String command : condition.getCommands()) {
            String processedCommand = command.replace("%player%", player.getName())
                    .replace("%player_name%", player.getName())
                    .replace("%player_x%", String.valueOf(player.getLocation().getX()))
                    .replace("%player_y%", String.valueOf(player.getLocation().getY()))
                    .replace("%player_z%", String.valueOf(player.getLocation().getZ()));
            
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
        }
        
        // 显示消息
        if (condition.getMessage() != null && Math.random() <= condition.getMessageChance()) {
            player.sendMessage(condition.getMessage());
        }
        
        // 应用伤害
        if (condition.getDamage() > 0 && Math.random() <= condition.getDamageChance()) {
            player.damage(condition.getDamage());
        }
        
        // 应用特殊效果类型
        for (SpecialEffect specialEffect : condition.getSpecialEffects()) {
            applySpecialEffect(player, world, specialEffect);
        }
    }
    
    /**
     * 应用特殊效果
     * @param player 玩家
     * @param world 世界
     * @param specialEffect 特殊效果
     */
    private void applySpecialEffect(Player player, World world, SpecialEffect specialEffect) {
        String effectType = specialEffect.getType();
        int duration = specialEffect.getDuration();
        
        switch (effectType) {
            case "sandstorm":
                // 沙尘暴效果
                applySandstormEffect(player, duration);
                break;
            case "blizzard":
                // 暴风雪效果
                applyBlizzardEffect(player, duration);
                break;
        }
    }
    
    /**
     * 应用沙尘暴效果
     * @param player 玩家
     * @param duration 持续时间
     */
    private void applySandstormEffect(Player player, int duration) {
        // 添加药水效果
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 1), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0), true);
        
        // 定期播放粒子效果
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                // 播放沙尘粒子效果
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.REDSTONE,
                    player.getLocation().add(0, 1, 0),
                    10,
                    1, 1, 1,
                    0.1,
                    new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(204, 178, 102), 1)
                );
                
                ticks += 5; // 每5个tick执行一次
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    /**
     * 应用暴风雪效果
     * @param player 玩家
     * @param duration 持续时间
     */
    private void applyBlizzardEffect(Player player, int duration) {
        // 添加药水效果
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 2), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 1), true);
        
        // 定期播放粒子效果
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                // 播放雪粒子效果
                player.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 5, 1, 1, 1, 0.1);
                player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation().add(0, 1, 0), 3, 1, 1, 1, 0.1);
                
                ticks += 5; // 每5个tick执行一次
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    /**
     * 获取当前世界的天气类型
     * @param world 世界
     * @return 天气类型
     */
    private String getWeatherType(World world) {
        if (world.isThundering()) {
            return "thunder";
        } else if (world.hasStorm()) {
            return "rain";
        } else {
            return "clear";
        }
    }
    
    /**
     * 获取当前时间周期
     * @param world 世界
     * @return 时间周期
     */
    private String getTimePeriod(World world) {
        long time = world.getTime();
        if (time >= 0 && time < 1000) {
            return "sunrise";
        } else if (time >= 1000 && time < 12000) {
            return "day";
        } else if (time >= 12000 && time < 13000) {
            return "sunset";
        } else {
            return "night";
        }
    }
    
    /**
     * 检查效果是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取生物群系名称
     * @return 生物群系名称
     */
    public String getBiomeName() {
        return biomeName;
    }
    
    /**
     * 天气条件内部类
     */
    private static class BiomeWeatherCondition {
        private boolean enabled;
        private double chance;
        private List<PotionEffect> potionEffects;
        private List<String> commands;
        private String message;
        private double messageChance;
        private double damage;
        private double damageChance;
        private List<SpecialEffect> specialEffects;
        
        public BiomeWeatherCondition() {
            this.enabled = false;
            this.chance = 1.0;
            this.potionEffects = new ArrayList<>();
            this.commands = new ArrayList<>();
            this.message = null;
            this.messageChance = 1.0;
            this.damage = 0;
            this.damageChance = 0;
            this.specialEffects = new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        public void loadFromConfig(ConfigurationSection config) {
            this.enabled = config.getBoolean("enabled", false);
            this.chance = config.getDouble("chance", 1.0);
            
            // 加载药水效果
            if (config.isList("potion-effects")) {
                List<Map<String, Object>> potionEffectsList = (List<Map<String, Object>>) (List<?>) config.getMapList("potion-effects");
                for (Map<String, Object> effectMap : potionEffectsList) {
                    String type = (String) effectMap.get("type");
                    int level = ((Number) effectMap.getOrDefault("level", 0)).intValue();
                    int duration = ((Number) effectMap.getOrDefault("duration", 200)).intValue();
                    
                    PotionEffectType effectType = PotionEffectType.getByName(type);
                    if (effectType != null) {
                        potionEffects.add(new PotionEffect(effectType, duration, level));
                    }
                }
            }
            
            // 加载命令
            ConfigurationSection commandsSection = config.getConfigurationSection("commands");
            if (commandsSection != null) {
                this.commands = commandsSection.getStringList("list");
            }
            
            // 加载消息
            ConfigurationSection messageSection = config.getConfigurationSection("message");
            if (messageSection != null && messageSection.getBoolean("enabled", false)) {
                this.message = messageSection.getString("text");
                this.messageChance = messageSection.getDouble("chance", 1.0);
            }
            
            // 加载伤害
            ConfigurationSection damageSection = config.getConfigurationSection("damage");
            if (damageSection != null && damageSection.getBoolean("enabled", false)) {
                this.damage = damageSection.getDouble("amount", 1.0);
                this.damageChance = damageSection.getDouble("chance", 1.0);
            }
            
            // 加载特殊效果
            if (config.isList("effects")) {
                List<Map<String, Object>> effectsList = (List<Map<String, Object>>) (List<?>) config.getMapList("effects");
                for (Map<String, Object> effectMap : effectsList) {
                    String type = (String) effectMap.get("type");
                    int duration = 200; // 默认持续时间
                    
                    if (effectMap.containsKey("duration")) {
                        Object durationObj = effectMap.get("duration");
                        if (durationObj instanceof List) {
                            List<?> durationList = (List<?>) durationObj;
                            if (durationList.size() >= 2) {
                                int min = ((Number) durationList.get(0)).intValue();
                                int max = ((Number) durationList.get(1)).intValue();
                                duration = min + (int) (Math.random() * (max - min + 1));
                            }
                        } else if (durationObj instanceof Number) {
                            duration = ((Number) durationObj).intValue();
                        }
                    }
                    
                    specialEffects.add(new SpecialEffect(type, duration));
                }
            }
        }
        
        // Getters
        public boolean isEnabled() { return enabled; }
        public double getChance() { return chance; }
        public List<PotionEffect> getPotionEffects() { return potionEffects; }
        public List<String> getCommands() { return commands; }
        public String getMessage() { return message; }
        public double getMessageChance() { return messageChance; }
        public double getDamage() { return damage; }
        public double getDamageChance() { return damageChance; }
        public List<SpecialEffect> getSpecialEffects() { return specialEffects; }
    }
    
    /**
     * 特殊效果内部类
     */
    private static class SpecialEffect {
        private final String type;
        private final int duration;
        
        public SpecialEffect(String type, int duration) {
            this.type = type;
            this.duration = duration;
        }
        
        public String getType() { return type; }
        public int getDuration() { return duration; }
    }
}