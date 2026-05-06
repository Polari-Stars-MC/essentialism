package com.essentialism.essener.entry;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.advancements.CriterionTrigger;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CriterionEntry<T extends CriterionTrigger<?>> extends RegistryEntry<CriterionTrigger<?>, T> {
    public CriterionEntry(AbstractRegistrum<?> owner, DeferredHolder<CriterionTrigger<?>, T> key) {
        super(owner, key);
    }

    public static <T extends CriterionTrigger<?>> CriterionEntry<T> cast(RegistryEntry<CriterionTrigger<?>, T> entry) {
        return RegistryEntry.cast(CriterionEntry.class, entry);
    }
}
