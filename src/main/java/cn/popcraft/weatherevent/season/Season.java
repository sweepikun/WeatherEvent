package cn.popcraft.weatherevent.season;

/**
 * 季节枚举
 * 定义四季及其属性
 */
public enum Season {
    SPRING("春天", "spring", "§a", 0),
    SUMMER("夏天", "summer", "§e", 1),
    AUTUMN("秋天", "autumn", "§6", 2),
    WINTER("冬天", "winter", "§b", 3);
    
    private final String displayName;
    private final String id;
    private final String color;
    private final int index;
    
    Season(String displayName, String id, String color, int index) {
        this.displayName = displayName;
        this.id = id;
        this.color = color;
        this.index = index;
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
    
    public int getIndex() {
        return index;
    }
    
    /**
     * 根据月份获取季节
     * @param month 月份 (1-12)
     * @return 对应的季节
     */
    public static Season fromMonth(int month) {
        if (month >= 3 && month <= 5) {
            return SPRING;
        } else if (month >= 6 && month <= 8) {
            return SUMMER;
        } else if (month >= 9 && month <= 11) {
            return AUTUMN;
        } else {
            return WINTER;
        }
    }
    
    /**
     * 获取下一个季节
     * @return 下一个季节
     */
    public Season next() {
        Season[] seasons = values();
        return seasons[(index + 1) % seasons.length];
    }
    
    /**
     * 获取上一个季节
     * @return 上一个季节
     */
    public Season previous() {
        Season[] seasons = values();
        return seasons[(index + 3) % seasons.length];
    }
}
