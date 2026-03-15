package cn.popcraft.weatherevent.disaster;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.*;
import java.util.logging.Logger;

/**
 * 灾害管理器
 * 负责管理天气灾害事件
 */
public class DisasterManager implements Listener {
    
    private final WeatherEvent plugin;
    private final Logger logger;
    
    // 灾害配置
    private boolean enabled;
    private double globalChance; // 全局触发几率
    private int minPlayersOnline; // 最少在线玩家数
    private boolean announceDisasters; // 是否广播灾害
    
    // 灾害配置映射
    private final Map<DisasterType, DisasterConfig> disasterConfigs;
    
    // 活跃灾害
    private final Map<String, ActiveDisaster> activeDisasters;
    
    // 灾害冷却
    private final Map<String, Long> disasterCooldowns;
    
    public DisasterManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.disasterConfigs = new HashMap<>();
        this.activeDisasters = new HashMap<>();
        this.disasterCooldowns = new HashMap<>();
        
        // 默认配置
        this.enabled = false;
        this.globalChance = 0.1;
        this.minPlayersOnline = 1;
        this.announceDisasters = true;
    }
    
    /**
     * 从配置中加载灾害系统
     * @param config 配置部分
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) {
            logger.warning("灾害系统配置为空，使用默认配置");
            return;
        }
        
        this.enabled = config.getBoolean("enabled", false);
        if (!enabled) {
            logger.info("灾害系统已禁用");
            return;
        }
        
        // 加载全局配置
        this.globalChance = config.getDouble("global-chance", 0.1);
        this.minPlayersOnline = config.getInt("min-players-online", 1);
        this.announceDisasters = config.getBoolean("announce-disasters", true);
        
        // 加载灾害配置
        loadDisasterConfigs(config.getConfigurationSection("disasters"));
        
        // 启动灾害更新任务
        startDisasterUpdateTask();
        
        logger.info("灾害系统已启用，共加载 " + disasterConfigs.size() + " 种灾害");
    }
    
    /**
     * 加载灾害配置
     * @param disastersConfig 灾害配置部分
     */
    private void loadDisasterConfigs(ConfigurationSection disastersConfig) {
        disasterConfigs.clear();
        
        if (disastersConfig == null) {
            logger.warning("灾害配置为空，使用默认灾害");
            return;
        }
        
        for (DisasterType type : DisasterType.values()) {
            ConfigurationSection typeSection = disastersConfig.getConfigurationSection(type.getId());
            if (typeSection != null) {
                DisasterConfig config = DisasterConfig.fromConfig(typeSection, type);
                disasterConfigs.put(type, config);
                logger.info("已加载 " + type.getDisplayName() + " 的配置");
            }
        }
    }
    
    /**
     * 启动灾害更新任务
     */
    private void startDisasterUpdateTask() {
        // 每秒检查一次灾害
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!enabled) return;
            
            // 检查活跃灾害
            updateActiveDisasters();
            
            // 尝试触发新灾害
            if (Bukkit.getOnlinePlayers().size() >= minPlayersOnline) {
                tryTriggerDisasters();
            }
        }, 20L, 20L); // 每秒更新一次
    }
    
    /**
     * 更新活跃灾害
     */
    private void updateActiveDisasters() {
        Iterator<Map.Entry<String, ActiveDisaster>> iterator = activeDisasters.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, ActiveDisaster> entry = iterator.next();
            ActiveDisaster disaster = entry.getValue();
            
            // 更新灾害
            disaster.update();
            
            // 检查是否结束
            if (disaster.isEnded()) {
                // 应用结束效果
                disaster.applyEndEffects();
                
                // 移除灾害
                iterator.remove();
                
                logger.info(disaster.getType().getDisplayName() + " 在 " + 
                          disaster.getWorld().getName() + " 结束");
            }
        }
    }
    
    /**
     * 尝试触发灾害
     */
    private void tryTriggerDisasters() {
        for (World world : Bukkit.getWorlds()) {
            // 检查世界是否已存在灾害
            if (hasActiveDisaster(world)) {
                continue;
            }
            
            // 检查世界冷却
            if (isOnCooldown(world)) {
                continue;
            }
            
            // 尝试触发每种灾害
            for (Map.Entry<DisasterType, DisasterConfig> entry : disasterConfigs.entrySet()) {
                DisasterType type = entry.getKey();
                DisasterConfig config = entry.getValue();
                
                if (!config.isEnabled()) {
                    continue;
                }
                
                // 检查触发条件
                if (canTriggerDisaster(world, type, config)) {
                    // 检查几率
                    double chance = config.getChance() * globalChance;
                    if (Math.random() < chance) {
                        triggerDisaster(world, type, config);
                        break; // 一次只触发一种灾害
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否可以触发灾害
     * @param world 世界
     * @param type 灾害类型
     * @param config 灾害配置
     * @return 是否可以触发
     */
    private boolean canTriggerDisaster(World world, DisasterType type, DisasterConfig config) {
        // 检查天气条件
        String weatherType = getCurrentWeatherType(world);
        if (!config.getAllowedWeathers().contains(weatherType)) {
            return false;
        }
        
        // 检查生物群系条件
        if (!config.getAllowedBiomes().isEmpty()) {
            // 这里可以添加生物群系检查逻辑
        }
        
        // 检查时间条件
        if (config.isNightOnly() && !isNight(world)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 触发灾害
     * @param world 世界
     * @param type 灾害类型
     * @param config 灾害配置
     */
    private void triggerDisaster(World world, DisasterType type, DisasterConfig config) {
        // 选择中心位置（随机玩家附近）
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) return;
        
        Player targetPlayer = players.get(new Random().nextInt(players.size()));
        Location center = targetPlayer.getLocation();
        
        // 创建活跃灾害
        ActiveDisaster disaster = new ActiveDisaster(type, config, world, center, plugin);
        activeDisasters.put(world.getName(), disaster);
        
        // 设置冷却
        setCooldown(world, config.getCooldownSeconds());
        
        // 广播灾害
        if (announceDisasters) {
            String message = "§c[灾害预警] " + type.getColor() + type.getDisplayName() + 
                           "§c 正在 " + world.getName() + " 发生！请小心！";
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
        }
        
        logger.info(type.getDisplayName() + " 在 " + world.getName() + " 触发");
    }
    
    /**
     * 获取当前天气类型
     * @param world 世界
     * @return 天气类型
     */
    private String getCurrentWeatherType(World world) {
        if (world.isThundering()) {
            return "thunder";
        } else if (world.hasStorm()) {
            return "rain";
        } else {
            return "clear";
        }
    }
    
    /**
     * 检查是否是夜晚
     * @param world 世界
     * @return 是否是夜晚
     */
    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }
    
    /**
     * 检查世界是否有活跃灾害
     * @param world 世界
     * @return 是否有活跃灾害
     */
    public boolean hasActiveDisaster(World world) {
        return activeDisasters.containsKey(world.getName());
    }
    
    /**
     * 检查世界是否在冷却中
     * @param world 世界
     * @return 是否在冷却中
     */
    private boolean isOnCooldown(World world) {
        Long cooldownEnd = disasterCooldowns.get(world.getName());
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * 设置世界冷却
     * @param world 世界
     * @param seconds 冷却秒数
     */
    private void setCooldown(World world, int seconds) {
        disasterCooldowns.put(world.getName(), 
            System.currentTimeMillis() + (seconds * 1000L));
    }
    
    /**
     * 天气变化时检查灾害
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!enabled) return;
        
        World world = event.getWorld();
        
        // 如果天气转为暴风雨，增加灾害几率
        if (event.toWeatherState()) {
            // 这里可以添加额外的灾害触发逻辑
        }
    }
    
    /**
     * 获取世界的活跃灾害
     * @param world 世界
     * @return 活跃灾害，如果没有返回null
     */
    public ActiveDisaster getActiveDisaster(World world) {
        return activeDisasters.get(world.getName());
    }
    
    /**
     * 强制触发灾害
     * @param world 世界
     * @param type 灾害类型
     * @return 是否成功触发
     */
    public boolean forceDisaster(World world, DisasterType type) {
        DisasterConfig config = disasterConfigs.get(type);
        if (config == null) {
            return false;
        }
        
        triggerDisaster(world, type, config);
        return true;
    }
    
    /**
     * 停止世界的灾害
     * @param world 世界
     * @return 是否成功停止
     */
    public boolean stopDisaster(World world) {
        ActiveDisaster disaster = activeDisasters.remove(world.getName());
        if (disaster != null) {
            disaster.applyEndEffects();
            return true;
        }
        return false;
    }
    
    /**
     * 检查灾害系统是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("disasters");
        loadFromConfig(config);
    }
}
