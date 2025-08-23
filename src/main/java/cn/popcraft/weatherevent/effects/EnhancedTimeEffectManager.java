package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * 增强的时间效果管理器
 * 支持特殊节日、月相和玩家在线时长相关的功能
 */
public class EnhancedTimeEffectManager {
    private final WeatherEvent plugin;
    private final Map<String, SpecialEventEffect> specialEventEffects;
    private final Map<UUID, PlayerSession> playerSessions;
    
    public EnhancedTimeEffectManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.specialEventEffects = new HashMap<>();
        this.playerSessions = new HashMap<>();
        loadSpecialEventEffects();
    }
    
    /**
     * 加载特殊事件效果
     */
    private void loadSpecialEventEffects() {
        // 添加一些预定义的特殊事件效果
        // 春节效果 (农历新年，这里简化为公历2月)
        specialEventEffects.put("spring_festival", new SpecialEventEffect(
            "spring_festival", 
            Month.FEBRUARY, 
            Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), // 2月1日到15日
            "luck_boost"
        ));
        
        // 万圣节效果 (10月31日)
        specialEventEffects.put("halloween", new SpecialEventEffect(
            "halloween",
            Month.OCTOBER,
            Arrays.asList(31),
            "fear_effect"
        ));
        
        // 圣诞节效果 (12月24-26日)
        specialEventEffects.put("christmas", new SpecialEventEffect(
            "christmas",
            Month.DECEMBER,
            Arrays.asList(24, 25, 26),
            "festive_warmth"
        ));
        
        // 夏日炎炎效果 (7月和8月)
        specialEventEffects.put("summer_heat", new SpecialEventEffect(
            "summer_heat",
            null, // 月份为null表示跨多个月
            Arrays.asList(Month.JULY, Month.AUGUST),
            "heat_resistance"
        ));
    }
    
    /**
     * 检查并应用特殊事件效果
     * @param player 玩家
     * @param world 世界
     */
    public void applySpecialEventEffects(Player player, World world) {
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        int currentDay = currentDate.getDayOfMonth();
        
        // 检查所有特殊事件效果
        for (SpecialEventEffect effect : specialEventEffects.values()) {
            if (effect.isApplicable(currentMonth, currentDay)) {
                effect.apply(player, world);
            }
        }
    }
    
    /**
     * 获取当前月相效果
     * @param world 世界
     * @return 月相效果类型
     */
    public String getMoonPhaseEffect(World world) {
        // Minecraft中一个月相周期为8个游戏日(192000 ticks)
        long fullTime = world.getFullTime();
        int moonPhase = (int) ((fullTime / 24000) % 8);
        
        switch (moonPhase) {
            case 0: // 满月
                return "full_moon";
            case 1: // 亏凸月
                return "waning_gibbous";
            case 2: // 下弦月
                return "last_quarter";
            case 3: // 亏弦月
                return "waning_crescent";
            case 4: // 新月
                return "new_moon";
            case 5: // 峨眉月
                return "waxing_crescent";
            case 6: // 上弦月
                return "first_quarter";
            case 7: // 盈凸月
                return "waxing_gibbous";
            default:
                return "normal";
        }
    }
    
    /**
     * 获取玩家会话信息
     * @param player 玩家
     * @return 玩家会话信息
     */
    public PlayerSession getPlayerSession(Player player) {
        return playerSessions.computeIfAbsent(player.getUniqueId(), 
            uuid -> new PlayerSession(player.getUniqueId()));
    }
    
    /**
     * 更新玩家会话信息
     * @param player 玩家
     */
    public void updatePlayerSession(Player player) {
        PlayerSession session = getPlayerSession(player);
        session.updateSessionTime();
    }
    
    /**
     * 获取基于玩家在线时长的效果加成
     * @param player 玩家
     * @return 在线时长效果加成类型
     */
    public String getPlaytimeEffect(Player player) {
        PlayerSession session = getPlayerSession(player);
        long totalPlayTime = session.getTotalPlayTime();
        
        if (totalPlayTime > 720000) { // 60分钟以上
            return "veteran_bonus";
        } else if (totalPlayTime > 360000) { // 30分钟以上
            return "experienced_bonus";
        } else if (totalPlayTime > 120000) { // 10分钟以上
            return "regular_bonus";
        } else {
            return "new_player_bonus";
        }
    }
    
    /**
     * 特殊事件效果类
     */
    private static class SpecialEventEffect {
        private final String id;
        private final Month month;
        private final List<?> days; // 可以是Integer列表(具体日期)或Month列表(月份)
        private final String effectType;
        
        public SpecialEventEffect(String id, Month month, List<?> days, String effectType) {
            this.id = id;
            this.month = month;
            this.days = days;
            this.effectType = effectType;
        }
        
        /**
         * 检查当前日期是否适用于此特殊事件
         * @param currentMonth 当前月份
         * @param currentDay 当前日期
         * @return 是否适用
         */
        public boolean isApplicable(Month currentMonth, int currentDay) {
            // 如果没有指定月份，则检查days是否为月份列表
            if (month == null) {
                return days.contains(currentMonth);
            }
            
            // 检查月份是否匹配
            if (currentMonth != month) {
                return false;
            }
            
            // 检查日期是否在列表中
            return days.contains(currentDay);
        }
        
        /**
         * 应用特殊事件效果
         * @param player 玩家
         * @param world 世界
         */
        public void apply(Player player, World world) {
            // 这里可以实现具体的效果应用逻辑
            // 例如发送节日消息、应用特殊药水效果等
            switch (effectType) {
                case "luck_boost":
                    // 可以增加幸运效果
                    break;
                case "fear_effect":
                    // 可以增加恐惧效果
                    break;
                case "festive_warmth":
                    // 可以增加温暖效果
                    break;
                case "heat_resistance":
                    // 可以增加抗热效果
                    break;
            }
        }
    }
    
    /**
     * 玩家会话信息类
     */
    public static class PlayerSession {
        private final UUID playerId;
        private long sessionStartTime;
        private long totalPlayTime;
        
        public PlayerSession(UUID playerId) {
            this.playerId = playerId;
            this.sessionStartTime = System.currentTimeMillis();
            this.totalPlayTime = 0;
        }
        
        /**
         * 更新会话时间
         */
        public void updateSessionTime() {
            long currentTime = System.currentTimeMillis();
            long sessionDuration = currentTime - sessionStartTime;
            totalPlayTime += sessionDuration;
            sessionStartTime = currentTime;
        }
        
        /**
         * 获取总游戏时间(毫秒)
         * @return 总游戏时间
         */
        public long getTotalPlayTime() {
            return totalPlayTime;
        }
        
        /**
         * 获取玩家ID
         * @return 玩家ID
         */
        public UUID getPlayerId() {
            return playerId;
        }
    }
}