package cn.popcraft.weatherevent.util;

import java.util.List;
import java.util.Random;

/**
 * 动态参数处理工具
 * 用于处理范围化参数和简单表达式
 */
public class DynamicValueProcessor {
    
    private static final Random random = new Random();
    
    /**
     * 处理可能是范围的整数值
     * @param value 配置中的值，可能是整数或整数范围列表[min, max]
     * @return 计算后的整数值
     */
    public static int processIntValue(Object value) {
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
            // 简单表达式处理，例如 "100 + 50"
            return evaluateExpression((String) value);
        }
        
        // 默认返回0
        return 0;
    }
    
    /**
     * 处理可能是范围的浮点数值
     * @param value 配置中的值，可能是浮点数或浮点数范围列表[min, max]
     * @return 计算后的浮点数值
     */
    public static double processDoubleValue(Object value) {
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
            // 简单表达式处理
            return evaluateExpression((String) value);
        }
        
        // 默认返回0.0
        return 0.0;
    }
    
    /**
     * 简单表达式求值
     * 支持基本的加减乘除运算
     * @param expression 表达式字符串
     * @return 计算结果
     */
    private static int evaluateExpression(String expression) {
        try {
            // 这里只是一个非常简单的实现，仅支持加法和乘法
            // 实际应用中可能需要更复杂的表达式解析器
            expression = expression.replaceAll("\\s+", ""); // 移除空格
            
            if (expression.contains("+")) {
                String[] parts = expression.split("\\+");
                int result = 0;
                for (String part : parts) {
                    result += Integer.parseInt(part);
                }
                return result;
            } else if (expression.contains("*")) {
                String[] parts = expression.split("\\*");
                int result = 1;
                for (String part : parts) {
                    result *= Integer.parseInt(part);
                }
                return result;
            } else {
                return Integer.parseInt(expression);
            }
        } catch (Exception e) {
            return 0; // 解析失败时返回0
        }
    }
}