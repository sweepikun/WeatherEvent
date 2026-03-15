package cn.popcraft.weatherevent.api;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.disaster.DisasterType;
import cn.popcraft.weatherevent.effects.BaseWeatherEffect;
import cn.popcraft.weatherevent.forecast.WeatherForecast;
import cn.popcraft.weatherevent.season.Season;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WeatherEvent API实现类
 */
public class WeatherEventAPIImpl implements WeatherEventAPI {
    
    private final WeatherEvent plugin;
    private final List<WeatherEventListener> listeners;
    
    public WeatherEventAPIImpl(WeatherEvent plugin) {
        this.plugin = plugin;
        this.listeners = new ArrayList<>();
    }
    
    @Override
    public WeatherEvent getPlugin() {
        return plugin;
    }
    
    // ==================== 天气效果相关 ====================
    
    @Override
    public Map<String, BaseWeatherEffect> getEffects() {
        return plugin.getEffectManager().getEffects();
    }
    
    @Override
    public BaseWeatherEffect getEffect(String effectId) {
        return plugin.getEffectManager().getEffect(effectId);
    }
    
    @Override
    public boolean registerEffect(BaseWeatherEffect effect) {
        try {
            plugin.getEffectManager().registerEffect(effect);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("注册效果失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean unregisterEffect(String effectId) {
        try {
            plugin.getEffectManager().unregisterEffect(effectId);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("取消注册效果失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean applyEffect(Player player, World world, String effectId) {
        BaseWeatherEffect effect = getEffect(effectId);
        if (effect != null) {
            effect.apply(player, world);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean removeEffect(Player player, World world, String effectId) {
        BaseWeatherEffect effect = getEffect(effectId);
        if (effect != null) {
            effect.remove(player, world);
            return true;
        }
        return false;
    }
    
    // ==================== 季节系统相关 ====================
    
    @Override
    public boolean isSeasonSystemEnabled() {
        return plugin.getSeasonManager() != null && plugin.getSeasonManager().isEnabled();
    }
    
    @Override
    public Season getWorldSeason(World world) {
        if (plugin.getSeasonManager() != null) {
            return plugin.getSeasonManager().getWorldSeason(world);
        }
        return Season.SPRING; // 默认春天
    }
    
    @Override
    public void setWorldSeason(World world, Season season) {
        if (plugin.getSeasonManager() != null) {
            plugin.getSeasonManager().setWorldSeason(world, season);
        }
    }
    
    @Override
    public void applySeasonEffects(Player player, World world) {
        if (plugin.getSeasonManager() != null) {
            plugin.getSeasonManager().applySeasonEffects(world, getWorldSeason(world));
        }
    }
    
    // ==================== 灾害系统相关 ====================
    
    @Override
    public boolean isDisasterSystemEnabled() {
        return plugin.getDisasterManager() != null && plugin.getDisasterManager().isEnabled();
    }
    
    @Override
    public boolean hasActiveDisaster(World world) {
        if (plugin.getDisasterManager() != null) {
            return plugin.getDisasterManager().hasActiveDisaster(world);
        }
        return false;
    }
    
    @Override
    public boolean forceDisaster(World world, DisasterType type) {
        if (plugin.getDisasterManager() != null) {
            return plugin.getDisasterManager().forceDisaster(world, type);
        }
        return false;
    }
    
    @Override
    public boolean stopDisaster(World world) {
        if (plugin.getDisasterManager() != null) {
            return plugin.getDisasterManager().stopDisaster(world);
        }
        return false;
    }
    
    // ==================== 天气预报相关 ====================
    
    @Override
    public boolean isWeatherForecastEnabled() {
        return plugin.getWeatherForecastManager() != null && 
               plugin.getWeatherForecastManager().isEnabled();
    }
    
    @Override
    public WeatherForecast getWeatherForecast(World world) {
        if (plugin.getWeatherForecastManager() != null) {
            return plugin.getWeatherForecastManager().getForecast(world);
        }
        return null;
    }
    
    @Override
    public WeatherForecast forceUpdateForecast(World world) {
        if (plugin.getWeatherForecastManager() != null) {
            return plugin.getWeatherForecastManager().forceUpdate(world);
        }
        return null;
    }
    
    // ==================== 事件系统相关 ====================
    
    @Override
    public void registerWeatherListener(WeatherEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void unregisterWeatherListener(WeatherEventListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 触发天气变化事件
     * @param world 世界
     * @param oldWeather 旧天气
     * @param newWeather 新天气
     */
    public void fireWeatherChange(World world, String oldWeather, String newWeather) {
        for (WeatherEventListener listener : listeners) {
            try {
                listener.onWeatherChange(world, oldWeather, newWeather);
            } catch (Exception e) {
                plugin.getLogger().warning("天气事件监听器异常: " + e.getMessage());
            }
        }
    }
    
    /**
     * 触发季节变化事件
     * @param world 世界
     * @param oldSeason 旧季节
     * @param newSeason 新季节
     */
    public void fireSeasonChange(World world, Season oldSeason, Season newSeason) {
        for (WeatherEventListener listener : listeners) {
            try {
                listener.onSeasonChange(world, oldSeason, newSeason);
            } catch (Exception e) {
                plugin.getLogger().warning("季节事件监听器异常: " + e.getMessage());
            }
        }
    }
    
    /**
     * 触发灾害开始事件
     * @param world 世界
     * @param disasterType 灾害类型
     */
    public void fireDisasterStart(World world, DisasterType disasterType) {
        for (WeatherEventListener listener : listeners) {
            try {
                listener.onDisasterStart(world, disasterType);
            } catch (Exception e) {
                plugin.getLogger().warning("灾害事件监听器异常: " + e.getMessage());
            }
        }
    }
    
    /**
     * 触发灾害结束事件
     * @param world 世界
     * @param disasterType 灾害类型
     */
    public void fireDisasterEnd(World world, DisasterType disasterType) {
        for (WeatherEventListener listener : listeners) {
            try {
                listener.onDisasterEnd(world, disasterType);
            } catch (Exception e) {
                plugin.getLogger().warning("灾害事件监听器异常: " + e.getMessage());
            }
        }
    }
    
    /**
     * 触发玩家天气效果事件
     * @param player 玩家
     * @param world 世界
     * @param effectId 效果ID
     */
    public void firePlayerWeatherEffect(Player player, World world, String effectId) {
        for (WeatherEventListener listener : listeners) {
            try {
                listener.onPlayerWeatherEffect(player, world, effectId);
            } catch (Exception e) {
                plugin.getLogger().warning("玩家天气效果事件监听器异常: " + e.getMessage());
            }
        }
    }
}
