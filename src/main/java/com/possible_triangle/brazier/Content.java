package com.possible_triangle.brazier;

import com.possible_triangle.brazier.block.BrazierBlock;
import com.possible_triangle.brazier.block.tile.BrazierTile;
import com.possible_triangle.brazier.entity.Crazed;
import com.possible_triangle.brazier.entity.CrazedFlame;
import com.possible_triangle.brazier.entity.render.CrazedFlameRenderer;
import com.possible_triangle.brazier.entity.render.CrazedRender;
import com.possible_triangle.brazier.item.Flame;
import com.possible_triangle.brazier.particle.FlameParticle;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.SidedProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.core.pattern.MarkerSimpleNamePatternConverter;
import org.omg.DynamicAny.DynEnumHelper;

import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;

public class Content {

    public static final ITag<Block> BRAZIER_BASE_BLOCKS = BlockTags.makeWrapperTag(new ResourceLocation(Brazier.MODID, "brazier_base_blocks").toString());

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Brazier.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Brazier.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Brazier.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Brazier.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Brazier.MODID);

    public static final RegistryObject<BrazierBlock> BRAZIER = registerBlock("brazier", BrazierBlock::new, p -> p.group(ItemGroup.DECORATIONS));
    public static final RegistryObject<TileEntityType<BrazierTile>> BRAZIER_TILE = TILES.register("brazier", () ->
        TileEntityType.Builder.create(BrazierTile::new, BRAZIER.get()).build(null)
    );

    public static final RegistryObject<Item> LIVING_FLAME = ITEMS.register("living_flame", Flame::new);

    public static final RegistryObject<EntityType<Crazed>> CRAZED = ENTITIES.register("crazed", () -> EntityType.Builder.<Crazed>create(Crazed::new, EntityClassification.MONSTER)
            .setCustomClientFactory((s, w) -> new Crazed(w))
            .immuneToFire().build("crazed"));

    public static final RegistryObject<EntityType<CrazedFlame>> CRAZED_FLAME = ENTITIES.register("crazed_flame", () -> EntityType.Builder.<CrazedFlame>create(CrazedFlame::new, EntityClassification.MISC)
            .setCustomClientFactory((s, w) -> new CrazedFlame(w))
            .size(0.6F, 0.6F)
            .setShouldReceiveVelocityUpdates(false)
            .immuneToFire().build("crazed_flame"));

    public static final RegistryObject<BasicParticleType> FLAME_PARTICLE = PARTICLES.register("flame", () -> new BasicParticleType(false));

    public static <B extends Block> RegistryObject<B> registerBlock(String name, Supplier<B> supplier, Function<Item.Properties,Item.Properties> props) {
        RegistryObject<B> block = BLOCKS.register(name, supplier);
        ITEMS.register(name, () -> new BlockItem(block.get(), props.apply(new Item.Properties())));
        return block;
    }

    public static void init() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void setup() {
        Content.CRAZED.ifPresent(type -> {
            new SpawnEggItem(type, 0x000000, 0x000000, new Item.Properties().group(ItemGroup.MISC));
            Crazed.init(type);
            EntitySpawnPlacementRegistry.register(type, EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Crazed::canSpawnHere);
        });
    }

    public static void clientSetup(Minecraft mc) {
        CRAZED.ifPresent(type -> mc.getRenderManager().register(type, new CrazedRender(mc.getRenderManager())));
        CRAZED_FLAME.ifPresent(type -> mc.getRenderManager().register(type, new CrazedFlameRenderer(mc.getRenderManager())));
        FLAME_PARTICLE.ifPresent(type -> mc.particles.registerFactory(type, FlameParticle.Factory::new));
    }
}