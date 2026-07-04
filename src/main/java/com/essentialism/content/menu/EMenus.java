package com.essentialism.content.menu;

import com.essentialism.Essentialism;
import com.essentialism.client.screen.AdvancedSpectrometerScreen;
import com.essentialism.client.screen.RuneMatrixScreen;
import com.essentialism.client.screen.SimpleAnalyzerScreen;
import com.essentialism.client.screen.SoulCentrifugeScreen;
import dev.anvilcraft.lib.v2.registrum.util.entry.MenuEntry;

public class EMenus {
    public static final MenuEntry<SimpleAnalyzerMenu> SIMPLE_ANALYZER =
            Essentialism.ESSENER
                    .menu("simple_analyzer",
                            (type, containerId, inventory, buf) -> new SimpleAnalyzerMenu(containerId, inventory),
                            () -> SimpleAnalyzerScreen::new)
                    .register();

    public static final MenuEntry<SoulCentrifugeMenu> SOUL_CENTRIFUGE =
            Essentialism.ESSENER
                    .menu("soul_centrifuge",
                            (type, containerId, inventory, buf) -> new SoulCentrifugeMenu(containerId, inventory),
                            () -> SoulCentrifugeScreen::new)
                    .register();

    public static final MenuEntry<AdvancedSpectrometerMenu> ADVANCED_SPECTROMETER =
            Essentialism.ESSENER
                    .menu("advanced_spectrometer",
                            (type, containerId, inventory, buf) -> new AdvancedSpectrometerMenu(containerId, inventory),
                            () -> AdvancedSpectrometerScreen::new)
                    .register();

    public static final MenuEntry<RuneMatrixMenu> RUNE_MATRIX =
            Essentialism.ESSENER
                    .menu("rune_matrix",
                            (type, containerId, inventory, buf) -> new RuneMatrixMenu(containerId, inventory),
                            () -> RuneMatrixScreen::new)
                    .register();

    public static void register() {}
}
