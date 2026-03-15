package cn.popcraft.weatherevent.disaster;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * 活跃灾害类
 * 表示正在发生中的灾害
 */
public class ActiveDisaster {
    
    private final DisasterType type;
    private final DisasterConfig config;
    private final World world;
    private final Location center;
    private final WeatherEvent plugin;
    
    private final long startTime;
    private int tickCount;
    private boolean ended;
    
    /**
     * 创建活跃灾害
     * @param type 灾害类型
     * @param config 灾害配置
     * @param world 世界
     * @param center 中心位置
     * @param plugin 插件实例
     */
    public ActiveDisaster(DisasterType type, DisasterConfig config, World world, 
                         Location center, WeatherEvent plugin) {
        this.type = type;
        this.config = config;
        this.world = world;
        this.center = center;
        this.plugin = plugin;
        this.startTime = System.currentTimeMillis();
        this.tickCount = 0;
        this.ended = false;
        
        // 应用开始效果
        applyStartEffects();
    }
    
    /**
     * 更新灾害状态
     */
    public void update() {
        if (ended) return;
        
        tickCount++;
        
        // 检查是否结束
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedSeconds >= config.getDurationSeconds()) {
            ended = true;
            return;
        }
        
        // 应用每秒效果
        applyTickEffects();
        
        // 根据灾害类型应用特殊效果
        applyTypeSpecificEffects();
    }
    
    /**
     * 应用开始效果
     */
    private void applyStartEffects() {
        // 执行开始命令
        for (String command : config.getStartCommands()) {
            String processedCommand = command
                    .replace("%world%", world.getName())
                    .replace("%x%", String.valueOf(center.getX()))
                    .replace("%y%", String.valueOf(center.getY()))
                    .replace("%z%", String.valueOf(center.getZ()))
                    .replace("%disaster%", type.getId());
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
        
        // 发送警告消息
        if (!config.getWarningMessage().isEmpty()) {
            String message = type.getColor() + config.getWarningMessage();
            for (Player player : getAffectedPlayers()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 应用每秒效果
     */
    private void applyTickEffects() {
        // 执行每秒命令
        for (String command : config.getTickCommands()) {
            String processedCommand = command
                    .replace("%world%", world.getName())
                    .replace("%x%", String.valueOf(center.getX()))
                    .replace("%y%", String.valueOf(center.getY()))
                    .replace("%z%", String.valueOf(center.getZ()))
                    .replace("%disaster%", type.getId())
                    .replace("%tick%", String.valueOf(tickCount));
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
        
        // 对受影响的实体造成伤害
        applyDamage();
    }
    
    /**
     * 应用类型特定效果
     */
    private void applyTypeSpecificEffects() {
        switch (type) {
            case TORNADO:
                applyTornadoEffects();
                break;
            case FLOOD:
                applyFloodEffects();
                break;
            case HAILSTORM:
                applyHailstormEffects();
                break;
            case SANDSTORM:
                applySandstormEffects();
                break;
            case BLIZZARD:
                applyBlizzardEffects();
                break;
            case THUNDERSTORM:
                applyThunderstormEffects();
                break;
            case HEATWAVE:
                applyHeatwaveEffects();
                break;
            case FROST:
                applyFrostEffects();
                break;
        }
    }
    
    /**
     * 应用龙卷风效果
     */
    private void applyTornadoEffects() {
        // 将实体拉向中心
        for (LivingEntity entity : getAffectedEntities()) {
            Location entityLoc = entity.getLocation();
            Vector direction = center.toVector().subtract(entityLoc.toVector()).normalize();
            Vector velocity = direction.multiply(0.5);
            velocity.setY(0.3); // 向上拉
            entity.setVelocity(velocity);
        }
        
        // 生成粒子效果
        if (tickCount % 5 == 0) {
            String particleCommand = "particle minecraft:cloud " + 
                    center.getX() + " " + (center.getY() + 10) + " " + center.getZ() + 
                    " 3 10 3 0.1 50 force";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), particleCommand);
        }
    }
    
    /**
     * 应用洪水效果
     */
    private void applyFloodEffects() {
        // 这里可以实现水位上升逻辑
        // 由于Minecraft限制，我们通过命令模拟
        if (tickCount % 20 == 0) {
            String waterCommand = "execute at @a[distance=.." + (int)config.getRadius() + 
                    "] run fill ~-2 ~ ~-2 ~2 ~ ~2 water keep";
            // 注意：这个命令可能需要根据实际情况调整
        }
    }
    
    /**
     * 应用冰雹效果
     */
    private void applyHailstormEffects() {
        // 生成冰雹粒子
        if (tickCount % 3 == 0) {
            String particleCommand = "particle minecraft:block ice " + 
                    center.getX() + " " + (center.getY() + 20) + " " + center.getZ() + 
                    " 10 0 10 0.5 100 force";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), particleCommand);
        }
        
        // 减缓实体移动速度
        for (LivingEntity entity : getAffectedEntities()) {
            entity.setVelocity(entity.getVelocity().multiply(0.8));
        }
    }
    
    /**
     * 应用沙尘暴效果
     */
    private void applySandstormEffects() {
        // 生成沙尘粒子
        if (tickCount % 2 == 0) {
            String particleCommand = "particle minecraft:dust 0.8 0.7 0.4 1 " + 
                    center.getX() + " " + (center.getY() + 2) + " " + center.getZ() + 
                    " 15 5 15 0.1 200 force";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), particleCommand);
        }
        
        // 降低能见度（通过给予失明效果）
        for (Player player : getAffectedPlayers()) {
            // 给予短暂的失明效果
            String effectCommand = "effect give " + player.getName() + 
                    " minecraft:blindness 2 0 true";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), effectCommand);
        }
    }
    
    /**
     * 应用暴风雪效果
     */
    private void applyBlizzardEffects() {
        // 生成雪花粒子
        if (tickCount % 2 == 0) {
            String particleCommand = "particle minecraft:snowflake " + 
                    center.getX() + " " + (center.getY() + 15) + " " + center.getZ() + 
                    " 12 0 12 0.05 150 force";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), particleCommand);
        }
        
        // 给予缓慢效果
        for (Player player : getAffectedPlayers()) {
            String effectCommand = "effect give " + player.getName() + 
                    " minecraft:slowness 3 1 true";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), effectCommand);
        }
    }
    
    /**
     * 应用雷暴效果
     */
    private void applyThunderstormEffects() {
        // 随机生成闪电
        if (Math.random() < 0.05) { // 5%几率每秒
            double offsetX = (Math.random() - 0.5) * config.getRadius() * 2;
            double offsetZ = (Math.random() - 0.5) * config.getRadius() * 2;
            Location strikeLocation = center.clone().add(offsetX, 0, offsetZ);
            
            world.strikeLightning(strikeLocation);
        }
    }
    
    /**
     * 应用热浪效果
     */
    private void applyHeatwaveEffects() {
        // 生成热浪粒子
        if (tickCount % 10 == 0) {
            String particleCommand = "particle minecraft:flame " + 
                    center.getX() + " " + (center.getY() + 1) + " " + center.getZ() + 
                    " 10 0 10 0.02 30 force";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), particleCommand);
        }
        
        // 给予饥饿效果
        for (Player player : getAffectedPlayers()) {
            String effectCommand = "effect give " + player.getName() + 
                    " minecraft:hunger 5 0 true";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), effectCommand);
        }
    }
    
    /**
     * 应用霜冻效果
     */
    private void applyFrostEffects() {
        // 生成霜冻粒子
        if (tickCount % 5 == 0) {
            String particleCommand = "particle minecraft:snowflake " + 
                    center.getX() + " " + (center.getY() + 1) + " " + center.getZ() + 
                    " 8 2 8 0.02 50 force";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), particleCommand);
        }
        
        // 给予冻结效果
        for (Player player : getAffectedPlayers()) {
            String effectCommand = "effect give " + player.getName() + 
                    " minecraft:slowness 3 2 true";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), effectCommand);
        }
    }
    
    /**
     * 对受影响的实体造成伤害
     */
    private void applyDamage() {
        if (config.getDamagePerSecond() <= 0) return;
        
        for (LivingEntity entity : getAffectedEntities()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                // 检查玩家是否免疫
                if (player.hasPermission("weatherevent.disaster.bypass")) {
                    continue;
                }
            }
            
            entity.damage(config.getDamagePerSecond());
        }
    }
    
    /**
     * 应用结束效果
     */
    public void applyEndEffects() {
        // 执行结束命令
        for (String command : config.getEndCommands()) {
            String processedCommand = command
                    .replace("%world%", world.getName())
                    .replace("%x%", String.valueOf(center.getX()))
                    .replace("%y%", String.valueOf(center.getY()))
                    .replace("%z%", String.valueOf(center.getZ()))
                    .replace("%disaster%", type.getId())
                    .replace("%duration%", String.valueOf(tickCount));
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
        
        // 发送结束消息
        String message = "§a[灾害结束] " + type.getColor() + type.getDisplayName() + 
                        "§a 已经结束！";
        for (Player player : world.getPlayers()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * 获取受影响的玩家
     * @return 受影响的玩家列表
     */
    private List<Player> getAffectedPlayers() {
        return world.getPlayers();
    }
    
    /**
     * 获取受影响的实体
     * @return 受影响的实体列表
     */
    private List<LivingEntity> getAffectedEntities() {
        List<LivingEntity> entities = new java.util.ArrayList<>();
        
        for (Entity entity : world.getNearbyEntities(center, config.getRadius(), 
                config.getRadius(), config.getRadius())) {
            if (entity instanceof LivingEntity) {
                entities.add((LivingEntity) entity);
            }
        }
        
        return entities;
    }
    
    // Getters
    public DisasterType getType() {
        return type;
    }
    
    public DisasterConfig getConfig() {
        return config;
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getCenter() {
        return center;
    }
    
    public boolean isEnded() {
        return ended;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public int getTickCount() {
        return tickCount;
    }
}
