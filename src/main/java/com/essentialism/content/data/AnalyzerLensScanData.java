package com.essentialism.content.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;

public final class AnalyzerLensScanData {
    public static final MapCodec<AnalyzerLensScanData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.listOf().optionalFieldOf("scanned_blocks", List.of()).forGetter(AnalyzerLensScanData::scannedBlocks)
    ).apply(instance, AnalyzerLensScanData::new));

    private final LinkedHashSet<Identifier> scannedBlocks;

    public AnalyzerLensScanData() {
        this(List.of());
    }

    public AnalyzerLensScanData(List<Identifier> scannedBlocks) {
        this.scannedBlocks = new LinkedHashSet<>(scannedBlocks);
    }

    public boolean recordBlock(Identifier blockId) {
        if (this.scannedBlocks.contains(blockId)) return false;
        return this.scannedBlocks.add(blockId);
    }

    public boolean hasScanned(Identifier blockId) {
        return this.scannedBlocks.contains(blockId);
    }

    public int scannedBlockCount() {
        return this.scannedBlocks.size();
    }

    public List<Identifier> scannedBlocks() {
        return List.copyOf(this.scannedBlocks);
    }

    public Set<Identifier> scannedBlockSet() {
        return Set.copyOf(this.scannedBlocks);
    }
}
