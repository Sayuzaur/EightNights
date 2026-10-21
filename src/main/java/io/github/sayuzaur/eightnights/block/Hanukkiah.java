/*
 * Copyright (c) 2026 Sayuzaur
 * Licensed under the EUPL-1.2-or-later.
 */

package io.github.sayuzaur.eightnights.block;

import io.github.sayuzaur.eightnights.event.init.BlockListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.item.ItemPlacementContext;
import net.modificationstation.stationapi.api.item.context.ItemTagContext;
import net.modificationstation.stationapi.api.registry.tag.ItemTags;
import net.modificationstation.stationapi.api.state.StateManager;
import net.modificationstation.stationapi.api.state.property.BooleanProperty;
import net.modificationstation.stationapi.api.state.property.DirectionProperty;
import net.modificationstation.stationapi.api.state.property.IntProperty;
import net.modificationstation.stationapi.api.state.property.Properties;
import net.modificationstation.stationapi.api.template.block.TemplateBlock;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Hanukkiah extends TemplateBlock {
    public static final DirectionProperty HORIZONTAL_FACING;
    public static final IntProperty NUM_CANDLES;
    public static final BooleanProperty LIT;

    static {
        HORIZONTAL_FACING = Properties.FACING;
        NUM_CANDLES = IntProperty.of("number_of_candles", 0, 9);
        LIT = BooleanProperty.of("lit");
    }

    public Hanukkiah(Identifier identifier) {
        super(identifier, Material.SOLID_ORGANIC);
        this.setHardness(0.5F);
        this.setResistance(1.5F);
        this.setSoundGroup(METAL_SOUND_GROUP);
        this.setBoundingBox(0.4F, 0.0F, 0.4F, 0.6F, 0.75F, 0.6F);
        this.setLuminance(this::getCandlesLuminance);
        setDefaultState(getStateManager().getDefaultState().with(HORIZONTAL_FACING, Direction.NORTH).with(NUM_CANDLES, 0).with(LIT, false));
    }

    public int getCandlesLuminance(BlockState state) {
        if (state.get(LIT)) {
            int numCandles = state.get(NUM_CANDLES);
            if (numCandles != 0) {
                return Math.min(numCandles + 9, 15);
            }
        }
        return 0;
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, NUM_CANDLES, LIT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getStateManager().getDefaultState().with(HORIZONTAL_FACING,context.getHorizontalPlayerFacing()).with(NUM_CANDLES, 0).with(LIT, true);
    }

    @Environment(EnvType.CLIENT)
    public Box getBoundingBox(World world, int x, int y, int z) {
        this.updateBoundingBox(world, x, y, z);
        return super.getBoundingBox(world, x, y, z);
    }

    public Box getCollisionShape(World world, int x, int y, int z) {
        this.updateBoundingBox(world, x, y, z);
        return super.getCollisionShape(world, x, y, z);
    }

    public void updateBoundingBox(BlockView blockView, int x, int y, int z) {
        if (!(blockView instanceof BlockStateView view)) {
            return;
        }
        if (blockView.getBlockId(x, y, z) == this.id) {
            if (view.getBlockState(x, y, z).get(HORIZONTAL_FACING) == Direction.NORTH || view.getBlockState(x, y, z).get(HORIZONTAL_FACING) == Direction.SOUTH) {
                this.setBoundingBox(-0.03125F, 0.0F, 0.40625F, 1.03125F, 0.75F, 0.59375F);
            } else {
                this.setBoundingBox(0.40625F, 0.0F, -0.03125F, 0.59375F, 0.75F, 1.03125F);
            }
        }
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
        return world.getBlockState(x, y, z).getBlock().isFullCube();
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

    @Override
    public boolean onUse(World world, int x, int y, int z, PlayerEntity player) {
        ItemStack userHand = player.getHand();
        BlockState state = world.getBlockState(x, y, z);
        Item candleItem = BlockListener.CANDLE.asItem();
        int candles = state.get(NUM_CANDLES);
        if (!world.isRemote) {

                //REMOVE CANDLES
            if (userHand == null) {
                if (candles > 0) {
                    float varX = x + 0.5F;
                    float varY = y + 0.5F;
                    float varZ = z + 0.5F;

                    ItemEntity candlesStack = new ItemEntity(world, varX, varY, varZ, new ItemStack(candleItem, candles));
                    candlesStack.pickupDelay = 10;
                    world.spawnEntity(candlesStack);

                    world.setBlockState(x, y, z, state.with(NUM_CANDLES, 0));
                    world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "mob.chickenplop", 0.6F, world.random.nextFloat() * 0.3F + 0.2F);
                    return true;
                } else {
                    return false;
                }

                //PLACE CANDLES
            } else if (userHand.itemId == candleItem.id) {
                if (candles < 9) {
                    world.setBlockState(x, y, z, state.with(NUM_CANDLES, candles + 1));
                    userHand.count--;
                    world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "mob.chickenplop", 0.6F, world.random.nextFloat() * 0.4F + 0.6F);
                    return true;
                } else {
                    return false;
                }

                //LIT
            } else if (userHand.isIn(ItemTags.IGNITERS, ItemTagContext.of(userHand))) {
                if (candles > 0) {
                    boolean isLit = state.get(LIT);
                    if (isLit) {
                        world.setBlockState(x, y, z, state.with(LIT, false));
                        world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "random.fizz", 0.5F, world.random.nextFloat() * 0.4F + 1.2F);
                    } else {
                        //Check if igniter is damageable. If so, damage it by 1, or remove stack if needed.
                        if (userHand.isDamageable()) {
                            int newDamage = userHand.getDamage() + 1;
                            if (newDamage >= userHand.getMaxDamage()) {
                                int slot = player.inventory.selectedSlot;
                                player.inventory.removeStack(slot, 1);
                            } else {
                                userHand.setDamage(newDamage);
                            }
                        }
                        world.setBlockState(x, y, z, state.with(LIT, true));
                        world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "fire.ignite", 0.7F, world.random.nextFloat() * 0.4F + 0.8F);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        //TODO Add sounds when playing on server.
        return true;
    }

    @Override
    public List<ItemStack> getDropList(World world, int x, int y, int z, BlockState state, int meta) {
        Item candleItem = BlockListener.CANDLE.asItem();
        ArrayList<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this.asItem()));

        int candles = state.get(NUM_CANDLES);
        if (candles > 0) {
            drops.add(new ItemStack(candleItem, candles));
        }
        return drops;
    }

    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        BlockState state = world.getBlockState(x, y, z);
        int candles = state.get(NUM_CANDLES);

        float flameY = y + 0.8F;
        float flameX = x + 0.5F;
        float flameZ = z + 0.5F;
        float candleSpacing = 0.125F;
        float addMoreSpace = 0.0F;
        boolean lightEvenNum;

        if (state.get(LIT) && candles > 0) {
            //Shammes, always in the middle
            renderParticles(world, flameX, flameY + 0.0625F, flameZ, random);

            if (candles > 1) {
                lightEvenNum = random.nextInt(2) == 0;

                for (int i = 0; i < candles - 1; i++) {
                    if ((i % 2 == 0 && lightEvenNum) || (i % 2 == 1 && !lightEvenNum)) {

                        if (i > 3) {
                            addMoreSpace = candleSpacing;
                        }

                        Direction direction = state.get(HORIZONTAL_FACING);
                        switch (direction) {
                            case NORTH -> {
                                flameX = x;
                                renderParticles(world, flameX + (i * candleSpacing) + addMoreSpace, flameY, flameZ, random);
                            }
                            case SOUTH -> {
                                flameX = x + 1.0F;
                                renderParticles(world, flameX - (i * candleSpacing) - addMoreSpace, flameY, flameZ, random);
                            }
                            case EAST -> {
                                flameZ = z;
                                renderParticles(world, flameX, flameY, flameZ + (i * candleSpacing) + addMoreSpace, random);
                            }
                            case WEST -> {
                                flameZ = z + 1.0F;
                                renderParticles(world, flameX, flameY, flameZ - (i * candleSpacing) - addMoreSpace, random);
                            }
                        }
                    }
                }
            }
        }
    }

    private void renderParticles(World world, float x, float y, float z, Random random) {
        float smokeOffsetY = 0.08F;
        int flameChance = 2;
        int smokeChance = 2;

        if (random.nextInt(flameChance) == 0) {
            world.addParticle("flame", x, y, z, 0.0F, 0.0F, 0.0F);
            if (random.nextInt(smokeChance) == 0) {
                world.addParticle("smoke", x, y + smokeOffsetY, z, 0.0F, 0.0F, 0.0F);
            }
        }
    }
}
