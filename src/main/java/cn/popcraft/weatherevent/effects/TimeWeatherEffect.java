package cn.popcraft.weatherevent.effects;

import org.bukkit.World;

/**
 * 时间相关的天气效果接口
 * 扩展了基本的天气效果接口，添加了时间检查功能
 */
public interface TimeWeatherEffect extends WeatherEffect {
    
    /**
     * 检查当前世界时间是否在效果的适用时间范围内
     * @param world 要检查的世界
     * @return 如果当前时间在效果的适用范围内，则返回true
     */
    boolean isTimeApplicable(World world);
    
    /**
     * 获取效果的时间范围
     * @return 时间范围数组，格式为[开始时间1, 开始时间2, 结束时间1, 结束时间2]
     */
    long[] getTimeRange();
}