package cn.popcraft.weatherevent.disaster;

/**
 * 灾害类型枚举
 * 定义各种天气灾害
 */
public enum DisasterType {
    TORNADO("龙卷风", "tornado", "§c", 1.0),
    FLOOD("洪水", "flood", "§9", 0.8),
    HAILSTORM("冰雹", "hailstorm", "§f", 0.6),
    SANDSTORM("沙尘暴", "sandstorm", "§e", 0.7),
    BLIZZARD("暴风雪", "blizzard", "§b", 0.9),
    THUNDERSTORM("雷暴", "thunderstorm", "§5", 1.0),
    HEATWAVE("热浪", "heatwave", "§6", 0.5),
    FROST("霜冻", "frost", "§3", 0.4);
    
    private final String displayName;
    private final String id;
    private final String color;
    private final double severity; // 严重程度 (0-1)
    
    DisasterType(String displayName, String id, String color, double severity) {
        this.displayName = displayName;
        this.id = id;
        this.color = color;
        this.severity = severity;
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
    
    public double getSeverity() {
        return severity;
    }
    
    /**
     * 根据ID获取灾害类型
     * @param id ID
     * @return 灾害类型，如果不存在返回null
     */
    public static DisasterType fromId(String id) {
        for (DisasterType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
