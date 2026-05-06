package com.essentialism.advancement;

import com.essentialism.Essentialism;
import com.essentialism.content.data.AnalyzerLensScanData;
import com.essentialism.init.EAttachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public final class AnalyzerLensScanSizeTrigger extends SimpleCriterionTrigger<AnalyzerLensScanSizeTrigger.TriggerInstance> {
    public AnalyzerLensScanSizeTrigger() {}

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        AnalyzerLensScanData scanData = player.getData(EAttachments.ANALYZER_LENS_SCAN);
        int scannedBlockCount = scanData.scannedBlockCount();
        this.trigger(player, instance -> instance.matches(scannedBlockCount));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            MinMaxBounds.Ints scannedBlocks
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("scanned_blocks", MinMaxBounds.Ints.ANY)
                        .forGetter(TriggerInstance::scannedBlocks)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(int scannedBlockCount) {
            return this.scannedBlocks.matches(scannedBlockCount);
        }
    }
}
