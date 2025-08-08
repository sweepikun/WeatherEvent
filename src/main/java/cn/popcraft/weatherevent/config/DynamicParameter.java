package cn.popcraft.weatherevent.config;

import java.util.List;
import java.util.Random;

/**
 * 动态参数处理
 * 用于处理配置中的动态参数，如范围值
 */
public class DynamicParameter {
    private static final Random random = new Random();
    
    /**
     * 解析整数参数，支持单一值和范围值
     * @param value 参数值，可以是整数或整数范围列表[min, max]
     * @return 解析后的整数值
     */
    public static int parseIntParameter(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> range = (List<Object>) value;
            if (range.size() >= 2) {
                int min = ((Number) range.get(0)).intValue();
                int max = ((Number) range.get(1)).intValue();
                return min + random.nextInt(max - min + 1);
            }
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                // 如果是表达式，可以在这里添加表达式解析
                return 0;
            }
        }
        
        return 0;
    }
    
    /**
     * 解析浮点数参数，支持单一值和范围值
     * @param value 参数值，可以是浮点数或浮点数范围列表[min, max]
     * @return 解析后的浮点数值
     */
    public static double parseDoubleParameter(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> range = (List<Object>) value;
            if (range.size() >= 2) {
                double min = ((Number) range.get(0)).doubleValue();
                double max = ((Number) range.get(1)).doubleValue();
                return min + (max - min) * random.nextDouble();
            }
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                // 如果是表达式，可以在这里添加表达式解析
                return 0.0;
            }
        }
        
        return 0.0;
    }
}