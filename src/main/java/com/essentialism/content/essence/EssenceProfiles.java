package com.essentialism.content.essence;

import com.essentialism.Essentialism;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class EssenceProfiles {
    private static final String BLOCK_RESOURCE_ROOT = "essentialism/block_essence_profiles";
    private static final String ENTITY_RESOURCE_ROOT = "essentialism/entity_essence_profiles";
    private static final String BLOCK_PREFIX = "blocks/";
    private static final String ENTITY_PREFIX = "entities/";
    private static final String TAG_PREFIX = "tags/";
    private static final EssenceProfile EMPTY = new EssenceProfile(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    private static final List<BlockRule> BLOCK_RULES = new ArrayList<>();
    private static final List<EntityRule> ENTITY_RULES = new ArrayList<>();
    public static final BlockCapability<EssenceProfile, Void> BLOCK_ESSENCE_PROFILE = BlockCapability.createVoid(
            Identifier.fromNamespaceAndPath(Essentialism.MOD_ID, "block_essence_profile"),
            EssenceProfile.class
    );
    public static final EntityCapability<EssenceProfile, Void> ENTITY_ESSENCE_PROFILE = EntityCapability.createVoid(
            Identifier.fromNamespaceAndPath(Essentialism.MOD_ID, "entity_essence_profile"),
            EssenceProfile.class
    );

    private EssenceProfiles() {}

    public static void register(RegisterCapabilitiesEvent event) {
        Block[] blocks = BuiltInRegistries.BLOCK.stream().toArray(Block[]::new);
        event.registerBlock(BLOCK_ESSENCE_PROFILE,
                (level, pos, state, blockEntity, context) -> resolveBlockProfile(state.getBlock()),
                blocks);
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            registerEntityCapability(event, entityType);
        }
    }

    public static @Nullable EssenceProfile get(Level level, BlockPos pos) {
        return level.getCapability(BLOCK_ESSENCE_PROFILE, pos, null);
    }

    public static @Nullable EssenceProfile get(LivingEntity entity) {
        return entity.getCapability(ENTITY_ESSENCE_PROFILE, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerEntityCapability(RegisterCapabilitiesEvent event, EntityType<?> entityType) {
        event.registerEntity(ENTITY_ESSENCE_PROFILE, (EntityType) entityType, EssenceProfiles::getEntityCapability);
    }

    private static @Nullable EssenceProfile getEntityCapability(Entity entity, Void context) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }

        EssenceProfile exact = resolveExactEntityProfile(livingEntity.getType());
        if (exact != null) {
            return exact;
        }
        return fallbackEntityProfile(livingEntity);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        reload(event.getServer());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        reload(event.getPlayerList().getServer());
    }

    private static void reload(MinecraftServer server) {
        reloadBlockProfiles(server);
        reloadEntityProfiles(server);
    }

    private static void reloadBlockProfiles(MinecraftServer server) {
        ResourceManager resourceManager = server.getResourceManager();
        List<BlockRule> rules = new ArrayList<>();
        Map<Identifier, List<Resource>> resources = resourceManager.listResourceStacks(
                BLOCK_RESOURCE_ROOT,
                id -> id.getPath().endsWith(".json")
        );
        resources.entrySet().stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<Identifier, List<Resource>> entry) -> parseBlockTarget(entry.getKey()).kind().sortOrder())
                        .thenComparing(entry -> entry.getKey().toString()))
                .forEach(entry -> applyBlockResourceStack(rules, entry.getKey(), entry.getValue()));
        BLOCK_RULES.clear();
        BLOCK_RULES.addAll(rules);
    }

    private static void reloadEntityProfiles(MinecraftServer server) {
        ResourceManager resourceManager = server.getResourceManager();
        List<EntityRule> rules = new ArrayList<>();
        Map<Identifier, List<Resource>> resources = resourceManager.listResourceStacks(
                ENTITY_RESOURCE_ROOT,
                id -> id.getPath().endsWith(".json")
        );
        resources.entrySet().stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<Identifier, List<Resource>> entry) -> parseEntityTarget(entry.getKey()).kind().sortOrder())
                        .thenComparing(entry -> entry.getKey().toString()))
                .forEach(entry -> applyEntityResourceStack(rules, entry.getKey(), entry.getValue()));
        ENTITY_RULES.clear();
        ENTITY_RULES.addAll(rules);
    }

    private static void applyBlockResourceStack(
            List<BlockRule> rules,
            Identifier resourceId,
            List<Resource> stack
    ) {
        BlockProfileTarget target = parseBlockTarget(resourceId);
        for (Resource resource : stack) {
            BlockEssenceProfileDefinition definition;
            try (BufferedReader reader = resource.openAsReader()) {
                definition = BlockEssenceProfileDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .getOrThrow(error -> new IllegalStateException("Failed to parse " + resourceId + ": " + error));
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to load essence profile from " + resource.sourcePackId(), exception);
            }
            rules.add(new BlockRule(target, definition));
        }
    }

    private static @Nullable EssenceProfile resolveBlockProfile(Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        EssenceProfile profile = null;
        for (BlockRule rule : BLOCK_RULES) {
            if (!rule.matches(block, blockId)) {
                continue;
            }
            EssenceProfile base = rule.definition().replace() || profile == null ? EMPTY : profile;
            profile = rule.definition().profile().apply(base);
        }
        return profile;
    }

    private static @Nullable EssenceProfile resolveExactEntityProfile(EntityType<?> type) {
        EssenceProfile profile = null;
        for (EntityRule rule : ENTITY_RULES) {
            if (!rule.matches(type)) {
                continue;
            }
            EssenceProfile base = rule.definition().replace() || profile == null ? EMPTY : profile;
            profile = rule.definition().profile().apply(base);
        }
        return profile;
    }

    private static void applyEntityResourceStack(
            List<EntityRule> rules,
            Identifier resourceId,
            List<Resource> stack
    ) {
        EntityProfileTarget target = parseEntityTarget(resourceId);
        if (target.kind() == EntityTargetKind.ENTITY && !BuiltInRegistries.ENTITY_TYPE.containsKey(target.id())) {
            throw new IllegalStateException("Unknown entity id in essence profile path: " + target.id());
        }
        for (Resource resource : stack) {
            EntityEssenceProfileDefinition definition;
            try (BufferedReader reader = resource.openAsReader()) {
                definition = EntityEssenceProfileDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .getOrThrow(error -> new IllegalStateException("Failed to parse " + resourceId + ": " + error));
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to load entity essence profile from " + resource.sourcePackId(), exception);
            }
            rules.add(new EntityRule(target, definition));
        }
    }

    private static BlockProfileTarget parseBlockTarget(Identifier resourceId) {
        String prefix = BLOCK_RESOURCE_ROOT + "/";
        String path = resourceId.getPath();
        if (!path.startsWith(prefix) || !path.endsWith(".json")) {
            throw new IllegalStateException("Invalid essence profile path: " + resourceId);
        }

        String relative = path.substring(prefix.length(), path.length() - ".json".length());
        if (relative.startsWith(TAG_PREFIX)) {
            return new BlockProfileTarget(
                    BlockTargetKind.TAG,
                    Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative.substring(TAG_PREFIX.length()))
            );
        }
        if (relative.startsWith(BLOCK_PREFIX)) {
            return new BlockProfileTarget(
                    BlockTargetKind.BLOCK,
                    Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative.substring(BLOCK_PREFIX.length()))
            );
        }
        if (relative.isEmpty()) {
            throw new IllegalStateException("Invalid essence profile path: " + resourceId);
        }
        return new BlockProfileTarget(BlockTargetKind.BLOCK, Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative));
    }

    private static EntityProfileTarget parseEntityTarget(Identifier resourceId) {
        String prefix = ENTITY_RESOURCE_ROOT + "/";
        String path = resourceId.getPath();
        if (!path.startsWith(prefix) || !path.endsWith(".json")) {
            throw new IllegalStateException("Invalid entity essence profile path: " + resourceId);
        }
        String relative = path.substring(prefix.length(), path.length() - ".json".length());
        if (relative.startsWith(TAG_PREFIX)) {
            return new EntityProfileTarget(
                    EntityTargetKind.TAG,
                    Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative.substring(TAG_PREFIX.length()))
            );
        }
        if (relative.startsWith(ENTITY_PREFIX)) {
            return new EntityProfileTarget(
                    EntityTargetKind.ENTITY,
                    Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative.substring(ENTITY_PREFIX.length()))
            );
        }
        if (relative.isEmpty()) {
            throw new IllegalStateException("Invalid entity essence profile path: " + resourceId);
        }
        return new EntityProfileTarget(EntityTargetKind.ENTITY, Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative));
    }

    private static @Nullable EssenceProfile fallbackEntityProfile(LivingEntity entity) {
        if (entity instanceof Player) {
            return resolveExactEntityProfile(EntityType.PLAYER);
        }
        if (entity instanceof EnderDragon) {
            return resolveExactEntityProfile(EntityType.ENDER_DRAGON);
        }
        if (entity instanceof WitherBoss) {
            return resolveExactEntityProfile(EntityType.WITHER);
        }
        if (entity instanceof Warden) {
            return resolveExactEntityProfile(EntityType.WARDEN);
        }
        if (entity instanceof Animal) {
            return new EssenceProfile(18.0F, 52.0F, 6.0F, 4.0F, 0.0F, 22.0F, 8.0F, 0.0F, 12.0F);
        }
        if (entity instanceof Enemy) {
            return new EssenceProfile(20.0F, 0.0F, 26.0F, 0.0F, 30.0F, 26.0F, 10.0F, 6.0F, 10.0F);
        }
        return null;
    }

    private record BlockRule(BlockProfileTarget target, BlockEssenceProfileDefinition definition) {
        private boolean matches(Block block, Identifier blockId) {
            if (this.target.kind() == BlockTargetKind.BLOCK) {
                return this.target.id().equals(blockId);
            }
            return BuiltInRegistries.BLOCK.wrapAsHolder(block).is(TagKey.create(Registries.BLOCK, this.target.id()));
        }
    }

    private record EntityRule(EntityProfileTarget target, EntityEssenceProfileDefinition definition) {
        private boolean matches(EntityType<?> type) {
            if (this.target.kind() == EntityTargetKind.ENTITY) {
                return this.target.id().equals(BuiltInRegistries.ENTITY_TYPE.getKey(type));
            }
            return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).is(TagKey.create(Registries.ENTITY_TYPE, this.target.id()));
        }
    }

    private record BlockProfileTarget(BlockTargetKind kind, Identifier id) {}

    private record EntityProfileTarget(EntityTargetKind kind, Identifier id) {}

    private enum BlockTargetKind {
        TAG(0),
        BLOCK(1);

        private final int sortOrder;

        BlockTargetKind(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public int sortOrder() {
            return this.sortOrder;
        }
    }

    private enum EntityTargetKind {
        TAG(0),
        ENTITY(1);

        private final int sortOrder;

        EntityTargetKind(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public int sortOrder() {
            return this.sortOrder;
        }
    }
}
