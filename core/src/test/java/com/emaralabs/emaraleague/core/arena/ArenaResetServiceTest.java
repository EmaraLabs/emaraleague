package com.emaralabs.emaraleague.core.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArenaResetServiceTest {

    private ArenaResetService service;
    private Arena arena;

    @BeforeEach
    void setUp() {
        service = new ArenaResetService();
        arena = new Arena("TestArena");
    }

    @Test
    void trackBlockBreak_storesOriginalState() {
        Block block = mock(Block.class);
        Location loc = mock(Location.class);
        BlockData data = mock(BlockData.class);
        when(block.getLocation()).thenReturn(loc);
        when(block.getBlockData()).thenReturn(data);
        when(data.clone()).thenReturn(data);

        service.trackBlockBreak(arena, block);

        assertTrue(service.hasTrackedChanges(arena));
        assertEquals(1, service.getTrackedChangeCount(arena));
    }

    @Test
    void trackBlockBreak_multipleBlocks_storesAll() {
        Block block1 = mock(Block.class);
        Block block2 = mock(Block.class);
        Location loc1 = mock(Location.class);
        Location loc2 = mock(Location.class);
        BlockData data = mock(BlockData.class);

        when(block1.getLocation()).thenReturn(loc1);
        when(block2.getLocation()).thenReturn(loc2);
        when(block1.getBlockData()).thenReturn(data);
        when(block2.getBlockData()).thenReturn(data);
        when(data.clone()).thenReturn(data);

        service.trackBlockBreak(arena, block1);
        service.trackBlockBreak(arena, block2);

        assertEquals(2, service.getTrackedChangeCount(arena));
    }

    @Test
    void hasTrackedChanges_noChanges_returnsFalse() {
        assertFalse(service.hasTrackedChanges(arena));
    }

    @Test
    void getTrackedChangeCount_noChanges_returnsZero() {
        assertEquals(0, service.getTrackedChangeCount(arena));
    }

    @Test
    void clearTrackedChanges_removesTracking() {
        Block block = mock(Block.class);
        Location loc = mock(Location.class);
        BlockData data = mock(BlockData.class);
        when(block.getLocation()).thenReturn(loc);
        when(block.getBlockData()).thenReturn(data);
        when(data.clone()).thenReturn(data);

        service.trackBlockBreak(arena, block);
        assertTrue(service.hasTrackedChanges(arena));

        service.clearTrackedChanges(arena);
        assertFalse(service.hasTrackedChanges(arena));
    }

    @Test
    void restoreArena_restoresBlocks() {
        Block block = mock(Block.class);
        Location loc = mock(Location.class);
        BlockData data = mock(BlockData.class);
        org.bukkit.World world = mock(org.bukkit.World.class);
        Block worldBlock = mock(Block.class);

        when(block.getLocation()).thenReturn(loc);
        when(block.getBlockData()).thenReturn(data);
        when(data.clone()).thenReturn(data);
        when(loc.getWorld()).thenReturn(world);
        when(loc.getBlock()).thenReturn(worldBlock);

        service.trackBlockBreak(arena, block);
        service.restoreArena(arena);

        verify(worldBlock).setBlockData(data);
    }

    @Test
    void restoreArena_clearsTrackedChanges() {
        Block block = mock(Block.class);
        Location loc = mock(Location.class);
        BlockData data = mock(BlockData.class);
        org.bukkit.World world = mock(org.bukkit.World.class);
        Block worldBlock = mock(Block.class);

        when(block.getLocation()).thenReturn(loc);
        when(block.getBlockData()).thenReturn(data);
        when(data.clone()).thenReturn(data);
        when(loc.getWorld()).thenReturn(world);
        when(loc.getBlock()).thenReturn(worldBlock);

        service.trackBlockBreak(arena, block);
        service.restoreArena(arena);

        assertFalse(service.hasTrackedChanges(arena));
    }

    @Test
    void restoreArena_noChanges_doesNothing() {
        service.restoreArena(arena);
        assertFalse(service.hasTrackedChanges(arena));
    }

    @Test
    void restoreArena_nullWorld_skipsBlock() {
        Block block = mock(Block.class);
        Location loc = mock(Location.class);
        BlockData data = mock(BlockData.class);

        when(block.getLocation()).thenReturn(loc);
        when(block.getBlockData()).thenReturn(data);
        when(data.clone()).thenReturn(data);
        when(loc.getWorld()).thenReturn(null);

        service.trackBlockBreak(arena, block);
        service.restoreArena(arena);

        assertFalse(service.hasTrackedChanges(arena));
    }
}
