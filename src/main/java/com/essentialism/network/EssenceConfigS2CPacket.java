package com.essentialism.network;

import com.essentialism.Essentialism;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record EssenceConfigS2CPacket(boolean isPlayerEssence) implements IClientboundPacket {
    public static final CustomPacketPayload.Type<EssenceConfigS2CPacket> TYPE =
            IPacket.type(Identifier.fromNamespaceAndPath("essentialism", "essence_config"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        Essentialism.ESSENCE_CONFIG.isPlayerEssence = this.isPlayerEssence;
    }
}
