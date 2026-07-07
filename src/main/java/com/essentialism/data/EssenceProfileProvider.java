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
        return writeDefinition(output, target, BlockEssenceProfileDefinition.CODEC, definition);
    }

    private CompletableFuture<?> writeBlockTagProfile(CachedOutput output, TagKey<Block> tag, EssenceProfile profile) {
        Path target = this.blockTagPathProvider.json(tag.location());
        BlockEssenceProfileDefinition definition = new BlockEssenceProfileDefinition(false, EssenceProfilePatch.of(profile));
        return writeDefinition(output, target, BlockEssenceProfileDefinition.CODEC, definition);
    }

    private CompletableFuture<?> writeEntityProfile(CachedOutput output, Identifier entityId, EssenceProfile profile) {
        Path target = this.entityPathProvider.json(entityId);
        EntityEssenceProfileDefinition definition = new EntityEssenceProfileDefinition(false, EssenceProfilePatch.of(profile));
        return writeDefinition(output, target, EntityEssenceProfileDefinition.CODEC, definition);
    }

    private CompletableFuture<?> writeEntityTagProfile(CachedOutput output, TagKey<EntityType<?>> tag, EssenceProfile profile) {
        Path target = this.entityTagPathProvider.json(tag.location());
        EntityEssenceProfileDefinition definition = new EntityEssenceProfileDefinition(false, EssenceProfilePatch.of(profile));
        return writeDefinition(output, target, EntityEssenceProfileDefinition.CODEC, definition);
    }

    private static <T> CompletableFuture<?> writeDefinition(CachedOutput output, Path target,
            com.mojang.serialization.Codec<T> codec, T definition) {
        JsonElement encoded = codec.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(error -> new IllegalStateException("Failed to encode: " + error));
        JsonObject obj = encoded.getAsJsonObject();
        obj.addProperty("replace", false);
        return DataProvider.saveStable(output, obj, target);
    }

    @Override
    public String getName() {
        return "Essentialism Essence Profiles";
    }

    private static Map<TagKey<Block>, EssenceProfile> createTagProfiles() {
        Map<TagKey<Block>, EssenceProfile> profiles = new LinkedHashMap<>();

        add(profiles, new EssenceProfile(20.0F, 20.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                BlockTags.DIRT);
        add(profiles, new EssenceProfile(15.0F, 10.0F, 35.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                BlockTags.ICE);
        add(profiles, new EssenceProfile(35.0F, 55.0F, 15.0F, 0.0F, 0.0F, 0.0F, 5.0F, 0.0F, 10.0F),
                BlockTags.LOGS);
        add(profiles, new EssenceProfile(10.0F, 75.0F, 20.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                BlockTags.LEAVES);
        add(profiles, new EssenceProfile(70.0F, 0.0F, 0.0F, 20.0F, 20.0F, 0.0F, 0.0F, 0.0F, 20.0F),
                Tags.Blocks.ORES);
        add(profiles, new EssenceProfile(18.0F, 0.0F, 12.0F, 8.0F, 0.0F, 18.0F, 0.0F, 0.0F, 0.0F),
                Tags.Blocks.SANDS);
        add(profiles, new EssenceProfile(18.0F, 0.0F, 20.0F, 10.0F, 4.0F, 18.0F, 0.0F, 0.0F, 0.0F),
                Tags.Blocks.SANDS_RED);
        add(profiles, new EssenceProfile(22.0F, 0.0F, 18.0F, 0.0F, 4.0F, 22.0F, 0.0F, 0.0F, 0.0F),
                Tags.Blocks.GRAVELS);
        add(profiles, new EssenceProfile(12.0F, 18.0F, 15.0F, 0.0F, 0.0F, 10.0F, 0.0F, 0.0F, 0.0F),
                BlockTags.MUD);
        add(profiles, new EssenceProfile(18.0F, 42.0F, 12.0F, 0.0F, 0.0F, 0.0F, 4.0F, 0.0F, 6.0F),
                BlockTags.PLANKS);
        add(profiles, new EssenceProfile(14.0F, 36.0F, 10.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 4.0F),
                BlockTags.WOODEN_SLABS);
        add(profiles, new EssenceProfile(16.0F, 38.0F, 10.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 4.0F),
                BlockTags.WOODEN_STAIRS);
        add(profiles, new EssenceProfile(16.0F, 34.0F, 10.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 4.0F),
                BlockTags.WOODEN_FENCES);
        add(profiles, new EssenceProfile(18.0F, 34.0F, 10.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F, 4.0F),
                BlockTags.FENCE_GATES);
        add(profiles, new EssenceProfile(12.0F, 36.0F, 10.0F, 0.0F, 0.0F, 6.0F, 2.0F, 0.0F, 6.0F),
                BlockTags.WOODEN_DOORS);
        add(profiles, new EssenceProfile(14.0F, 34.0F, 10.0F, 0.0F, 0.0F, 6.0F, 2.0F, 0.0F, 6.0F),
                BlockTags.WOODEN_TRAPDOORS);
        add(profiles, new EssenceProfile(8.0F, 18.0F, 6.0F, 0.0F, 0.0F, 10.0F, 2.0F, 0.0F, 8.0F),
                BlockTags.WOODEN_BUTTONS);
        add(profiles, new EssenceProfile(10.0F, 22.0F, 8.0F, 0.0F, 0.0F, 10.0F, 2.0F, 0.0F, 8.0F),
                BlockTags.WOODEN_PRESSURE_PLATES);
        add(profiles, new EssenceProfile(8.0F, 26.0F, 8.0F, 0.0F, 0.0F, 6.0F, 8.0F, 0.0F, 20.0F),
                BlockTags.STANDING_SIGNS);
        add(profiles, new EssenceProfile(8.0F, 26.0F, 8.0F, 0.0F, 0.0F, 4.0F, 8.0F, 0.0F, 20.0F),
                BlockTags.WALL_SIGNS);
        add(profiles, new EssenceProfile(10.0F, 44.0F, 6.0F, 6.0F, 0.0F, 0.0F, 20.0F, 0.0F, 18.0F),
                Tags.Blocks.BOOKSHELVES);
        add(profiles, new EssenceProfile(18.0F, 38.0F, 10.0F, 0.0F, 0.0F, 0.0F, 8.0F, 0.0F, 12.0F),
                Tags.Blocks.CHESTS, Tags.Blocks.BARRELS);
        add(profiles, new EssenceProfile(40.0F, 70.0F, 15.0F, 35.0F, 0.0F, 0.0F, 8.0F, 0.0F, 0.0F),
                BlockTags.GRASS_BLOCKS);
        add(profiles, new EssenceProfile(8.0F, 0.0F, 0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 10.0F, 28.0F),
                Tags.Blocks.GLASS_BLOCKS);
        add(profiles, new EssenceProfile(6.0F, 0.0F, 0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 8.0F, 24.0F),
                Tags.Blocks.GLASS_PANES);
        add(profiles, new EssenceProfile(72.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 14.0F),
                Tags.Blocks.STORAGE_BLOCKS_IRON);
        add(profiles, new EssenceProfile(66.0F, 0.0F, 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F, 24.0F),
                Tags.Blocks.STORAGE_BLOCKS_GOLD);
        add(profiles, new EssenceProfile(58.0F, 0.0F, 0.0F, 8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 18.0F),
                Tags.Blocks.STORAGE_BLOCKS_COPPER);
        add(profiles, new EssenceProfile(84.0F, 0.0F, 0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 8.0F, 34.0F),
                Tags.Blocks.STORAGE_BLOCKS_DIAMOND);
        add(profiles, new EssenceProfile(70.0F, 0.0F, 0.0F, 14.0F, 0.0F, 0.0F, 0.0F, 6.0F, 28.0F),
                Tags.Blocks.STORAGE_BLOCKS_EMERALD);
        add(profiles, new EssenceProfile(52.0F, 0.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.0F, 10.0F, 26.0F),
                Tags.Blocks.STORAGE_BLOCKS_LAPIS);
        add(profiles, new EssenceProfile(2.0F, 72.0F, 18.0F, 12.0F, 0.0F, 6.0F, 0.0F, 0.0F, 6.0F),
                Tags.Blocks.FLOWERS);
        add(profiles, new EssenceProfile(3.0F, 58.0F, 14.0F, 8.0F, 0.0F, 8.0F, 0.0F, 0.0F, 4.0F),
                BlockTags.SAPLINGS);
        add(profiles, new EssenceProfile(12.0F, 46.0F, 10.0F, 0.0F, 0.0F, 18.0F, 0.0F, 0.0F, 10.0F),
                BlockTags.BAMBOO_BLOCKS);
        return profiles;
    }

    private static Map<Identifier, EssenceProfile> createBlockProfiles() {
        Map<Identifier, EssenceProfile> profiles = new LinkedHashMap<>();

        // ─── README design table blocks (values aligned with the 九大本质 table) ───

        // 草方块: 坚固=中(40), 生命=高(70), 熵增=低(15), 光明=中(35), 心灵=低(8)
        add(profiles, new EssenceProfile(40.0F, 70.0F, 15.0F, 35.0F, 0.0F, 0.0F, 8.0F, 0.0F, 0.0F),
                Blocks.MOSS_BLOCK); // moss_block gets a similar grass-associated profile

        // 石头系: 坚固=高(70), 熵增=中(35)
        add(profiles, new EssenceProfile(70.0F, 0.0F, 35.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                Blocks.STONE, Blocks.COBBLESTONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE);

        // 深板岩: 坚固=高(75), 熵增=中(40), 暗影=高(65), 时空=中(35)
        add(profiles, new EssenceProfile(75.0F, 0.0F, 40.0F, 0.0F, 65.0F, 0.0F, 0.0F, 35.0F, 0.0F),
                Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE);

        // 荧石: 坚固=低(12), 熵增=低(15), 光明=极高(92), 暗影=高(60) — 但 sea_lantern 分开处理
        add(profiles, new EssenceProfile(12.0F, 0.0F, 15.0F, 92.0F, 60.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                Blocks.GLOWSTONE);

        // 海晶灯: 光明=高(80), 共鸣=中(40)
        add(profiles, new EssenceProfile(18.0F, 0.0F, 8.0F, 80.0F, 0.0F, 0.0F, 0.0F, 0.0F, 40.0F),
                Blocks.SEA_LANTERN);

        // 基岩: 坚固=100(无限), 时空=极高(95)
        add(profiles, new EssenceProfile(100.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 95.0F, 0.0F),
                Blocks.BEDROCK);

        // 灵魂沙: 坚固=低(18), 熵增=极高(90), 暗影=高(70), 心灵=高(65), 时空=低(12)
        add(profiles, new EssenceProfile(18.0F, 0.0F, 90.0F, 0.0F, 70.0F, 0.0F, 65.0F, 12.0F, 0.0F),
                Blocks.SOUL_SAND, Blocks.SOUL_SOIL);

        // TNT: 坚固=极低(5), 熵增=极高(88), 光明=低(10), 运动=极高(90), 共鸣=低(8)
        add(profiles, new EssenceProfile(5.0F, 0.0F, 88.0F, 10.0F, 0.0F, 90.0F, 0.0F, 0.0F, 8.0F),
                Blocks.TNT);

        // 紫水晶块: 坚固=中(40), 光明=中(35), 心灵=高(65), 时空=低(18), 共鸣=中(40)
        add(profiles, new EssenceProfile(40.0F, 0.0F, 0.0F, 35.0F, 0.0F, 0.0F, 65.0F, 18.0F, 40.0F),
                Blocks.AMETHYST_BLOCK, Blocks.BUDDING_AMETHYST);

        // 唱片机: 坚固=低(20), 光明=低(15), 运动=低(18), 心灵=中(40), 共鸣=极高(90)
        add(profiles, new EssenceProfile(20.0F, 0.0F, 0.0F, 15.0F, 0.0F, 18.0F, 40.0F, 0.0F, 90.0F),
                Blocks.JUKEBOX);

        // 音符盒: 坚固=低(15), 光明=低(12), 运动=低(10), 心灵=低(15), 共鸣=高(70)
        add(profiles, new EssenceProfile(15.0F, 0.0F, 0.0F, 12.0F, 0.0F, 10.0F, 15.0F, 0.0F, 70.0F),
                Blocks.NOTE_BLOCK);

        // 钟: 坚固=中(30), 共鸣=高(65), 运动=低(10)
        add(profiles, new EssenceProfile(30.0F, 0.0F, 0.0F, 0.0F, 0.0F, 10.0F, 0.0F, 0.0F, 65.0F),
                Blocks.BELL);

        // 下界岩: 坚固=低(22), 熵增=高(65), 光明=低(15), 暗影=中(40)
        add(profiles, new EssenceProfile(22.0F, 0.0F, 65.0F, 15.0F, 40.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                Blocks.NETHERRACK);

        // 末地石: 坚固=高(65), 熵增=低(18), 暗影=高(60), 心灵=低(10), 时空=极高(88), 共鸣=低(15)
        add(profiles, new EssenceProfile(65.0F, 0.0F, 18.0F, 0.0F, 60.0F, 0.0F, 10.0F, 88.0F, 15.0F),
                Blocks.END_STONE);

        // 岩浆块: 坚固=低(20), 熵增=高(65), 光明=高(70), 共鸣=低(10)
        add(profiles, new EssenceProfile(20.0F, 0.0F, 65.0F, 70.0F, 0.0F, 0.0F, 0.0F, 0.0F, 10.0F),
                Blocks.MAGMA_BLOCK);

        // 岩浆（液体）: 熵增=极高(85), 光明=高(70)
        add(profiles, new EssenceProfile(5.0F, 0.0F, 85.0F, 70.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                Blocks.LAVA);

        // 黑曜石: 坚固=极高(88), 时空=高(70)
        add(profiles, new EssenceProfile(88.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 70.0F, 0.0F),
                Blocks.OBSIDIAN);

        // 哭泣的黑曜石: 坚固=极高(85), 时空=高(70), 暗影=中(40)
        add(profiles, new EssenceProfile(85.0F, 0.0F, 0.0F, 0.0F, 40.0F, 0.0F, 0.0F, 70.0F, 0.0F),
                Blocks.CRYING_OBSIDIAN);

        // 海绵: 坚固=低(12), 生命=低(10), 共鸣=高(75)
        add(profiles, new EssenceProfile(12.0F, 10.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 75.0F),
                Blocks.SPONGE, Blocks.WET_SPONGE);

        // 蜂蜜块: 坚固=极低(8), 生命=中(40), 运动=低(15), 共鸣=中(45)
        add(profiles, new EssenceProfile(8.0F, 40.0F, 0.0F, 0.0F, 0.0F, 15.0F, 0.0F, 0.0F, 45.0F),
                Blocks.HONEY_BLOCK);

        // 史莱姆块: 坚固=极低(8), 生命=中(35), 运动=高(70), 共鸣=中(45)
        add(profiles, new EssenceProfile(8.0F, 35.0F, 0.0F, 0.0F, 0.0F, 70.0F, 0.0F, 0.0F, 45.0F),
                Blocks.SLIME_BLOCK);

        // 幽匿催发体: 坚固=低(18), 生命=极高(92), 暗影=极高(90), 心灵=极高(90), 共鸣=高(70)
        // (sculk_catalyst gets the "catalyst" profile; other sculk blocks get reduced values)
        add(profiles, new EssenceProfile(18.0F, 92.0F, 0.0F, 0.0F, 90.0F, 0.0F, 90.0F, 0.0F, 70.0F),
                Blocks.SCULK_CATALYST);

        // 幽匿块: 坚固=低(14), 生命=高(65), 暗影=高(70), 心灵=中(50), 共鸣=中(40)
        add(profiles, new EssenceProfile(14.0F, 65.0F, 0.0F, 0.0F, 70.0F, 0.0F, 50.0F, 0.0F, 40.0F),
                Blocks.SCULK);

        // 幽匿脉络/传感器/尖啸体: less intense versions
        add(profiles, new EssenceProfile(10.0F, 55.0F, 0.0F, 0.0F, 60.0F, 0.0F, 40.0F, 0.0F, 50.0F),
                Blocks.SCULK_VEIN, Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER);

        // 海晶石系列
        add(profiles, new EssenceProfile(30.0F, 0.0F, 0.0F, 30.0F, 0.0F, 0.0F, 80.0F, 10.0F, 30.0F),
                Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.CONDUIT);

        // ─── Redstone / mechanical blocks ───
        add(profiles, new EssenceProfile(20.0F, 0.0F, 0.0F, 20.0F, 0.0F, 20.0F, 0.0F, 0.0F, 90.0F),
                Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_WIRE, Blocks.REPEATER, Blocks.COMPARATOR, Blocks.OBSERVER);

        // ─── Water ───
        add(profiles, new EssenceProfile(0.0F, 15.0F, 0.0F, 0.0F, 0.0F, 70.0F, 0.0F, 10.0F, 15.0F),
                Blocks.WATER, Blocks.BUBBLE_COLUMN);

        // ─── Other functional blocks ───
        add(profiles, new EssenceProfile(26.0F, 8.0F, 8.0F, 0.0F, 0.0F, 6.0F, 0.0F, 0.0F, 4.0F),
                Blocks.CLAY);
        add(profiles, new EssenceProfile(22.0F, 46.0F, 10.0F, 0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 14.0F),
                Blocks.CRAFTING_TABLE);
        add(profiles, new EssenceProfile(56.0F, 0.0F, 12.0F, 8.0F, 4.0F, 0.0F, 8.0F, 0.0F, 10.0F),
                Blocks.FURNACE);
        add(profiles, new EssenceProfile(68.0F, 0.0F, 14.0F, 10.0F, 4.0F, 0.0F, 10.0F, 0.0F, 14.0F),
                Blocks.BLAST_FURNACE);
        add(profiles, new EssenceProfile(24.0F, 38.0F, 16.0F, 10.0F, 0.0F, 0.0F, 6.0F, 0.0F, 8.0F),
                Blocks.SMOKER);
        add(profiles, new EssenceProfile(62.0F, 18.0F, 0.0F, 24.0F, 12.0F, 0.0F, 42.0F, 18.0F, 36.0F),
                Blocks.ENCHANTING_TABLE);
        add(profiles, new EssenceProfile(84.0F, 0.0F, 6.0F, 0.0F, 0.0F, 0.0F, 10.0F, 0.0F, 18.0F),
                Blocks.ANVIL);
        add(profiles, new EssenceProfile(80.0F, 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 8.0F, 0.0F, 16.0F),
                Blocks.CHIPPED_ANVIL);
        add(profiles, new EssenceProfile(76.0F, 0.0F, 20.0F, 0.0F, 0.0F, 0.0F, 6.0F, 0.0F, 14.0F),
                Blocks.DAMAGED_ANVIL);
        add(profiles, new EssenceProfile(38.0F, 0.0F, 0.0F, 48.0F, 0.0F, 0.0F, 26.0F, 20.0F, 42.0F),
                Blocks.BEACON);
        add(profiles, new EssenceProfile(18.0F, 40.0F, 8.0F, 0.0F, 0.0F, 0.0F, 18.0F, 0.0F, 16.0F),
                Blocks.LECTERN);
        add(profiles, new EssenceProfile(10.0F, 0.0F, 0.0F, 4.0F, 18.0F, 0.0F, 0.0F, 10.0F, 24.0F),
                Blocks.TINTED_GLASS);
        add(profiles, new EssenceProfile(58.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 18.0F),
                Blocks.EXPOSED_COPPER);
        add(profiles, new EssenceProfile(54.0F, 0.0F, 14.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 16.0F),
                Blocks.WEATHERED_COPPER);
        add(profiles, new EssenceProfile(50.0F, 0.0F, 20.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 14.0F),
                Blocks.OXIDIZED_COPPER);
        add(profiles, new EssenceProfile(14.0F, 30.0F, 18.0F, 16.0F, 0.0F, 4.0F, 0.0F, 0.0F, 4.0F),
                Blocks.CACTUS);
        add(profiles, new EssenceProfile(2.0F, 36.0F, 26.0F, 2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 8.0F),
                Blocks.BROWN_MUSHROOM);
        add(profiles, new EssenceProfile(2.0F, 40.0F, 30.0F, 4.0F, 14.0F, 0.0F, 0.0F, 0.0F, 10.0F),
                Blocks.RED_MUSHROOM);
        add(profiles, new EssenceProfile(68.0F, 0.0F, 24.0F, 0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 6.0F),
                Blocks.BLACKSTONE);
        add(profiles, new EssenceProfile(62.0F, 0.0F, 16.0F, 0.0F, 8.0F, 0.0F, 0.0F, 0.0F, 4.0F),
                Blocks.BASALT);
        add(profiles, new EssenceProfile(66.0F, 0.0F, 12.0F, 0.0F, 6.0F, 0.0F, 0.0F, 0.0F, 4.0F),
                Blocks.SMOOTH_BASALT);
        add(profiles, new EssenceProfile(58.0F, 0.0F, 22.0F, 0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 8.0F),
                Blocks.NETHER_BRICKS);
        add(profiles, new EssenceProfile(42.0F, 0.0F, 4.0F, 12.0F, 0.0F, 0.0F, 0.0F, 6.0F, 18.0F),
                Blocks.QUARTZ_BRICKS);

        // ─── 紫珀块 (Purpur Block): 坚固=中(40), 光明=低(12), 暗影=中(45), 时空=高(70), 共鸣=中(40) ───
        add(profiles, new EssenceProfile(40.0F, 0.0F, 0.0F, 12.0F, 45.0F, 0.0F, 0.0F, 70.0F, 40.0F),
                Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_SLAB, Blocks.PURPUR_STAIRS);

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
