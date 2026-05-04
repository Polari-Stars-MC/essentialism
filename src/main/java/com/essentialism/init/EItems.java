package com.essentialism.init;

import com.essentialism.Essentialism;
import com.essentialism.content.item.AnalyzerLensItem;
import dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry;

public class EItems {
    public static final ItemEntry<AnalyzerLensItem> ANALYZERS_LENS = Essentialism.REGISTRUM
            .item("analyzers_lens", AnalyzerLensItem::new)
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .lang("Analyzer's Lens")
            .register();

    public static void register() {}
}
