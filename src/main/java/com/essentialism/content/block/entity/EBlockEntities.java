package com.essentialism.content.block.entity;

import com.essentialism.Essentialism;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;

public class EBlockEntities {
    public static final BlockEntityEntry<SimpleAnalyzerBlockEntity> SIMPLE_ANALYZER =
            Essentialism.ESSENER
                    .blockEntity("simple_analyzer", SimpleAnalyzerBlockEntity::new)
                    .register();

    public static final BlockEntityEntry<SoulCentrifugeBlockEntity> SOUL_CENTRIFUGE =
            Essentialism.ESSENER
                    .blockEntity("soul_centrifuge", SoulCentrifugeBlockEntity::new)
                    .register();

    public static final BlockEntityEntry<AdvancedSpectrometerBlockEntity> ADVANCED_SPECTROMETER =
            Essentialism.ESSENER
                    .blockEntity("advanced_spectrometer", AdvancedSpectrometerBlockEntity::new)
                    .register();

    public static final BlockEntityEntry<RuneMatrixBlockEntity> RUNE_MATRIX =
            Essentialism.ESSENER
                    .blockEntity("rune_matrix", RuneMatrixBlockEntity::new)
                    .register();

    public static final BlockEntityEntry<EssenceResonanceMatrixBlockEntity> ESSENCE_RESONANCE_MATRIX =
            Essentialism.ESSENER
                    .blockEntity("essence_resonance_matrix", EssenceResonanceMatrixBlockEntity::new)
                    .register();

    /** Called by AnvilLib during static initialization. */
    public static void register() {}
}
