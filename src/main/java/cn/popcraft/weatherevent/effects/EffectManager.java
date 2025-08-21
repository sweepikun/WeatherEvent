package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * 效果管理器
 * 负责加载、注册和应用各种天气和时间效果
 */
public class EffectManager implements Listener {
    
    private final WeatherEvent plugin;
    private final Map<String, BaseWeatherEffect> effects; // 效果映射表
    private BukkitTask effectTask; // 效果更新任务
    private int updateInterval; // 更新间隔，单位为 tick
    
    // 用于跟踪每个世界的天气状态
    private final Map<String, WeatherState> worldWeatherStates;
    
    // 生物群系天气管理器
    private final BiomeWeatherManager biomeWeatherManager;
    
    // 内部类，用于存储世界的天气状态
    private static class WeatherState {
        boolean isRaining;
        boolean isThundering;
        
        WeatherState(boolean isRaining, boolean isThundering) {
            this.isRaining = isRaining;
            this.isThundering = isThundering;
        }
    }
    
    /**
     * 创建一个效果管理器
     * @param plugin 插件实例
     * @param biomeWeatherManager 生物群系天气管理器
     */
    public EffectManager(WeatherEvent plugin, BiomeWeatherManager biomeWeatherManager) {
        this.plugin = plugin;
        this.effects = new HashMap<>();
        this.worldWeatherStates = new HashMap<>();
        this.updateInterval = 20; // 默认每秒更新一次
        this.biomeWeatherManager = biomeWeatherManager;
    }
    
    /**
     * 从配置中加载所有效果
     */
    public void loadEffects() {
        // 清除现有效果
        unregisterAllEffects();
        
        // 获取配置
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("effects");
        if (config == null) {
            plugin.getLogger().warning("配置中未找到效果部分，使用默认效果");
            registerDefaultEffects();
            return;
        }
        
        // 设置更新间隔
        updateInterval = plugin.getConfig().getInt("update-interval", 20);
        
        // 初始化所有世界的天气状态
        initializeWorldWeatherStates();
        
        // 加载天气效果
        loadWeatherEffects(config);
        
        // 加载时间效果
        loadTimeEffects(config);
        
        // 加载生物群系天气效果
        loadBiomeWeatherEffects(config);
        
        // 启动效果更新任务
        startEffectTask();
        
        plugin.getLogger().info("已加载 " + effects.size() + " 个效果");
    }
    
    /**
     * 初始化所有世界的天气状态
     */
    private void initializeWorldWeatherStates() {
        worldWeatherStates.clear();
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            worldWeatherStates.put(worldName, new WeatherState(world.hasStorm(), world.isThundering()));
        }
        plugin.getLogger().info("已初始化 " + worldWeatherStates.size() + " 个世界的天气状态");
    }
    
    /**
     * 加载天气效果
     * @param config 配置部分
     */
    private void loadWeatherEffects(ConfigurationSection config) {
        // 雨天减速效果，期望 2 参数：Plugin, ConfigurationSection
        if (config.getBoolean("rain-slow.enabled", true)) {
            registerEffect(new RainSlowEffect(plugin, config.getConfigurationSection("rain-slow")));
        }
        
        // 晴天奖励效果，期望 2 参数：Plugin, ConfigurationSection
        if (config.getBoolean("sunny-bonus.enabled", true)) {
            registerEffect(new SunnyBonusEffect(plugin, config.getConfigurationSection("sunny-bonus")));
        }
        
        // 雷暴伤害效果，期望 2 参数：Plugin, ConfigurationSection
        if (config.getBoolean("thunder-damage.enabled", true)) {
            registerEffect(new ThunderDamageEffect(plugin, "thunder-damage", config.getConfigurationSection("thunder-damage")));
        }
        
        // 雨天效果，期望 2 参数：WeatherEvent, ConfigurationSection
        if (config.getBoolean("rain.enabled", true)) {
            registerEffect(new RainEffect(plugin, config.getConfigurationSection("rain")));
        }
        
        // 雷暴效果，期望 2 参数：WeatherEvent, ConfigurationSection
        if (config.getBoolean("thunder.enabled", true)) {
            registerEffect(new ThunderEffect(plugin, config.getConfigurationSection("thunder")));
        }
        
        // 晴天效果，基于错误消息可能期望 3 参数，但调用时可能为 2 或 null，调整为匹配
        if (config.getBoolean("clear.enabled", true)) {
            registerEffect(new ClearEffect(plugin, config.getConfigurationSection("clear")));
        }
        
        // 洞穴效果，期望 2 参数：WeatherEvent, ConfigurationSection
        if (config.getBoolean("cave.enabled", false)) {
            registerEffect(new CaveEffect(plugin, config.getConfigurationSection("cave")));
        }
        
        // 生物群系天气效果，期望 2 参数：WeatherEvent, ConfigurationSection
        if (config.getBoolean("biome-weather.enabled", false)) {
            registerEffect(new BiomeWeatherEffect(plugin, config.getConfigurationSection("biome-weather")));
        }
    }
    
    /**
     * 加载时间效果
     * @param config 配置部分
     */
    private void loadTimeEffects(ConfigurationSection config) {
        // 日出效果，期望 3 参数，但错误显示不匹配，调整为期望参数
        if (config.getBoolean("sunrise.enabled", true)) {
            registerEffect(new SunriseEffect(plugin, config.getConfigurationSection("sunrise")));
        }
        
        // 日落效果，现在期望 2 参数：WeatherEvent, ConfigurationSection
        if (config.getBoolean("sunset.enabled", true)) {
            registerEffect(new SunsetEffect(plugin, config.getConfigurationSection("sunset")));
        }
    }
    
    /**
     * 加载生物群系天气效果
     * @param config 配置部分
     */
    private void loadBiomeWeatherEffects(ConfigurationSection config) {
        // 加载生物群系天气效果
        ConfigurationSection biomeWeatherSection = config.getConfigurationSection("biome-weather");
        if (biomeWeatherSection != null) {
            biomeWeatherManager.loadFromConfig(biomeWeatherSection);
        }
        
        // 加载单独的生物群系效果
        ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
        if (biomesSection != null) {
            for (String biomeName : biomesSection.getKeys(false)) {
                ConfigurationSection biomeSection = biomesSection.getConfigurationSection(biomeName);
                if (biomeSection != null && biomeSection.getBoolean("enabled", false)) {
                    registerEffect(new BiomeEffect(plugin, biomeName, biomeSection));
                    plugin.getLogger().info("已加载生物群系效果: " + biomeName);
                }
            }
        }
        
        // 白天效果，现在期望 2 参数：WeatherEvent, ConfigurationSection
        if (config.getBoolean("day.enabled", true)) {
            registerEffect(new DayEffect(plugin, config.getConfigurationSection("day")));
        }
        
        // 夜晚效果，现在期望 2 参数：WeatherEvent, ConfigurationSection
        if (config.getBoolean("night.enabled", true)) {
            registerEffect(new NightEffect(plugin, "night", config.getConfigurationSection("night")));
        }
    }
    
    /**
     * 注册默认效果
     */
    private void registerDefaultEffects() {
        // 注册默认的天气效果，调整参数以匹配子类期望
        registerEffect(new RainSlowEffect(plugin, null));
        registerEffect(new SunnyBonusEffect(plugin, null));
        registerEffect(new ThunderDamageEffect(plugin, "thunder-damage", null));
        registerEffect(new RainEffect(plugin, null));
        registerEffect(new ThunderEffect(plugin, null));
        registerEffect(new ClearEffect(plugin, null)); // 假设期望 2 参数
        registerEffect(new CaveEffect(plugin, null)); // 添加默认的洞穴效果
        registerEffect(new BiomeWeatherEffect(plugin, null)); // 添加默认的生物群系天气效果
        
        // 注册默认的时间效果，调整参数
        registerEffect(new SunriseEffect(plugin, null));
        registerEffect(new SunsetEffect(plugin, null));
        registerEffect(new DayEffect(plugin, null));
        registerEffect(new NightEffect(plugin, "night", null));
        
        // 启动效果更新任务
        startEffectTask();
    }
    
    /**
     * 注册一个效果
     * @param effect 要注册的效果
     */
    public void registerEffect(BaseWeatherEffect effect) {
        effects.put(effect.getId(), effect);
        plugin.getLogger().info("已注册效果: " + effect.getId() + " - " + effect.getDescription());
    }
    
    /**
     * 取消注册一个效果
     * @param effectId 效果ID
     */
    public void unregisterEffect(String effectId) {
        BaseWeatherEffect effect = effects.remove(effectId);
        if (effect != null) {
            plugin.getLogger().info("已取消注册效果: " + effectId);
        }
    }
    
    /**
     * 取消注册所有效果
     */
    public void unregisterAllEffects() {
        effects.clear();
        stopEffectTask();
    }
    
    /**
     * 启动效果更新任务
     */
    private void startEffectTask() {
        // 如果任务已经在运行，先停止它
        stopEffectTask();
        
        // 创建新的定时任务，定期更新所有效果
        effectTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateEffects, 20L, updateInterval);
    }
    
    /**
     * 停止效果更新任务
     */
    private void stopEffectTask() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
            effectTask = null;
        }
    }
    
    /**
     * 更新所有效果
     */
    private void updateEffects() {
        // 对每个世界应用效果
        for (World world : Bukkit.getWorlds()) {
            // 获取适用于当前世界的效果
            List<BaseWeatherEffect> applicableEffects = new ArrayList<>();
            for (BaseWeatherEffect effect : effects.values()) {
                if (effect.isApplicable(world)) {
                    applicableEffects.add(effect);
                }
            }
            
            // 对世界中的每个玩家应用效果
            for (Player player : world.getPlayers()) {
                for (BaseWeatherEffect effect : applicableEffects) {
                    effect.apply(player, world);
                }
                
                // 应用生物群系天气效果
                if (biomeWeatherManager != null && biomeWeatherManager.isEnabled()) {
                    biomeWeatherManager.applyEffects(player, world);
                }
                
                // 执行天气指令
                executeWeatherCommands(player, world);
            }
        }
    }
    
    /**
     * 执行天气相关指令
     * @param player 目标玩家
     * @param world 当前世界
     */
    private void executeWeatherCommands(Player player, World world) {
        String worldName = world.getName();
        
        // 获取当前天气类型，优先使用缓存的状态
        WeatherState state = worldWeatherStates.computeIfAbsent(
            worldName, k -> new WeatherState(world.hasStorm(), world.isThundering())
        );
        boolean isRaining = state.isRaining;
        boolean isThundering = state.isThundering;
        
        // 获取对应天气的配置
        ConfigurationSection weatherConfig = plugin.getConfig().getConfigurationSection("effects." + 
            (isThundering ? "thunder" : isRaining ? "rain" : "clear"));
        
        if (weatherConfig == null || !weatherConfig.getBoolean("enabled", true)) {
            return;
        }
        
        // 检查指令配置
        ConfigurationSection commandsConfig = weatherConfig.getConfigurationSection("commands");
        if (commandsConfig == null || !commandsConfig.getBoolean("enabled", true)) {
            return;
        }
        
        // 检查触发几率
        double chance = commandsConfig.getDouble("chance", 0.0);
        if (chance <= 0 || new Random().nextDouble() > chance) {
            return;
        }
        
        // 执行随机指令
        List<String> commands = commandsConfig.getStringList("list");
        if (!commands.isEmpty()) {
            executeRandomCommand(commands, player);
        }
    }
    
    /**
     * 随机执行一条指令
     * @param commands 指令列表
     * @param player 目标玩家
     */
    private void executeRandomCommand(List<String> commands, Player player) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        
        Random random = new Random();
        String command = commands.get(random.nextInt(commands.size()));
        executeCommand(command, player);
    }
    
    /**
     * 执行指令，替换玩家占位符
     * @param command 指令
     * @param player 目标玩家
     */
    private void executeCommand(String command, Player player) {
        if (command == null || command.isEmpty()) {
            return;
        }
        
        String processedCommand = command.replace("%player%", player.getName());
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
    }
    
    /**
     * 获取所有注册的效果
     * @return 效果映射表
     */
    public Map<String, BaseWeatherEffect> getEffects() {
        return Collections.unmodifiableMap(effects);
    }
    
    /**
     * 获取指定ID的效果
     * @param effectId 效果ID
     * @return 效果实例，如果不存在则返回null
     */
    public BaseWeatherEffect getEffect(String effectId) {
        return effects.get(effectId);
    }
    
    /**
     * 当玩家加入服务器时
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 玩家加入时，立即应用适用的效果
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        for (BaseWeatherEffect effect : effects.values()) {
            if (effect.isApplicable(world)) {
                effect.apply(player, world);
            }
        }
    }
    
    /**
     * 当玩家退出服务器时
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家退出时，移除所有效果
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        for (BaseWeatherEffect effect : effects.values()) {
            effect.remove(player, world);
        }
    }
    
    /**
     * 当玩家切换世界时
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();
        
        // 移除旧世界的效果
        for (BaseWeatherEffect effect : effects.values()) {
            if (effect.isApplicable(fromWorld)) {
                effect.remove(player, fromWorld);
            }
        }
        
        // 应用新世界的效果
        for (BaseWeatherEffect effect : effects.values()) {
            if (effect.isApplicable(toWorld)) {
                effect.apply(player, toWorld);
            }
        }
    }
    
    /**
     * 当天气改变时
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        
        // 更新世界天气状态
        WeatherState state = worldWeatherStates.computeIfAbsent(
            worldName, k -> new WeatherState(world.hasStorm(), world.isThundering())
        );
        state.isRaining = event.toWeatherState(); // 更新为新的雨天状态
        
        // 天气改变时，更新效果
        updateEffects();
    }
    
    /**
     * 当雷暴状态改变时
     */
    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        
        // 更新世界天气状态
        WeatherState state = worldWeatherStates.computeIfAbsent(
            worldName, k -> new WeatherState(world.hasStorm(), world.isThundering())
        );
        state.isThundering = event.toThunderState(); // 更新为新的雷暴状态
        
        // 雷暴状态改变时，更新效果
        updateEffects();
    }
    
    /**
     * 当玩家移动时，检测生物群系变化
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // 只在玩家跨区块移动或者改变生物群系时处理
        if (event.getFrom().getBlock().getBiome() != event.getTo().getBlock().getBiome()) {
            Player player = event.getPlayer();
            World world = player.getWorld();
            
            // 更新玩家的生物群系相关效果
            if (biomeWeatherManager != null && biomeWeatherManager.isEnabled()) {
                biomeWeatherManager.applyEffects(player, world);
            }
            
            // 检查并应用单独的生物群系效果
            for (BaseWeatherEffect effect : effects.values()) {
                if (effect instanceof BiomeEffect) {
                    BiomeEffect biomeEffect = (BiomeEffect) effect;
                    if (biomeEffect.isApplicable(player, world)) {
                        biomeEffect.apply(player, world);
                    }
                }
            }
        }
    }
}