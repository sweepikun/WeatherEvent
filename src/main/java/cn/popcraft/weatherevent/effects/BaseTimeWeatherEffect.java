package cn.popcraft.weatherevent.effects;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.potion.PotionEffect; // 导入 PotionEffect 类，用于 getPotionEffects() 方法
import java.util.ArrayList; // 导入 ArrayList 用于创建空列表
import java.util.List; // 导入 List 接口

/**
 * 基础时间相关天气效果实现类
 * 提供了时间相关天气效果的通用实现
 */
public abstract class BaseTimeWeatherEffect extends BaseWeatherEffect implements TimeWeatherEffect {
    
    protected long[] timeRange;
    
    /**
     * 构造一个基础时间相关天气效果
     * @param plugin 插件实例
     * @param effectName 效果名称
     * @param config 效果配置
     */
    public BaseTimeWeatherEffect(Plugin plugin, String effectName, ConfigurationSection config) {
        super(plugin, effectName, config);
        loadTimeRange();
    }
    
    /**
     * 从配置中加载时间范围
     */
    protected void loadTimeRange() {
        if (config == null) {
            timeRange = new long[]{0, 24000}; // 默认为全天
            return;
        }
        
        List<Integer> range = config.getIntegerList("time-range");
        if (range == null || range.isEmpty()) {
            timeRange = new long[]{0, 24000}; // 默认为全天
            return;
        }
        
        // 处理不同格式的时间范围
        if (range.size() == 2) {
            // 简单格式：[开始, 结束]
            timeRange = new long[]{range.get(0), range.get(1)};
        } else if (range.size() >= 4) {
            // 复杂格式：[开始1, 开始2, 结束1, 结束2]
            timeRange = new long[]{range.get(0), range.get(1), range.get(2), range.get(3)};
        } else {
            timeRange = new long[]{0, 24000}; // 默认为全天
        }
    }
    
    @Override
    public boolean isTimeApplicable(World world) {
        long time = world.getTime();
        
        if (timeRange.length == 2) {
            // 简单时间范围检查
            return time >= timeRange[0] && time <= timeRange[1];
        } else if (timeRange.length >= 4) {
            // 复杂时间范围检查（支持跨越午夜的时间段）
            return (time >= timeRange[0] && time <= timeRange[1]) || 
                   (time >= timeRange[2] && time <= timeRange[3]);
        }
        
        return true; // 默认适用
    }
    
    @Override
    public boolean isApplicable(World world) {
        return isTimeApplicable(world);
    }
    
    @Override
    public long[] getTimeRange() {
        return timeRange;
    }
    
    // 新添加的方法：修复返回类型不兼容的错误
    // 假设 WeatherEffect 或 BaseWeatherEffect 定义了 getPotionEffects() 返回 List<PotionEffect>
    // 这里提供一个默认实现，返回空列表，以确保兼容性
    @Override
    public List<PotionEffect> getPotionEffects() {
        // 默认返回空列表，避免 NullPointerException
        // 在子类中可以覆写此方法以提供具体实现
        return new ArrayList<>(); 
    }
}
