/*
 * Copyright (c) 2026 Sayuzaur
 * Licensed under the EUPL-1.2-or-later.
 */

package io.github.sayuzaur.eightnights.event.init;

import io.github.sayuzaur.eightnights.block.Candle;
import io.github.sayuzaur.eightnights.block.Hanukkiah;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;

import java.lang.invoke.MethodHandles;

import static io.github.sayuzaur.eightnights.event.init.InitListener.NAMESPACE;

public class BlockListener {
    static {
        EntrypointManager.registerLookup(MethodHandles.lookup());
    }

    public static Block HANUKKIAH;
    public static Block CANDLE;

    @EventListener
    private static void registerBlocks(BlockRegistryEvent event) {
        CANDLE = new Candle(NAMESPACE.id("candle"));
        HANUKKIAH = new Hanukkiah(NAMESPACE.id("hanukkiah"));
    }
}