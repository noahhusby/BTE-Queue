/**
 * BTEQueue.java
 * Author: Noah Husby
 * Date: 9/1/2020
 */
package com.noahhusby.btequeue;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.ServerLoginHandler;
import com.github.steveice10.mc.protocol.data.game.chunk.BlockStorage;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.chunk.NibbleArray3d;
import com.github.steveice10.mc.protocol.data.game.entity.player.PlayerAction;
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;
import com.github.steveice10.mc.protocol.data.game.world.WorldType;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import com.github.steveice10.mc.protocol.data.message.*;
import com.github.steveice10.mc.protocol.data.status.PlayerInfo;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.VersionInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket;
import com.github.steveice10.opennbt.stream.NBTInputStream;
import com.github.steveice10.opennbt.tag.ByteArrayTag;
import com.github.steveice10.opennbt.tag.CompoundTag;
import com.github.steveice10.opennbt.tag.ShortTag;
import com.github.steveice10.opennbt.tag.Tag;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.event.server.ServerAdapter;
import com.github.steveice10.packetlib.event.server.SessionAddedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.noahhusby.btequeue.config.ConfigHandler;
import com.noahhusby.btequeue.data.chunks.BlockChunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Proxy;
import java.util.*;

import static com.github.steveice10.opennbt.NBTUtils.getChildTag;

public class BTEQueue {
    private static final ConfigHandler config = ConfigHandler.getInstance();

    public static void main(String[] args) {
        splash();
        List<Column> chunkPayload = generateChunkPayload();

        System.out.println("Ready for players!");
        Server server = createServer();

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, (ServerLoginHandler) session -> {
            session.send(new ServerJoinGamePacket(0, false, config.gameMode, 0, Difficulty.PEACEFUL, 1000, WorldType.DEFAULT, false));
            session.send(new ServerUpdateTimePacket(0, 16000));
            session.send(new ServerPlayerPositionRotationPacket(config.x,config.y,config.z,0,0,0));
            // Send initial payload
            for(Column c : chunkPayload) {
                ServerChunkDataPacket pkX = new ServerChunkDataPacket(c);
                session.send(pkX);
            }
        });

        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, config.server_compression_threshold);
        server.addListener(new ServerAdapter() {
            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(PacketReceivedEvent event) {
                        if(event.getPacket() instanceof ClientPlayerPositionRotationPacket) {
                            // Periodically checks player location, and returns them to the structure if they fall in the void.
                            // Since the server isn't actively asking for the location, the client sends it much more infrequently
                            ClientPlayerPositionRotationPacket p = event.getPacket();
                            if(p.getY() < 0) {
                                event.getSession().send(new ServerUpdateTimePacket(0, 18000));
                                event.getSession().send(new ServerPlayerPositionRotationPacket(config.x,config.y,config.z,0,0,0));
                                for(Column c : chunkPayload) {
                                    ServerChunkDataPacket pkX = new ServerChunkDataPacket(c);
                                    event.getSession().send(pkX);
                                }
                            }
                        } else if(event.getPacket() instanceof ClientPlayerPositionPacket) {
                            // Periodically checks player location, and returns them to the structure if they fall in the void.
                            // Since the server isn't actively asking for the location, the client sends it much more infrequently
                            ClientPlayerPositionPacket p = event.getPacket();
                            if(p.getY() < 0) {
                                event.getSession().send(new ServerUpdateTimePacket(0, 18000));
                                event.getSession().send(new ServerPlayerPositionRotationPacket(config.x,config.y,config.z,0,0,0));
                                for(Column c : chunkPayload) {
                                    ServerChunkDataPacket pkX = new ServerChunkDataPacket(c);
                                    event.getSession().send(pkX);
                                }
                            }
                        } else if (event.getPacket() instanceof ClientPlayerActionPacket) {
                            // Transfers the chunk payload upon breaking blocks, thus preventing it
                            // This has no known impact on performance
                            ClientPlayerActionPacket p = event.getPacket();
                            if(p.getAction() == PlayerAction.FINISH_DIGGING) {
                                event.getSession().send(new ServerUpdateTimePacket(0, 18000));
                                for(Column c : chunkPayload) {
                                    ServerChunkDataPacket pkX = new ServerChunkDataPacket(c);
                                    event.getSession().send(pkX);
                                }
                            }
                        }
                    }
                });
            }

        });
        server.bind();

    }

    private static void splash() {
        System.out.println("--------------------------------------------------------");
        System.out.println("BTE Queue v"+ Constants.VERSION);
        System.out.println("Developed by: Noah Husby\n");

        System.out.println("Host: "+ config.host);
        System.out.println("Port: "+ config.port);
        System.out.println("--------------------------------------------------------");

    }

    private static Server createServer() {
        Server server = new Server(config.host, Integer.parseInt(config.port), MinecraftProtocol.class, new TcpSessionFactory(Proxy.NO_PROXY));
        server.setGlobalFlag(MinecraftConstants.AUTH_PROXY_KEY, Proxy.NO_PROXY);
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, false);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, (ServerInfoBuilder) session -> new ServerStatusInfo(new VersionInfo(MinecraftConstants.GAME_VERSION, MinecraftConstants.PROTOCOL_VERSION), new PlayerInfo(1000, 0, new GameProfile[0]), new TextMessage("BTE Queue"), null));
        return server;
    }

    private static List<Column> generateChunkPayload() {
        System.out.println("\nGenerating chunk payload from schematic...");
        // Load Schematic
        File f = new File(System.getProperty("user.dir"), "queue.schematic");
        if(!f.exists()) {
            System.out.println("Error: queue.schematic couldn't be found in: "+System.getProperty("user.dir"));
            System.out.println("Please confirm that the file exists, and try again!");
            System.exit(0);
        }
        try {
            FileInputStream fis = new FileInputStream(f);
            NBTInputStream nbt = new NBTInputStream(fis);
            CompoundTag backupTag = (CompoundTag) nbt.readTag();
            Map<String, Tag> tagCollection = backupTag.getValue();

            short width = getChildTag(tagCollection, "Width", ShortTag.class).getValue();
            short height = getChildTag(tagCollection, "Height", ShortTag.class).getValue();
            short length = getChildTag(tagCollection, "Length", ShortTag.class).getValue();

            byte[] localBlocks = getChildTag(tagCollection, "Blocks", ByteArrayTag.class).getValue();
            byte[] localMetadata = getChildTag(tagCollection, "Data", ByteArrayTag.class).getValue();

            short[][][] blocks = new short[width][height][length];
            byte[][][] metadata = new byte[width][height][length];

            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int z = 0; z < length; z++)
                    {
                        int index = x + (y * length + z) * width;
                        blocks[x][y][z] = (short) ((localBlocks[index] & 0xFF));
                        metadata[x][y][z] = (byte) (localMetadata[index] & 0xFF);
                    }
                }
            }

            nbt.close();
            fis.close();

            double xChunk = 14;
            double yChunk = Math.ceil(height/16.0);
            double zChunk = 14;

            // Increase the default chunk payload if schematic is larger
            if(Math.ceil(width/16.0) > 7) {
                xChunk = Math.ceil(width/16.0);
            }

            if(Math.ceil(length/16.0) > 7) {
                zChunk = Math.ceil(length/16.0);
            }

            List<Column> l = new ArrayList<>();

            for(int x = -14; x < xChunk; x++) {
                for(int z = -14; z < zChunk; z++) {
                    Map<Integer, BlockChunk> verticalChunks = new HashMap<>();
                    for(int y = 0; y < yChunk; y++) {
                        NibbleArray3d blockLighting = new NibbleArray3d(4096);

                        BlockStorage b = new BlockStorage();
                        for(int xBlock = 0; xBlock < 16; xBlock++) {
                            for(int zBlock = 0; zBlock < 16; zBlock++) {
                                for(int yBlock = 0; yBlock < 16; yBlock++) {
                                    try {
                                        b.set(xBlock, yBlock, zBlock, new BlockState(blocks[xBlock+(x*16)][yBlock+(y*16)][zBlock+(z*16)], metadata[xBlock+(x*16)][yBlock+(y*16)][zBlock+(z*16)]));
                                        blockLighting.set(xBlock, yBlock, zBlock, 15);
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        b.set(xBlock, yBlock, zBlock, new BlockState(0, 0));
                                    }
                                }
                            }
                        }
                        verticalChunks.put(y, new BlockChunk(b, blockLighting));
                    }
                    List<Chunk> chunks = new ArrayList<>();
                    for(int val = 0; val < verticalChunks.size(); val++) {
                        chunks.add(new Chunk(verticalChunks.get(val).blockStorage, verticalChunks.get(val).blockLighting, new NibbleArray3d(4096)));
                    }
                    for(int val = 0; val < 16-chunks.size(); val++) {
                          chunks.add(new Chunk(new BlockStorage(),  new NibbleArray3d(4096), new NibbleArray3d(4096)));
                    }
                    Chunk[] chunkArray = chunks.toArray(new Chunk[16]);
                    Column col = new Column(x, z, chunkArray, new byte[256], null);
                    l.add(col);
                }
            }
            System.out.println("Succsessfully generated chunk payload!\n");
            return l;

            } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("There was an error loading the chunk payload, major issues may occur!\n");
        return null;
    }
}
