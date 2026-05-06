package com.essentialism.essener;

import com.essentialism.essener.builders.AttachmentBuilder;
import com.essentialism.essener.builders.CriterionBuilder;
import dev.anvilcraft.lib.v2.registrum.Registrum;
import net.minecraft.advancements.CriterionTrigger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Essener extends Registrum {

    private static final Logger LOGGER = LogManager.getLogger(Essener.class);

    protected Essener(String modid) {
        super(modid);
    }

    public <E> AttachmentBuilder<E, AttachmentType<E>, Essener> attachment(String name, Function<IAttachmentHolder, E> const_) {
        return entry(name, callback -> new AttachmentBuilder<>(this, this, name, callback, const_));
    }
    public <E> AttachmentBuilder<E, AttachmentType<E>, Essener> attachment(String name, Supplier<E> const_) {
        return entry(name, callback -> new AttachmentBuilder<>(this, this, name, callback, const_));
    }

    public <T extends CriterionTrigger<?>> CriterionBuilder<T, Essener> trigger(String name, Supplier<T> supplier) {
        return entry(name, callback -> new CriterionBuilder<>(this, this, name, callback, supplier));
    }


    public static Essener create(String modid) {
        var ret = new Essener(modid);
        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modid).map(ModContainer::getEventBus);
        modEventBus.ifPresentOrElse(ret::registerEventListeners, () -> {
            String message = "# [Essener] Failed to register eventListeners for mod " + modid + ", This should be reported to this mod's dev #";
            StringBuilder hashtags = new StringBuilder().repeat("#", message.length());
            LOGGER.fatal(hashtags.toString());
            LOGGER.fatal(message);
            LOGGER.fatal(hashtags.toString());
        });
        return ret;
    }
}
