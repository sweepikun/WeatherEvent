package cn.popcraft.weatherevent.forecast;

/**
 * 预报条目类
 * 表示一个天气预报条目
 */
public class ForecastEntry {
    
    private final WeatherType weatherType;
    private final long gameTicks; // 游戏刻数
    private final double confidence; // 可信度 (0-1)
    private final String timeDescription; // 时间描述
    
    /**
     * 创建预报条目
     * @param weatherType 天气类型
     * @param gameTicks 游戏刻数
     * @param confidence 可信度
     * @param timeDescription 时间描述
     */
    public ForecastEntry(WeatherType weatherType, long gameTicks, double confidence, 
                        String timeDescription) {
        this.weatherType = weatherType;
        this.gameTicks = gameTicks;
        this.confidence = confidence;
        this.timeDescription = timeDescription;
    }
    
    /**
     * 获取天气类型
     * @return 天气类型
     */
    public WeatherType getWeatherType() {
        return weatherType;
    }
    
    /**
     * 获取游戏刻数
     * @return 游戏刻数
     */
    public long getGameTicks() {
        return gameTicks;
    }
    
    /**
     * 获取可信度
     * @return 可信度
     */
    public double getConfidence() {
        return confidence;
    }
    
    /**
     * 获取时间描述
     * @return 时间描述
     */
    public String getTimeDescription() {
        return timeDescription;
    }
    
    /**
     * 获取天气颜色代码
     * @return 颜色代码
     */
    public String getColor() {
        return weatherType.getColor();
    }
}
