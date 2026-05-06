package com.essentialism.essener.builders;

import com.essentialism.essener.entry.AttachmentEntry;
import com.mojang.serialization.MapCodec;
import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.builders.AbstractBuilder;
import dev.anvilcraft.lib.v2.registrum.builders.BuilderCallback;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings({"NullableProblems", "unused"})
public class AttachmentBuilder<E,T extends AttachmentType<E>, P> extends AbstractBuilder<AttachmentType<?>,T, P, AttachmentBuilder<E, T, P>> {

    private final AttachmentType.Builder<E> builder;

    public AttachmentBuilder(AbstractRegistrum<?> owner,
                             P parent,
                             String name,
                             BuilderCallback callback,
                             Function<IAttachmentHolder, E> const_) {
        super(owner, parent, name, callback, NeoForgeRegistries.Keys.ATTACHMENT_TYPES);
        builder = AttachmentType.builder(const_);
    }

    public AttachmentBuilder(AbstractRegistrum<?> owner,
                             P parent,
                             String name,
                             BuilderCallback callback,
                             Supplier<E> const_) {
        super(owner, parent, name, callback, NeoForgeRegistries.Keys.ATTACHMENT_TYPES);
        builder = AttachmentType.builder(const_);
    }

    public AttachmentBuilder<E, T, P> serialize(IAttachmentSerializer<E> serializer) {
        builder.serialize(serializer);
        return this;
    }
    public AttachmentBuilder<E, T, P> serialize(MapCodec<E> serializer) {
        builder.serialize(serializer);
        return this;
    }
    public AttachmentBuilder<E, T, P> serialize(MapCodec<E> serializer, Predicate<? super E> predicate) {
        builder.serialize(serializer, predicate);
        return this;
    }

    public AttachmentBuilder<E, T, P> copyOnDeath() {
        builder.copyOnDeath();
        return this;
    }

    public AttachmentBuilder<E, T, P> copyHandler(IAttachmentCopyHandler<E> cloner) {
        builder.copyHandler(cloner);
        return this;
    }
    public AttachmentBuilder<E, T, P> sync(AttachmentSyncHandler<E> syncHandler) {
        builder.sync(syncHandler);
        return this;
    }
    public AttachmentBuilder<E, T, P> sync(StreamCodec<? super RegistryFriendlyByteBuf, E> streamCodec) {
        builder.sync(streamCodec);
        return this;
    }
    public AttachmentBuilder<E, T, P> sync(BiPredicate<IAttachmentHolder, ServerPlayer> sendToPlayer, StreamCodec<? super RegistryFriendlyByteBuf, E> streamCodec) {
        builder.sync(sendToPlayer, streamCodec);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T createEntry() {
        return (T) builder.build();
    }

    @Override
    public AttachmentEntry<T> register() {
        return (AttachmentEntry<T>) super.register();
    }

    @Override
    protected AttachmentEntry<T> createEntryWrapper(DeferredHolder<AttachmentType<?>, T> delegate) {
        return new AttachmentEntry<>(getOwner(), delegate);
    }
}
