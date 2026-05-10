package cn.popcraft.weatherevent.season;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.api.WeatherEventAPIImpl;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 季节管理器
 * 负责管理季节系统，支持真实时间和游戏内时间两种模式
 */
public class SeasonManager implements Listener {
    
    private final WeatherEvent plugin;
    private final Logger logger;
    
    // 季节配置
    private boolean enabled;
    private SeasonMode mode;
    private int seasonDurationDays; // 游戏内时间模式下，每个季节的天数
    private int realTimeMonthOffset; // 真实时间月份偏移
    private boolean broadcastSeasonChange; // 是否广播季节变化
    
    // 当前季节（每个世界独立）
    private final Map<String, Season> worldSeasons;
    
    // 季节效果配置
    private final Map<Season, SeasonConfig> seasonConfigs;
    
    // 游戏内时间追踪
    private final Map<String, Long> worldDayCounters;
    
    // 玩家上次应用药水效果的时间（防止频繁刷新）
    private final Map<String, Long> playerLastEffectTime;
    
    // 更新任务
    private BukkitTask updateTask;
    
    // 应用效果的最小间隔（毫秒）
    private static final long MIN_EFFECT_INTERVAL_MS = 30000; // 30秒
    
    public SeasonManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.worldSeasons = new HashMap<>();
        this.seasonConfigs = new HashMap<>();
        this.worldDayCounters = new HashMap<>();
        this.playerLastEffectTime = new HashMap<>();
        
        // 默认配置
        this.enabled = false;
        this.mode = SeasonMode.REAL_TIME;
        this.seasonDurationDays = 30;
        this.realTimeMonthOffset = 0;
        this.broadcastSeasonChange = true;
    }
    
    /**
     * 从配置中加载季节系统
     * @param config 配置部分
     */
    public void loadFromConfig(ConfigurationSection config) {
        // 先停止旧的更新任务
        stopUpdateTask();
        
        if (config == null) {
            logger.warning("季节系统配置为空，使用默认配置");
            this.enabled = false;
            return;
        }
        
        this.enabled = config.getBoolean("enabled", false);
        if (!enabled) {
            logger.info("季节系统已禁用");
            return;
        }
        
        // 加载模式
        String modeStr = config.getString("mode", "real_time");
        this.mode = SeasonMode.fromString(modeStr);
        
        // 加载其他配置
        this.seasonDurationDays = Math.max(1, config.getInt("season-duration-days", 30));
        this.realTimeMonthOffset = config.getInt("real-time-month-offset", 0);
        this.broadcastSeasonChange = config.getBoolean("broadcast-season-change", true);
        
        // 加载季节效果配置
        loadSeasonConfigs(config.getConfigurationSection("effects"));
        
        // 初始化所有世界的季节
        initializeWorldSeasons();
        
        // 启动季节更新任务
        startSeasonUpdateTask();
        
        logger.info("季节系统已启用，模式: " + mode.getDisplayName());
    }
    
    /**
     * 加载季节效果配置
     * @param effectsConfig 效果配置部分
     */
    private void loadSeasonConfigs(ConfigurationSection effectsConfig) {
        seasonConfigs.clear();
        
        if (effectsConfig == null) {
            logger.warning("季节效果配置为空，使用默认效果");
            return;
        }
        
        for (Season season : Season.values()) {
            ConfigurationSection seasonSection = effectsConfig.getConfigurationSection(season.getId());
            if (seasonSection != null) {
                SeasonConfig config = SeasonConfig.fromConfig(seasonSection, season);
                seasonConfigs.put(season, config);
                logger.info("已加载 " + season.getDisplayName() + " 的效果配置");
            }
        }
    }
    
    /**
     * 初始化所有世界的季节
     */
    private void initializeWorldSeasons() {
        worldSeasons.clear();
        worldDayCounters.clear();
        playerLastEffectTime.clear();
        
        for (World world : Bukkit.getWorlds()) {
            // 初始化天数计数器
            if (mode == SeasonMode.GAME_TIME) {
                worldDayCounters.put(world.getName(), world.getFullTime() / 24000);
            } else {
                worldDayCounters.put(world.getName(), 0L);
            }
            
            // 计算并设置初始季节（不触发变化事件）
            Season season = calculateSeason(world);
            worldSeasons.put(world.getName(), season);
        }
        
        logger.info("已初始化 " + worldSeasons.size() + " 个世界的季节");
    }
    
    /**
     * 计算世界的当前季节（不触发事件）
     * @param world 世界
     * @return 季节
     */
    private Season calculateSeason(World world) {
        if (mode == SeasonMode.REAL_TIME) {
            // 真实时间模式：根据当前月份计算季节
            LocalDate now = LocalDate.now();
            int month = now.getMonthValue();
            // 应用月份偏移
            month = ((month - 1 + realTimeMonthOffset) % 12 + 12) % 12 + 1;
            return Season.fromMonth(month);
        } else {
            // 游戏内时间模式：根据游戏天数计算季节
            long dayCounter = worldDayCounters.getOrDefault(world.getName(), 0L);
            int seasonIndex = (int) ((dayCounter / seasonDurationDays) % 4);
            if (seasonIndex < 0) seasonIndex += 4;
            return Season.values()[seasonIndex];
        }
    }
    
    /**
     * 更新世界的季节
     * @param world 世界
     */
    private void updateWorldSeason(World world) {
        String worldName = world.getName();
        Season newSeason = calculateSeason(world);
        Season oldSeason = worldSeasons.get(worldName);
        
        // 只有季节真正变化时才更新和触发事件
        if (oldSeason != newSeason) {
            worldSeasons.put(worldName, newSeason);
            if (oldSeason != null) {
                onSeasonChange(world, oldSeason, newSeason);
            }
        }
    }
    
    /**
     * 季节变化时的处理
     * @param world 世界
     * @param oldSeason 旧季节
     * @param newSeason 新季节
     */
    private void onSeasonChange(World world, Season oldSeason, Season newSeason) {
        logger.info(world.getName() + " 的季节从 " + oldSeason.getDisplayName() + 
                   " 变为 " + newSeason.getDisplayName());
        
        // 广播季节变化消息
        if (broadcastSeasonChange) {
            String message = "§6[天气系统] " + world.getName() + " 进入了 " + 
                            newSeason.getColor() + newSeason.getDisplayName() + "§6！";
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
        }
        
        // 应用季节效果（清除上次效果时间记录，让玩家立即收到效果）
        for (Player player : world.getPlayers()) {
            playerLastEffectTime.remove(player.getUniqueId().toString());
        }
        applySeasonEffects(world, newSeason);
        
        // 兼容其他插件：触发Bukkit自定义事件
        SeasonChangeEvent event = new SeasonChangeEvent(world, oldSeason, newSeason);
        Bukkit.getPluginManager().callEvent(event);
        
        // 触发API事件
        if (plugin.getAPI() instanceof WeatherEventAPIImpl) {
            ((WeatherEventAPIImpl) plugin.getAPI()).fireSeasonChange(world, oldSeason, newSeason);
        }
    }
    
    /**
     * 启动季节更新任务
     */
    private void startSeasonUpdateTask() {
        stopUpdateTask();
        
        // 每分钟检查一次季节变化（季节变化是缓慢的）
        // 同时定期应用季节效果给玩家（带间隔限制防止重复应用）
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                // 更新游戏内天数计数器
                if (mode == SeasonMode.GAME_TIME) {
                    String worldName = world.getName();
                    long currentTime = world.getFullTime();
                    long dayCount = currentTime / 24000; // Minecraft一天=24000ticks
                    worldDayCounters.put(worldName, dayCount);
                }
                
                // 更新世界季节
                updateWorldSeason(world);
                
                // 定期应用季节效果（避免药水效果过期）
                Season season = worldSeasons.get(world.getName());
                if (season != null) {
                    applySeasonEffectsWithInterval(world, season);
                }
            }
        }, 100L, 1200L); // 每分钟更新一次
    }
    
    /**
     * 停止更新任务
     */
    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    
    /**
     * 应用季节效果（不限制间隔）
     * @param world 世界
     * @param season 季节
     */
    public void applySeasonEffects(World world, Season season) {
        SeasonConfig config = seasonConfigs.get(season);
        if (config == null || !config.isEnabled()) {
            return;
        }
        
        // 对世界中的每个玩家应用效果
        for (Player player : world.getPlayers()) {
            config.applyEffects(player, world);
            playerLastEffectTime.put(player.getUniqueId().toString(), System.currentTimeMillis());
        }
    }
    
    /**
     * 带间隔限制的季节效果应用
     * @param world 世界
     * @param season 季节
     */
    private void applySeasonEffectsWithInterval(World world, Season season) {
        SeasonConfig config = seasonConfigs.get(season);
        if (config == null || !config.isEnabled()) {
            return;
        }
        
        long now = System.currentTimeMillis();
        
        for (Player player : world.getPlayers()) {
            String playerId = player.getUniqueId().toString();
            Long lastTime = playerLastEffectTime.get(playerId);
            
            if (lastTime == null || (now - lastTime) >= MIN_EFFECT_INTERVAL_MS) {
                config.applyEffects(player, world);
                playerLastEffectTime.put(playerId, now);
            }
        }
    }
    
    /**
     * 获取世界的当前季节
     * @param world 世界
     * @return 当前季节
     */
    public Season getWorldSeason(World world) {
        return worldSeasons.getOrDefault(world.getName(), Season.SPRING);
    }
    
    /**
     * 设置世界的季节
     * @param world 世界
     * @param season 季节
     */
    public void setWorldSeason(World world, Season season) {
        Season oldSeason = worldSeasons.get(world.getName());
        worldSeasons.put(world.getName(), season);
        
        if (oldSeason != season) {
            onSeasonChange(world, oldSeason != null ? oldSeason : Season.SPRING, season);
        }
    }
    
    /**
     * 检查季节系统是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取季节模式
     * @return 季节模式
     */
    public SeasonMode getMode() {
        return mode;
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("seasons");
        loadFromConfig(config);
    }
    
    /**
     * 玩家加入时应用季节效果
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        World world = player.getWorld();
        Season season = getWorldSeason(world);
        
        // 应用当前季节的效果
        SeasonConfig config = seasonConfigs.get(season);
        if (config != null && config.isEnabled()) {
            config.applyEffects(player, world);
        }
    }
    
    /**
     * 季节模式枚举
     */
    public enum SeasonMode {
        REAL_TIME("真实时间"),
        GAME_TIME("游戏内时间");
        
        private final String displayName;
        
        SeasonMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static SeasonMode fromString(String str) {
            for (SeasonMode mode : values()) {
                if (mode.name().equalsIgnoreCase(str) || 
                    mode.displayName.equals(str)) {
                    return mode;
                }
            }
            return REAL_TIME;
        }
    }
}
