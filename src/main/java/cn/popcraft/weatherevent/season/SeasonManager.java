package cn.popcraft.weatherevent.season;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.LocalDate;
import java.time.Month;
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
    private boolean useRealTime;
    private int realTimeMonthOffset; // 真实时间月份偏移
    
    // 当前季节（每个世界独立）
    private final Map<String, Season> worldSeasons;
    
    // 季节效果配置
    private final Map<Season, SeasonConfig> seasonConfigs;
    
    // 游戏内时间追踪
    private final Map<String, Long> worldDayCounters;
    
    public SeasonManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.worldSeasons = new HashMap<>();
        this.seasonConfigs = new HashMap<>();
        this.worldDayCounters = new HashMap<>();
        
        // 默认配置
        this.enabled = false;
        this.mode = SeasonMode.REAL_TIME;
        this.seasonDurationDays = 30;
        this.useRealTime = true;
        this.realTimeMonthOffset = 0;
    }
    
    /**
     * 从配置中加载季节系统
     * @param config 配置部分
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) {
            logger.warning("季节系统配置为空，使用默认配置");
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
        this.seasonDurationDays = config.getInt("season-duration-days", 30);
        this.useRealTime = config.getBoolean("use-real-time", true);
        this.realTimeMonthOffset = config.getInt("real-time-month-offset", 0);
        
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
        
        for (World world : Bukkit.getWorlds()) {
            updateWorldSeason(world);
            worldDayCounters.put(world.getName(), 0L);
        }
        
        logger.info("已初始化 " + worldSeasons.size() + " 个世界的季节");
    }
    
    /**
     * 更新世界的季节
     * @param world 世界
     */
    private void updateWorldSeason(World world) {
        String worldName = world.getName();
        Season newSeason;
        
        if (mode == SeasonMode.REAL_TIME) {
            // 真实时间模式：根据当前月份计算季节
            LocalDate now = LocalDate.now();
            int month = now.getMonthValue();
            // 应用月份偏移
            month = ((month - 1 + realTimeMonthOffset) % 12) + 1;
            newSeason = Season.fromMonth(month);
        } else {
            // 游戏内时间模式：根据游戏天数计算季节
            long dayCounter = worldDayCounters.getOrDefault(worldName, 0L);
            int seasonIndex = (int) ((dayCounter / seasonDurationDays) % 4);
            newSeason = Season.values()[seasonIndex];
        }
        
        Season oldSeason = worldSeasons.put(worldName, newSeason);
        
        // 如果季节变化，触发事件
        if (oldSeason != newSeason && oldSeason != null) {
            onSeasonChange(world, oldSeason, newSeason);
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
        String message = "§6[天气系统] " + world.getName() + " 进入了 " + 
                        newSeason.getColor() + newSeason.getDisplayName() + "§6！";
        
        if (plugin.getConfig().getBoolean("seasons.broadcast-season-change", true)) {
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
        }
        
        // 应用季节效果
        applySeasonEffects(world, newSeason);
        
        // 兼容其他插件：触发自定义事件
        SeasonChangeEvent event = new SeasonChangeEvent(world, oldSeason, newSeason);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    /**
     * 启动季节更新任务
     */
    private void startSeasonUpdateTask() {
        // 每秒检查一次季节变化
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
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
            }
        }, 20L, 20L); // 每秒更新一次
    }
    
    /**
     * 应用季节效果
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
