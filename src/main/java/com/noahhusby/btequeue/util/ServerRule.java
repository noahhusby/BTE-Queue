package com.noahhusby.btequeue.util;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;

public class ServerRule {
    public static void setGamerule(String gamerule, String value) {
        for(World w : Bukkit.getWorlds()) {
            if(w.getName().equals("world")) {
                w.setGameRuleValue(gamerule, value);
            }
        }
    }

    public static void setTime(int time) {
        for (World w : Bukkit.getWorlds()) {
            if (w.getName().equals("world")) {
                w.setTime(time);
            }
        }
    }

    public static void setDifficulty(Difficulty d) {
        for (World w : Bukkit.getWorlds()) {
            if (w.getName().equals("world")) {
                w.setDifficulty(d);
            }
        }
    }
}
