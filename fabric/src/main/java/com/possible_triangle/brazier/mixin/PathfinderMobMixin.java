package com.possible_triangle.brazier.mixin;

import com.possible_triangle.brazier.block.BrazierBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathfinderMob.class)
public class PathfinderMobMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z")
    public void checkSpawnRules(LevelAccessor world, MobSpawnType reason, CallbackInfoReturnable<Boolean> callback) {
        if(BrazierBlock.prevents((Entity) (Object) this, world, reason)) callback.setReturnValue(false);
    }

}