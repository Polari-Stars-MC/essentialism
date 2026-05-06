package com.essentialism.init;

import com.essentialism.content.data.AnalyzerLensScanData;
import java.util.function.Supplier;

import com.essentialism.essener.entry.AttachmentEntry;
import net.neoforged.neoforge.attachment.AttachmentType;

import static com.essentialism.Essentialism.ESSENER;

public final class EAttachments {
    public static final AttachmentEntry<AttachmentType<AnalyzerLensScanData>> ANALYZER_LENS_SCAN = ESSENER
            .attachment("analyzer_lens_scan", (Supplier<AnalyzerLensScanData>) AnalyzerLensScanData::new)
            .serialize(AnalyzerLensScanData.CODEC)
            .copyOnDeath()
            .register();

    private EAttachments() {

    }

    public static void register() {

    }
}
