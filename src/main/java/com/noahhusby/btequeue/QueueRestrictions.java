package com.noahhusby.btequeue;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

public class QueueRestrictions implements Listener {

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(e.getTo().getBlockY() < 10) {
            e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), 0, 100, 0));
        }
    }

    @EventHandler
    public void onFoodChangeEvent(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        e.setJoinMessage("");
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e) {
        e.setQuitMessage("");
    }

    @EventHandler
    public void onGamemodeChangeEvent(PlayerGameModeChangeEvent e) {
        if(!e.getPlayer().isOp() && !e.getNewGameMode().equals(GameMode.SURVIVAL)) {
            e.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if(!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if(!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickEvent(PlayerInteractEvent e) {
        if(!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommandFiredEvent(PlayerCommandPreprocessEvent e) {
        String[] args = e.getMessage().split(" ");
        if((args[0].toLowerCase().equals("/plugins") || args[0].toLowerCase().equals("/help")) && !e.getPlayer().isOp()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED+"You can't do that!");
        }
    }
}
