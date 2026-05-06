package com.essentialism.essener.builders;

import com.essentialism.essener.entry.CriterionEntry;
import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.builders.AbstractBuilder;
import dev.anvilcraft.lib.v2.registrum.builders.BuilderCallback;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class CriterionBuilder<T extends CriterionTrigger<?>, P> extends AbstractBuilder<CriterionTrigger<?>, T, P, CriterionBuilder<T, P>> {

    private final Supplier<T> supplier;

    public CriterionBuilder(AbstractRegistrum<?> owner, P parent, String name, BuilderCallback callback, Supplier<T> supplier) {
        super(owner, parent, name, callback, Registries.TRIGGER_TYPE);
        this.supplier = supplier;
    }

    @Override
    protected T createEntry() {
        return supplier.get();
    }

    @Override
    public CriterionEntry<T> register() {
        return (CriterionEntry<T>) super.register();
    }

    @Override
    protected CriterionEntry<T> createEntryWrapper(DeferredHolder<CriterionTrigger<?>, T> delegate) {
        return new CriterionEntry<>(getOwner(), delegate);
    }
}
