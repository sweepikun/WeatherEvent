package cn.popcraft.weatherevent.forecast;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * 天气预报管理器
 * 负责生成和管理天气预报
 */
public class WeatherForecastManager {
    
    private final WeatherEvent plugin;
    private final Logger logger;
    
    // 配置
    private boolean enabled;
    private int forecastDays; // 预报天数
    private int updateIntervalTicks; // 更新间隔
    private double baseAccuracy; // 基础准确度
    
    // 预报缓存
    private final Map<String, WeatherForecast> forecastCache;
    
    // 更新任务
    private BukkitTask updateTask;
    
    // 随机数生成器
    private final Random random;
    
    public WeatherForecastManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.forecastCache = new HashMap<>();
        this.random = new Random();
        
        // 默认配置
        this.enabled = false;
        this.forecastDays = 3;
        this.updateIntervalTicks = 1200; // 1分钟
        this.baseAccuracy = 0.8;
    }
    
    /**
     * 从配置中加载天气预报系统
     * @param config 配置部分
     */
    public void loadFromConfig(ConfigurationSection config) {
        // 先停止旧的更新任务
        stopUpdateTask();
        
        if (config == null) {
            logger.warning("天气预报系统配置为空，使用默认配置");
            this.enabled = false;
            return;
        }
        
        this.enabled = config.getBoolean("enabled", false);
        if (!enabled) {
            logger.info("天气预报系统已禁用");
            return;
        }
        
        // 加载配置
        this.forecastDays = Math.max(1, Math.min(14, config.getInt("forecast-days", 3)));
        this.updateIntervalTicks = Math.max(20, config.getInt("update-interval-ticks", 1200));
        this.baseAccuracy = Math.max(0.1, Math.min(1.0, config.getDouble("base-accuracy", 0.8)));
        
        // 启动更新任务
        startUpdateTask();
        
        logger.info("天气预报系统已启用，预报天数: " + forecastDays);
    }
    
    /**
     * 停止更新任务（但不清除缓存）
     */
    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    
    /**
     * 启动更新任务
     */
    private void startUpdateTask() {
        stopUpdateTask();
        
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (World world : plugin.getServer().getWorlds()) {
                updateForecast(world);
            }
        }, 0L, updateIntervalTicks);
    }
    
    /**
     * 更新世界的天气预报
     * @param world 世界
     */
    private void updateForecast(World world) {
        WeatherForecast forecast = generateForecast(world);
        forecastCache.put(world.getName(), forecast);
    }
    
    /**
     * 生成天气预报
     * @param world 世界
     * @return 天气预报
     */
    private WeatherForecast generateForecast(World world) {
        WeatherForecast forecast = new WeatherForecast(world);
        
        long currentTime = world.getFullTime();
        long ticksPerDay = 24000;
        
        // 获取当前天气
        WeatherType currentWeather = WeatherType.fromBooleans(
            world.hasStorm(), world.isThundering()
        );
        
        // 生成未来几天的预报 - 使用连续的马尔可夫链
        WeatherType previousWeather = currentWeather;
        for (int day = 1; day <= forecastDays; day++) {
            long forecastTime = currentTime + (day * ticksPerDay);
            
            // 基于前一天的天气预测下一天
            WeatherType predictedWeather = predictNextWeather(previousWeather);
            previousWeather = predictedWeather;
            
            // 计算可信度（越远越不准确）
            double confidence = Math.max(0.1, baseAccuracy * Math.pow(0.85, day - 1));
            
            // 时间描述
            String timeDescription;
            if (day == 1) {
                timeDescription = "明天";
            } else if (day == 2) {
                timeDescription = "后天";
            } else {
                timeDescription = day + " 天后";
            }
            
            ForecastEntry entry = new ForecastEntry(
                predictedWeather, forecastTime, confidence, timeDescription
            );
            
            forecast.addEntry(entry);
        }
        
        return forecast;
    }
    
    /**
     * 基于当前天气预测下一天天气（马尔可夫链）
     * @param currentWeather 当前天气
     * @return 预测的天气
     */
    private WeatherType predictNextWeather(WeatherType currentWeather) {
        double rand = random.nextDouble();
        
        // 天气转换概率矩阵 - 基于真实气象规律
        switch (currentWeather) {
            case CLEAR:
                // 晴天 -> 60%晴天, 30%雨天, 10%雷暴
                if (rand < 0.6) return WeatherType.CLEAR;
                if (rand < 0.9) return WeatherType.RAIN;
                return WeatherType.THUNDER;
                
            case RAIN:
                // 雨天 -> 35%晴天, 50%雨天, 15%雷暴
                if (rand < 0.35) return WeatherType.CLEAR;
                if (rand < 0.85) return WeatherType.RAIN;
                return WeatherType.THUNDER;
                
            case THUNDER:
                // 雷暴 -> 25%晴天, 55%雨天, 20%雷暴
                if (rand < 0.25) return WeatherType.CLEAR;
                if (rand < 0.80) return WeatherType.RAIN;
                return WeatherType.THUNDER;
                
            default:
                return WeatherType.CLEAR;
        }
    }
    
    /**
     * 获取世界的天气预报
     * @param world 世界
     * @return 天气预报
     */
    public WeatherForecast getForecast(World world) {
        return forecastCache.get(world.getName());
    }
    
    /**
     * 强制更新预报
     * @param world 世界
     * @return 更新后的预报
     */
    public WeatherForecast forceUpdate(World world) {
        updateForecast(world);
        return getForecast(world);
    }
    
    /**
     * 检查天气预报系统是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("weather-forecast");
        loadFromConfig(config);
    }
    
    /**
     * 停止更新任务
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        forecastCache.clear();
    }
}
