package com.essentialism.config;

import com.essentialism.Essentialism;
import dev.anvilcraft.lib.v2.config.Comment;
import dev.anvilcraft.lib.v2.config.Config;
import net.neoforged.fml.config.ModConfig;

@Config(name = Essentialism.MOD_ID, type = ModConfig.Type.COMMON)
public class EssenceConfig {
    @Comment("是否可以在玩家上用透镜")
    public boolean isPlayerEssence = true;

}
