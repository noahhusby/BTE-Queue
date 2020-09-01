/**
 * config/ConfigHandler.java
 * Author: Noah Husby
 * Date: 9/1/2020
 */
package com.noahhusby.btequeue.config;

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;

public class ConfigHandler {
    private static ConfigHandler mInstance = null;

    public static ConfigHandler getInstance() {
        if(mInstance == null) mInstance = new ConfigHandler();
        return mInstance;
    }

    public String host;
    public String port;
    public int server_compression_threshold;
    public GameMode gameMode;
    public int x;
    public int y;
    public int z;

    private ConfigHandler() {
        createConfig();
        getFieldsFromConfig();
    }

    private void getFieldsFromConfig() {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(new File(System.getProperty("user.dir"), "config.json"))) {
            //Read JSON file
            JSONObject config = (JSONObject) jsonParser.parse(reader);
            host = (String) config.getOrDefault("host", "127.0.0.1");
            port = (String) config.getOrDefault("port", "25565");
            server_compression_threshold = Integer.parseInt((String) config.getOrDefault("server_compression_threshold", "100"));
            String cGameMode = (String) config.getOrDefault("gamemode", "survival");
            switch (cGameMode) {
                default:
                case "survival":
                    gameMode = GameMode.SURVIVAL;
                    break;
                case "creative":
                    gameMode = GameMode.CREATIVE;
                    break;
            }
            JSONObject spawn_location = (JSONObject) config.get("spawn_location");
            x = Integer.parseInt((String) spawn_location.getOrDefault("x", "8"));
            y = Integer.parseInt((String) spawn_location.getOrDefault("y", "1"));
            z = Integer.parseInt((String) spawn_location.getOrDefault("z", "9"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void createConfig() {
        File file = new File(System.getProperty("user.dir"), "config.json");

        if (!file.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.json")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }}
