package com.essentialism.data;

import com.essentialism.content.essence.BlockEssenceProfileDefinition;
import com.essentialism.content.essence.EntityEssenceProfileDefinition;
import com.essentialism.content.essence.EssenceProfile;
import com.essentialism.content.essence.EssenceProfilePatch;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

public class EssenceProfileProvider implements DataProvider {
    private final PackOutput.PathProvider blockPathProvider;
    private final PackOutput.PathProvider blockTagPathProvider;
    private final PackOutput.PathProvider entityPathProvider;
    private final PackOutput.PathProvider entityTagPathProvider;

    public EssenceProfileProvider(PackOutput output) {
        this.blockPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "essentialism/block_essence_profiles/blocks");
        this.blockTagPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "essentialism/block_essence_profiles/tags");
        this.entityPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "essentialism/entity_essence_profiles/entities");
        this.entityTagPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "essentialism/entity_essence_profiles/tags");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        createTagProfiles().forEach((tag, profile) -> writes.add(writeBlockTagProfile(output, tag, profile)));
        createBlockProfiles().forEach((blockId, profile) -> writes.add(writeBlockProfile(output, blockId, profile)));
        createEntityTagProfiles().forEach((tag, profile) -> writes.add(writeEntityTagProfile(output, tag, profile)));
        createEntityProfiles().forEach((entityId, profile) -> writes.add(writeEntityProfile(output, entityId, profile)));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> writeBlockProfile(CachedOutput output, Identifier blockId, EssenceProfile profile) {
        Path target = this.blockPathProvider.json(blockId);
        BlockEssenceProfileDefinition definition = new BlockEssenceProfileDefinition(false, EssenceProfilePatch.of(profile));
        JsonElement encodedProfile = BlockEssenceProfileDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(error -> new IllegalStateException("Failed to encode block profile for " + blockId + ": " + error));
        JsonObject encoded = encodedProfile.getAsJsonObject();
        encoded.addProperty("replace", definition.replace());
        return DataProvider.saveStable(output, encoded, target);
    }

    private CompletableFuture<?> writeBlockTagProfile(CachedOutput output, TagKey<Block> tag, EssenceProfile profile) {
        Identifier tagId = tag.location();
        Path target = this.blockTagPathProvider.json(tagId);
        BlockEssenceProfileDefinition definition = new BlockEssenceProfileDefinition(false, EssenceProfilePatch.of(profile));
        JsonElement encodedProfile = BlockEssenceProfileDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(error -> new IllegalStateException("Failed to encode tag profile for " + tagId + ": " + error));
        JsonObject encoded = encodedProfile.getAsJsonObject();
        encoded.addProperty("replace", definition.replace());
        return DataProvider.saveStable(output, encoded, target);
    }

    private CompletableFuture<?> writeEntityProfile(CachedOutput output, Identifier entityId, EssenceProfile profile) {
        Path target = this.entityPathProvider.json(entityId);
        EntityEssenceProfileDefinition definition = new EntityEssenceProfileDefinition(false, EssenceProfilePatch.of(profile));
        JsonElement encodedProfile = EntityEssenceProfileDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(error -> new IllegalStateException("Failed to encode entity profile for " + entityId + ": " + error));
        JsonObject encoded = encodedProfile.getAsJsonObject();
        encoded.addProperty("replace", definition.replace());
        return DataProvider.saveStable(output, encoded, target);
    }

    private CompletableFuture<?> writeEntityTagProfile(CachedOutput output, TagKey<EntityType<?>> tag, EssenceProfile profile) {
        Identifier tagId = tag.location();
        Path target = this.entityTagPathProvider.json(tagId);
        EntityEssenceProfileDefinition definition = new EntityEssenceProfileDefinition(false, EssenceProfilePatch.of(profile));
        JsonElement encodedProfile = EntityEssenceProfileDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(error -> new IllegalStateException("Failed to encode entity tag profile for " + tagId + ": " + error));
        JsonObject encoded = encodedProfile.getAsJsonObject();
        encoded.addProperty("replace", definition.replace());
        return DataProvider.saveStable(output, encoded, target);
    }

    @Override
    public String getName() {
        return "Essentialism Essence Profiles";
    }

    private static Map<TagKey<Block>, EssenceProfile> createTagProfiles() {
        Map<TagKey<Block>, EssenceProfile> profiles = new LinkedHashMap<>();

        add(profiles, new EssenceProfile(20.0F, 20.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                BlockTags.DIRT);
        add(profiles, new EssenceProfile(10.0F, 10.0F, 30.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                BlockTags.ICE);
        add(profiles, new EssenceProfile(25.0F, 55.0F, 15.0F, 0.0F, 0.0F, 0.0F, 5.0F, 0.0F, 10.0F),
                BlockTags.LOGS);
        add(profiles, new EssenceProfile(10.0F, 75.0F, 20.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                BlockTags.LEAVES);
        add(profiles, new EssenceProfile(70.0F, 0.0F, 0.0F, 20.0F, 20.0F, 0.0F, 0.0F, 0.0F, 20.0F),
                Tags.Blocks.ORES);
        add(profiles, new EssenceProfile(18, 0, 12, 8, 0, 18, 0, 0, 0),
                Tags.Blocks.SANDS);
        add(profiles, new EssenceProfile(18, 0, 20, 10, 4, 18, 0, 0, 0),
                Tags.Blocks.SANDS_RED);
        add(profiles, new EssenceProfile(22, 0, 18, 0, 4, 22, 0, 0, 0),
                Tags.Blocks.GRAVELS);
        add(profiles, new EssenceProfile(12, 18, 15, 0, 0, 10, 0, 0, 0),
                BlockTags.MUD);
        add(profiles, new EssenceProfile(18, 42, 12, 0, 0, 0, 4, 0, 6),
                BlockTags.PLANKS);
        add(profiles, new EssenceProfile(14, 36, 10, 0, 0, 0, 2, 0, 4),
                BlockTags.WOODEN_SLABS);
        add(profiles, new EssenceProfile(16, 38, 10, 0, 0, 0, 2, 0, 4),
                BlockTags.WOODEN_STAIRS);
        add(profiles, new EssenceProfile(16, 34, 10, 0, 0, 0, 2, 0, 4),
                BlockTags.WOODEN_FENCES);
        add(profiles, new EssenceProfile(18, 34, 10, 0, 0, 4, 2, 0, 4),
                BlockTags.FENCE_GATES);
        add(profiles, new EssenceProfile(12, 36, 10, 0, 0, 6, 2, 0, 6),
                BlockTags.WOODEN_DOORS);
        add(profiles, new EssenceProfile(14, 34, 10, 0, 0, 6, 2, 0, 6),
                BlockTags.WOODEN_TRAPDOORS);
        add(profiles, new EssenceProfile(8, 18, 6, 0, 0, 10, 2, 0, 8),
                BlockTags.WOODEN_BUTTONS);
        add(profiles, new EssenceProfile(10, 22, 8, 0, 0, 10, 2, 0, 8),
                BlockTags.WOODEN_PRESSURE_PLATES);
        add(profiles, new EssenceProfile(8, 26, 8, 0, 0, 6, 8, 0, 20),
                BlockTags.STANDING_SIGNS);
        add(profiles, new EssenceProfile(8, 26, 8, 0, 0, 4, 8, 0, 20),
                BlockTags.WALL_SIGNS);
        add(profiles, new EssenceProfile(10, 44, 6, 6, 0, 0, 20, 0, 18),
                Tags.Blocks.BOOKSHELVES);
        add(profiles, new EssenceProfile(18, 38, 10, 0, 0, 0, 8, 0, 12),
                Tags.Blocks.CHESTS, Tags.Blocks.BARRELS);
        add(profiles, new EssenceProfile(30.0F, 70.0F, 10.0F, 30.0F, 0.0F, 0.0F, 10.0F, 0.0F, 0.0F),
                BlockTags.GRASS_BLOCKS);
        add(profiles, new EssenceProfile(8, 0, 0, 18, 0, 0, 0, 10, 28),
                Tags.Blocks.GLASS_BLOCKS);
        add(profiles, new EssenceProfile(6, 0, 0, 16, 0, 0, 0, 8, 24),
                Tags.Blocks.GLASS_PANES);
        add(profiles, new EssenceProfile(72, 0, 0, 0, 0, 0, 0, 0, 14),
                Tags.Blocks.STORAGE_BLOCKS_IRON);
        add(profiles, new EssenceProfile(66, 0, 0, 12, 0, 0, 0, 0, 24),
                Tags.Blocks.STORAGE_BLOCKS_GOLD);
        add(profiles, new EssenceProfile(58, 0, 0, 8, 0, 0, 0, 0, 18),
                Tags.Blocks.STORAGE_BLOCKS_COPPER);
        add(profiles, new EssenceProfile(84, 0, 0, 18, 0, 0, 0, 8, 34),
                Tags.Blocks.STORAGE_BLOCKS_DIAMOND);
        add(profiles, new EssenceProfile(70, 0, 0, 14, 0, 0, 0, 6, 28),
                Tags.Blocks.STORAGE_BLOCKS_EMERALD);
        add(profiles, new EssenceProfile(52, 0, 0, 8, 8, 0, 0, 10, 26),
                Tags.Blocks.STORAGE_BLOCKS_LAPIS);
        add(profiles, new EssenceProfile(2, 72, 18, 12, 0, 6, 0, 0, 6),
                Tags.Blocks.FLOWERS);
        add(profiles, new EssenceProfile(3, 58, 14, 8, 0, 8, 0, 0, 4),
                BlockTags.SAPLINGS);
        add(profiles, new EssenceProfile(12, 46, 10, 0, 0, 18, 0, 0, 10),
                BlockTags.BAMBOO_BLOCKS);
        return profiles;
    }

    private static Map<Identifier, EssenceProfile> createBlockProfiles() {
        Map<Identifier, EssenceProfile> profiles = new LinkedHashMap<>();
        add(profiles, new EssenceProfile(30.0F, 70.0F, 10.0F, 30.0F, 0.0F, 0.0F, 10.0F, 0.0F, 0.0F),
                Blocks.MOSS_BLOCK);
        add(profiles, new EssenceProfile(60.0F, 0.0F, 20.0F, 0.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                Blocks.STONE, Blocks.COBBLESTONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE);
        add(profiles, new EssenceProfile(80.0F, 0.0F, 30.0F, 0.0F, 80.0F, 0.0F, 0.0F, 30.0F, 0.0F),
                Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
        add(profiles, new EssenceProfile(0.0F, 0.0F, 10.0F, 95.0F, 70.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                Blocks.GLOWSTONE, Blocks.SEA_LANTERN);
        add(profiles, new EssenceProfile(100.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 95.0F, 0.0F),
                Blocks.BEDROCK);
        add(profiles, new EssenceProfile(10.0F, 0.0F, 95.0F, 0.0F, 80.0F, 0.0F, 80.0F, 10.0F, 0.0F),
                Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
        add(profiles, new EssenceProfile(5.0F, 0.0F, 95.0F, 10.0F, 0.0F, 95.0F, 0.0F, 0.0F, 10.0F),
                Blocks.TNT);
        add(profiles, new EssenceProfile(30.0F, 0.0F, 0.0F, 10.0F, 30.0F, 0.0F, 0.0F, 80.0F, 30.0F),
                Blocks.AMETHYST_BLOCK, Blocks.BUDDING_AMETHYST);
        add(profiles, new EssenceProfile(10.0F, 0.0F, 0.0F, 10.0F, 0.0F, 10.0F, 30.0F, 0.0F, 95.0F),
                Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.BELL);
        add(profiles, new EssenceProfile(10.0F, 0.0F, 80.0F, 10.0F, 30.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                Blocks.NETHERRACK);
        add(profiles, new EssenceProfile(80.0F, 0.0F, 10.0F, 0.0F, 80.0F, 0.0F, 10.0F, 95.0F, 10.0F),
                Blocks.END_STONE);
        add(profiles, new EssenceProfile(10.0F, 0.0F, 80.0F, 80.0F, 0.0F, 0.0F, 0.0F, 0.0F, 10.0F),
                Blocks.MAGMA_BLOCK, Blocks.LAVA);
        add(profiles, new EssenceProfile(95.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 80.0F, 0.0F),
                Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
        add(profiles, new EssenceProfile(10.0F, 10.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 80.0F),
                Blocks.SPONGE, Blocks.WET_SPONGE);
        add(profiles, new EssenceProfile(5.0F, 30.0F, 0.0F, 0.0F, 0.0F, 10.0F, 0.0F, 0.0F, 30.0F),
                Blocks.HONEY_BLOCK);
        add(profiles, new EssenceProfile(5.0F, 30.0F, 0.0F, 0.0F, 0.0F, 80.0F, 0.0F, 0.0F, 30.0F),
                Blocks.SLIME_BLOCK);
        add(profiles, new EssenceProfile(10.0F, 95.0F, 0.0F, 0.0F, 95.0F, 0.0F, 95.0F, 0.0F, 80.0F),
                Blocks.SCULK, Blocks.SCULK_VEIN, Blocks.SCULK_CATALYST, Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER);
        add(profiles, new EssenceProfile(30.0F, 0.0F, 0.0F, 30.0F, 0.0F, 0.0F, 80.0F, 10.0F, 30.0F),
                Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.CONDUIT);
        add(profiles, new EssenceProfile(0.0F, 15.0F, 0.0F, 0.0F, 0.0F, 70.0F, 0.0F, 10.0F, 15.0F),
                Blocks.WATER, Blocks.BUBBLE_COLUMN);
        add(profiles, new EssenceProfile(20.0F, 0.0F, 0.0F, 20.0F, 0.0F, 20.0F, 0.0F, 0.0F, 90.0F),
                Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_WIRE, Blocks.REPEATER, Blocks.COMPARATOR, Blocks.OBSERVER);
        add(profiles, new EssenceProfile(26, 8, 8, 0, 0, 6, 0, 0, 4),
                Blocks.CLAY);
        add(profiles, new EssenceProfile(22, 46, 10, 0, 0, 0, 12, 0, 14),
                Blocks.CRAFTING_TABLE);
        add(profiles, new EssenceProfile(56, 0, 12, 8, 4, 0, 8, 0, 10),
                Blocks.FURNACE);
        add(profiles, new EssenceProfile(68, 0, 14, 10, 4, 0, 10, 0, 14),
                Blocks.BLAST_FURNACE);
        add(profiles, new EssenceProfile(24, 38, 16, 10, 0, 0, 6, 0, 8),
                Blocks.SMOKER);
        add(profiles, new EssenceProfile(62, 18, 0, 24, 12, 0, 42, 18, 36),
                Blocks.ENCHANTING_TABLE);
        add(profiles, new EssenceProfile(84, 0, 6, 0, 0, 0, 10, 0, 18),
                Blocks.ANVIL);
        add(profiles, new EssenceProfile(80, 0, 12, 0, 0, 0, 8, 0, 16),
                Blocks.CHIPPED_ANVIL);
        add(profiles, new EssenceProfile(76, 0, 20, 0, 0, 0, 6, 0, 14),
                Blocks.DAMAGED_ANVIL);
        add(profiles, new EssenceProfile(38, 0, 0, 48, 0, 0, 26, 20, 42),
                Blocks.BEACON);
        add(profiles, new EssenceProfile(18, 40, 8, 0, 0, 0, 18, 0, 16),
                Blocks.LECTERN);
        add(profiles, new EssenceProfile(10, 0, 0, 4, 18, 0, 0, 10, 24),
                Blocks.TINTED_GLASS);
        add(profiles, new EssenceProfile(58, 0, 8, 8, 0, 0, 0, 0, 18),
                Blocks.EXPOSED_COPPER);
        add(profiles, new EssenceProfile(58, 0, 14, 8, 0, 0, 0, 0, 18),
                Blocks.WEATHERED_COPPER);
        add(profiles, new EssenceProfile(58, 0, 20, 8, 0, 0, 0, 0, 18),
                Blocks.OXIDIZED_COPPER);
        add(profiles, new EssenceProfile(14, 30, 18, 16, 0, 4, 0, 0, 4),
                Blocks.CACTUS);
        add(profiles, new EssenceProfile(2, 36, 26, 2, 12, 0, 0, 0, 8),
                Blocks.BROWN_MUSHROOM);
        add(profiles, new EssenceProfile(2, 40, 30, 4, 14, 0, 0, 0, 10),
                Blocks.RED_MUSHROOM);
        add(profiles, new EssenceProfile(68, 0, 24, 0, 18, 0, 0, 0, 6),
                Blocks.BLACKSTONE);
        add(profiles, new EssenceProfile(62, 0, 16, 0, 8, 0, 0, 0, 4),
                Blocks.BASALT);
        add(profiles, new EssenceProfile(66, 0, 12, 0, 6, 0, 0, 0, 4),
                Blocks.SMOOTH_BASALT);
        add(profiles, new EssenceProfile(58, 0, 22, 0, 16, 0, 0, 0, 8),
                Blocks.NETHER_BRICKS);
        add(profiles, new EssenceProfile(42, 0, 4, 12, 0, 0, 0, 6, 18),
                Blocks.QUARTZ_BRICKS);
        return profiles;
    }

    private static Map<Identifier, EssenceProfile> createEntityProfiles() {
        Map<Identifier, EssenceProfile> profiles = new LinkedHashMap<>();
        add(profiles, new EssenceProfile(18.0F, 60.0F, 10.0F, 8.0F, 4.0F, 26.0F, 42.0F, 18.0F, 32.0F),
                EntityType.PLAYER);
        add(profiles, new EssenceProfile(14.0F, 58.0F, 8.0F, 6.0F, 2.0F, 12.0F, 46.0F, 8.0F, 34.0F),
                EntityType.VILLAGER, EntityType.WANDERING_TRADER);
        add(profiles, new EssenceProfile(10.0F, 52.0F, 6.0F, 28.0F, 0.0F, 34.0F, 20.0F, 16.0F, 44.0F),
                EntityType.ALLAY);
        add(profiles, new EssenceProfile(82.0F, 52.0F, 0.0F, 0.0F, 6.0F, 10.0F, 18.0F, 0.0F, 20.0F),
                EntityType.IRON_GOLEM);
        add(profiles, new EssenceProfile(20.0F, 40.0F, 0.0F, 18.0F, 0.0F, 16.0F, 8.0F, 0.0F, 14.0F),
                EntityType.SNOW_GOLEM);
        add(profiles, new EssenceProfile(8.0F, 42.0F, 6.0F, 18.0F, 0.0F, 48.0F, 10.0F, 0.0F, 22.0F),
                EntityType.BEE);
        add(profiles, new EssenceProfile(8.0F, 46.0F, 0.0F, 6.0F, 2.0F, 54.0F, 10.0F, 0.0F, 18.0F),
                EntityType.DOLPHIN, EntityType.AXOLOTL, EntityType.FROG);
        add(profiles, new EssenceProfile(6.0F, 34.0F, 4.0F, 36.0F, 8.0F, 26.0F, 12.0F, 8.0F, 36.0F),
                EntityType.GLOW_SQUID);
        add(profiles, new EssenceProfile(6.0F, 30.0F, 10.0F, 0.0F, 30.0F, 36.0F, 0.0F, 0.0F, 18.0F),
                EntityType.SQUID);
        add(profiles, new EssenceProfile(12.0F, 0.0F, 68.0F, 0.0F, 44.0F, 18.0F, 0.0F, 0.0F, 8.0F),
                EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER,
                EntityType.ZOMBIFIED_PIGLIN, EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED,
                EntityType.WITHER_SKELETON, EntityType.PHANTOM);
        add(profiles, new EssenceProfile(10.0F, 0.0F, 18.0F, 18.0F, 12.0F, 46.0F, 0.0F, 10.0F, 12.0F),
                EntityType.BLAZE, EntityType.GHAST, EntityType.MAGMA_CUBE);
        add(profiles, new EssenceProfile(14.0F, 0.0F, 12.0F, 0.0F, 26.0F, 42.0F, 8.0F, 36.0F, 18.0F),
                EntityType.CREEPER, EntityType.ENDERMAN, EntityType.SHULKER);
        add(profiles, new EssenceProfile(18.0F, 0.0F, 10.0F, 0.0F, 18.0F, 18.0F, 34.0F, 8.0F, 16.0F),
                EntityType.WITCH, EntityType.EVOKER, EntityType.VINDICATOR, EntityType.PILLAGER,
                EntityType.ILLUSIONER, EntityType.RAVAGER);
        add(profiles, new EssenceProfile(24.0F, 0.0F, 10.0F, 0.0F, 12.0F, 42.0F, 0.0F, 0.0F, 26.0F),
                EntityType.SLIME);
        add(profiles, new EssenceProfile(90.0F, 0.0F, 18.0F, 0.0F, 88.0F, 12.0F, 20.0F, 12.0F, 34.0F),
                EntityType.WARDEN);
        add(profiles, new EssenceProfile(30.0F, 72.0F, 0.0F, 26.0F, 0.0F, 38.0F, 26.0F, 28.0F, 44.0F),
                EntityType.ENDER_DRAGON);
        add(profiles, new EssenceProfile(42.0F, 0.0F, 82.0F, 0.0F, 76.0F, 30.0F, 24.0F, 36.0F, 46.0F),
                EntityType.WITHER);
        add(profiles, new EssenceProfile(10.0F, 26.0F, 0.0F, 0.0F, 0.0F, 62.0F, 0.0F, 18.0F, 22.0F),
                EntityType.BREEZE);
        return profiles;
    }

    private static Map<TagKey<EntityType<?>>, EssenceProfile> createEntityTagProfiles() {
        Map<TagKey<EntityType<?>>, EssenceProfile> profiles = new LinkedHashMap<>();
        addEntityTags(profiles, new EssenceProfile(18.0F, 0.0F, 10.0F, 0.0F, 18.0F, 18.0F, 34.0F, 8.0F, 16.0F),
                EntityTypeTags.RAIDERS);
        addEntityTags(profiles, new EssenceProfile(12.0F, 0.0F, 68.0F, 0.0F, 44.0F, 18.0F, 0.0F, 0.0F, 8.0F),
                EntityTypeTags.SKELETONS, EntityTypeTags.ZOMBIES);
        addEntityTags(profiles, new EssenceProfile(18.0F, 52.0F, 6.0F, 4.0F, 0.0F, 22.0F, 8.0F, 0.0F, 12.0F),
                EntityTypeTags.CAN_BREATHE_UNDER_WATER);
        addEntityTags(profiles, new EssenceProfile(8.0F, 44.0F, 4.0F, 6.0F, 0.0F, 48.0F, 6.0F, 0.0F, 20.0F),
                EntityTypeTags.CAN_WEAR_HORSE_ARMOR);

        return profiles;
    }

    private static void add(Map<Identifier, EssenceProfile> profiles, EssenceProfile profile, Block... blocks) {
        for (Block block : blocks) {
            profiles.put(BuiltInRegistries.BLOCK.getKey(block), profile);
        }
    }

    private static void add(Map<Identifier, EssenceProfile> profiles, EssenceProfile profile, EntityType<?>... types) {
        for (EntityType<?> type : types) {
            profiles.put(BuiltInRegistries.ENTITY_TYPE.getKey(type), profile);
        }
    }

    @SafeVarargs
    private static void addEntityTags(Map<TagKey<EntityType<?>>, EssenceProfile> profiles, EssenceProfile profile, TagKey<EntityType<?>>... tags) {
        for (TagKey<EntityType<?>> tag : tags) {
            profiles.put(tag, profile);
        }
    }

    @SafeVarargs
    private static void add(Map<TagKey<Block>, EssenceProfile> profiles, EssenceProfile profile, TagKey<Block>... tags) {
        for (TagKey<Block> tag : tags) {
            profiles.put(tag, profile);
        }
    }
}
