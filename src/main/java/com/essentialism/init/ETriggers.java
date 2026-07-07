package com.essentialism.init;

import com.essentialism.advancement.AnalyzerLensScanSizeTrigger;
import com.essentialism.essener.entry.CriterionEntry;

import static com.essentialism.Essentialism.ESSENER;

public final class ETriggers {

    public static final CriterionEntry<AnalyzerLensScanSizeTrigger> ANALYZER_LENS_SCAN_SIZE =
            ESSENER.trigger("analyzer_lens_scan_size", AnalyzerLensScanSizeTrigger::new).register();


    private ETriggers() {}

    /** Called by AnvilLib during static initialization to trigger field registration. */
    public static void register() {
    }
}
