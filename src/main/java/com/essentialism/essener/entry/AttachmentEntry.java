package com.essentialism.essener.entry;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AttachmentEntry<T extends AttachmentType<?>> extends RegistryEntry<AttachmentType<?>, T> {
    public AttachmentEntry(AbstractRegistrum<?> owner, DeferredHolder<AttachmentType<?>, T> delegate) {
        super(owner, delegate);
    }

    public static <E,T extends AttachmentType<E>> AttachmentEntry<T> cast(RegistryEntry<AttachmentType<?>, T> entry) {
        return RegistryEntry.cast(AttachmentEntry.class, entry);
    }
}
