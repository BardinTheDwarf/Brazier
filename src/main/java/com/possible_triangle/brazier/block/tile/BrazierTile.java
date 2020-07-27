package com.possible_triangle.brazier.block.tile;

import com.google.common.collect.Maps;
import com.possible_triangle.brazier.Brazier;
import com.possible_triangle.brazier.Content;
import com.possible_triangle.brazier.block.BrazierBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;

public class BrazierTile extends BaseTile implements ITickableTileEntity {

    private static final HashMap<BlockPos, Integer> BRAZIERS = Maps.newHashMap();
    private static final int MAX_HEIGHT = 10;

    private int ticksExisted = 0;
    private int height = 0;

    public static boolean inRange(BlockPos pos) {
        return BRAZIERS.entrySet().stream().anyMatch(e -> {
            double dist = e.getKey().distanceSq(pos);
            int maxDist = e.getValue() * e.getValue();
            return dist <= maxDist;
        });
    }

    @Override
    public void tick() {
        ++this.ticksExisted;
        if (this.ticksExisted % 40 == 0) checkStructure();

        if (this.height > 0 && world instanceof ServerWorld && ticksExisted % 10 == 0) {
            ((ServerWorld) world).spawnParticle(Content.FLAME_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, 1, 0.4, 0.8, 0.4, 0);
        }
    }

    public void playSound(SoundEvent sound) {
        if (world != null) world.playSound(null, this.pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private void checkStructure() {
        if (world != null) {
            int height = findHeight();
            if (height != this.height) {

                this.height = height;
                BlockState s = world.getBlockState(pos);
                world.setBlockState(pos, s.with(BrazierBlock.LIT, height > 0));
                if (height > 0) playSound(SoundEvents.ITEM_FIRECHARGE_USE);
                else playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH);

                markDirty();
            }
        }
    }

    private int findHeight() {
        assert world != null;
        if (!world.getBlockState(pos.up()).isAir(world, pos)) return 0;
        for (int height = 1; height <= MAX_HEIGHT; height++) {
            boolean b = true;
            for (int x = -2; x <= 2; x++)
                for (int z = -2; z <= 2; z++)
                    if (Math.abs(x * z) < 4) {
                        BlockState state = world.getBlockState(pos.add(x, -height, z));
                        b = b && Content.BRAZIER_BASE_BLOCKS.func_230235_a_(state.getBlock());
                    }
            if (!b) return height - 1;
        }
        return MAX_HEIGHT;
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT nbt) {
        super.func_230337_a_(state, nbt);
        if (nbt.contains("height")) this.height = nbt.getInt("height");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt = super.write(nbt);
        nbt.putInt("height", height);
        return nbt;
    }

    public BrazierTile() {
        super(Content.BRAZIER_TILE.get());
    }

    public int getRange() {
        return 20 * height;
    }

    @Override
    public void onLoad() {
        BRAZIERS.put(pos, getRange());
    }

    @Override
    public void remove() {
        BRAZIERS.remove(pos);
    }
}
