package com.essentialism.content.menu;

import com.essentialism.Essentialism;
import com.essentialism.client.screen.AdvancedSpectrometerScreen;
import com.essentialism.client.screen.RuneMatrixScreen;
import com.essentialism.client.screen.SimpleAnalyzerScreen;
import com.essentialism.client.screen.SoulCentrifugeScreen;
import dev.anvilcraft.lib.v2.registrum.util.entry.MenuEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Shared stillValid check for all Essentialism block entity menus.
     * Returns true if the block entity still exists at its position and the
     * player is within 8 blocks.
     */
    public static boolean stillValid(@Nullable BlockEntity blockEntity, Player player) {
        if (blockEntity == null) return true;
        return blockEntity.getLevel() != null
                && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity
                && player.distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5
                ) <= 64.0;
    }

    /** Called by AnvilLib during static initialization. */
    public static void register() {}
}
