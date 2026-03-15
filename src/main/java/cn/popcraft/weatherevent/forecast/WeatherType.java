package cn.popcraft.weatherevent.forecast;

/**
 * 天气类型枚举
 */
public enum WeatherType {
    CLEAR("晴朗", "clear", "§e"),
    RAIN("下雨", "rain", "§9"),
    THUNDER("雷暴", "thunder", "§5"),
    SNOW("下雪", "snow", "§f"),
    UNKNOWN("未知", "unknown", "§7");
    
    private final String displayName;
    private final String id;
    private final String color;
    
    WeatherType(String displayName, String id, String color) {
        this.displayName = displayName;
        this.id = id;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getId() {
        return id;
    }
    
    public String getColor() {
        return color;
    }
    
    /**
     * 根据ID获取天气类型
     * @param id ID
     * @return 天气类型
     */
    public static WeatherType fromId(String id) {
        for (WeatherType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * 从布尔值获取天气类型
     * @param isRaining 是否下雨
     * @param isThundering 是否雷暴
     * @return 天气类型
     */
    public static WeatherType fromBooleans(boolean isRaining, boolean isThundering) {
        if (isThundering) {
            return THUNDER;
        } else if (isRaining) {
            return RAIN;
        } else {
            return CLEAR;
        }
    }
}
