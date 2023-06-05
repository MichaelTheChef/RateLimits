package me.mio.ratelimits;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RateLimits extends JavaPlugin implements Listener {
    private Map<UUID, Integer> requestCounts;
    private Map<UUID, BukkitTask> resetTasks;
    private int maxRequestsPerSecond;

    @Override
    public void onEnable() {
        requestCounts = new HashMap<>();
        resetTasks = new HashMap<>();
        maxRequestsPerSecond = 10;

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (BukkitTask task : resetTasks.values()) {
            task.cancel();
        }
        requestCounts.clear();
        resetTasks.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        startResetTask(playerId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        stopResetTask(playerId);
        requestCounts.remove(playerId);
    }

    private void startResetTask(UUID playerId) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                requestCounts.remove(playerId);
            }
        }.runTaskLater(this, 20L);

        resetTasks.put(playerId, task);
    }

    private void stopResetTask(UUID playerId) {
        BukkitTask task = resetTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isRateLimited(UUID playerId) {
        int requestCount = requestCounts.getOrDefault(playerId, 0);
        return requestCount >= maxRequestsPerSecond;
    }

    public void incrementRequestCount(UUID playerId) {
        int requestCount = requestCounts.getOrDefault(playerId, 0);
        requestCounts.put(playerId, requestCount + 1);
    }
}
