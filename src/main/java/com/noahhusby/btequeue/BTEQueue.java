package com.noahhusby.btequeue;

import com.noahhusby.btequeue.util.ServerRule;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class BTEQueue extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new QueueRestrictions(), this);
        configureServer();
    }

    public void configureServer() {
        getServer().setDefaultGameMode(GameMode.SURVIVAL);
        ServerRule.setGamerule("announceAdvancements", "false");
        ServerRule.setGamerule("doDaylightCycle", "false");
        ServerRule.setGamerule("doFireTick", "false");
        ServerRule.setGamerule("doMobSpawning", "false");
        ServerRule.setGamerule("doWeatherCycle", "false");
        ServerRule.setGamerule("mobGriefing", "false");
        ServerRule.setGamerule("showDeathMessages", "false");
        ServerRule.setDifficulty(Difficulty.PEACEFUL);
        ServerRule.setTime(15000);
    }

}
