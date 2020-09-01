/**
 * data/chunks/BlockChunk.java
 * Author: Noah Husby
 * Date: 9/1/2020
 */
package com.noahhusby.btequeue.data.chunks;

import com.github.steveice10.mc.protocol.data.game.chunk.BlockStorage;
import com.github.steveice10.mc.protocol.data.game.chunk.NibbleArray3d;

public class BlockChunk {
    public final BlockStorage blockStorage;
    public final NibbleArray3d blockLighting;

    public BlockChunk(BlockStorage blockStorage, NibbleArray3d blockLighting) {
        this.blockStorage = blockStorage;
        this.blockLighting = blockLighting;
    }
}
