package com.possible_triangle.brazier.entity;

import com.possible_triangle.brazier.Content;
import com.possible_triangle.brazier.block.tile.BrazierTile;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Crazed extends SpellcastingIllagerEntity {

    public static final double BUFF_RADIUS = 7;

    @SuppressWarnings("unused")
    public Crazed(World world) {
        this(Content.CRAZED.get(), world);
    }

    public static void init(EntityType<? extends LivingEntity> type) {
        GlobalEntityTypeAttributes.put(type, MonsterEntity.func_234295_eP_()
                .func_233815_a_(Attributes.field_233821_d_, 0.6D)
                .func_233815_a_(Attributes.field_233819_b_, 12.0D)
                .func_233815_a_(Attributes.field_233818_a_, 24.0D)
                .func_233813_a_()
        );
    }

    public Crazed(EntityType<? extends Crazed> type, World world) {
        super(type, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new CastingASpellGoal());
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, PlayerEntity.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.addGoal(5, new BuffSpellGoal());
        this.goalSelector.addGoal(6, new FlameSpellGoal());
        this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, AbstractRaiderEntity.class)).setCallsForHelp());
        this.targetSelector.addGoal(2, (new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, (new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, GuardianEntity.class, false));
    }

    @Override
    protected SoundEvent getSpellSound() {
        return SoundEvents.ITEM_FIRECHARGE_USE;
    }

    @Override
    public void func_213660_a(int p_213660_1_, boolean p_213660_2_) {
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_EVOKER_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_EVOKER_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_EVOKER_HURT;
    }

    @Override
    public SoundEvent getRaidLossSound() {
        return SoundEvents.ENTITY_VINDICATOR_CELEBRATE;
    }

    class FlameSpellGoal extends SpellcastingIllagerEntity.UseSpellGoal {
        private FlameSpellGoal() {
        }

        protected int getCastingTime() {
            return 40;
        }

        protected int getCastingInterval() {
            return 100;
        }

        protected void castSpell() {
            LivingEntity target = Crazed.this.getAttackTarget();
            if (target != null) spawnFlame(target.prevPosX, target.prevPosY, target.prevPosZ);
        }

        private void spawnFlame(double x, double y, double z) {
            BlockPos blockpos = new BlockPos(x, y, z);
            if (!Crazed.this.world.hasWater(blockpos))
                Crazed.this.world.addEntity(new CrazedFlame(Crazed.this.world, x, y + 0.4, z, Crazed.this));
        }

        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK;
        }

        protected SpellType getSpellType() {
            return SpellType.FANGS;
        }
    }

    class BuffSpellGoal extends SpellcastingIllagerEntity.UseSpellGoal {
        private BuffSpellGoal() {
        }

        protected int getCastingTime() {
            return 10;
        }

        protected int getCastingInterval() {
            return 200;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && getTargets().stream().anyMatch(e -> !e.isPotionActive(Effects.FIRE_RESISTANCE));
        }

        private List<LivingEntity> getTargets() {
            return world.getEntitiesWithinAABB(LivingEntity.class, getBoundingBox().grow(BUFF_RADIUS), e ->
                    e.isAlive() && (EntityTypeTags.RAIDERS.func_230235_a_(e.getType()) || e.isOnSameTeam(Crazed.this))
            );
        }

        protected void castSpell() {
            getTargets().forEach(this::buff);
        }

        private void buff(LivingEntity entity) {
            entity.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 20 * 10, 0));
        }

        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK;
        }

        protected SpellType getSpellType() {
            return SpellType.FANGS;
        }
    }

    public static boolean canSpawnHere(EntityType<? extends MonsterEntity> type, IWorld world, SpawnReason reason, BlockPos pos, Random random) {
        return canMonsterSpawnInLight(type, world, reason, pos, random) && (reason != SpawnReason.NATURAL || world.getBlockState(pos).isFireSource(world, pos, Direction.UP));
    }

}
