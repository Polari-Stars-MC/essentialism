package com.essentialism.network;

import com.essentialism.Essentialism;
import com.essentialism.client.render.item.AnalyzerLensClientExtension;
import com.essentialism.config.EssenceConfig;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record EssenceConfigS2CPacket(boolean isPlayerEssence) implements IClientboundPacket {
    public static final CustomPacketPayload.Type<EssenceConfigS2CPacket> TYPE =
            IPacket.type(Identifier.fromNamespaceAndPath("essentialism", "essence_config"));
    public static final StreamCodec<ByteBuf, EssenceConfigS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeBoolean(packet.isPlayerEssence),
            buffer -> new EssenceConfigS2CPacket(buffer.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        Essentialism.ESSENCE_CONFIG.isPlayerEssence = this.isPlayerEssence;
    }
}
