/*
 * Copyright (c) 2026 Sayuzaur
 * Licensed under the EUPL-1.2-or-later.
 */

package io.github.sayuzaur.eightnights.block;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.template.block.TemplateBlock;
import net.modificationstation.stationapi.api.util.Identifier;

public class Candle extends TemplateBlock {
    public Candle(Identifier identifier) {
        super(identifier, Material.SOLID_ORGANIC);
        this.setHardness(0.5F);
        this.setSoundGroup(WOOL_SOUND_GROUP);
        this.setBoundingBox(0.4F, 0.0F, 0.4F, 0.6F, 0.4F, 0.6F);
        this.setLuminance(0.6F);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    public boolean canPlaceOnTop(World world, int x, int y, int z) {
        //return world.getBlockState(x, y, z).getBlock().isFullCube();
        return false;
    }

    @Override
    public boolean canPlaceAt(World world, int x, int y, int z, int side) {
        return super.canPlaceAt(world, x, y, z) && canPlaceOnTop(world, x, y - 1, z);
    }

    protected final void breakIfCannotStay(World world, int x, int y, int z) {
        if (!canPlaceOnTop(world, x, y - 1, z)) {
            this.dropStacks(world, x, y, z, world.getBlockMeta(x, y, z));
            world.setBlock(x, y, z, 0);
        }
    }

    public void neighborUpdate(World world, int x, int y, int z, int id) {
        super.neighborUpdate(world, x, y, z, id);
        this.breakIfCannotStay(world, x, y, z);
    }
}
