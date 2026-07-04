package com.essentialism.content.block.entity;

import com.essentialism.Essentialism;
import dev.anvilcraft.lib.v2.registrum.builders.BlockEntityBuilder;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class EBlockEntities {
    public static final BlockEntityEntry<SimpleAnalyzerBlockEntity> SIMPLE_ANALYZER =
            Essentialism.ESSENER
                    .blockEntity("simple_analyzer", SimpleAnalyzerBlockEntity::new)
                    .register();

    public static void register() {}
}
