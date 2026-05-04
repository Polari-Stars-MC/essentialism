package com.essentialism.network;

import com.essentialism.Essentialism;
import com.essentialism.client.render.item.AnalyzerLensClientExtension;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record AnalyzerLensSigilS2CPacket(
        double x,
        double y,
        double z,
        float radius,
        int color,
        int durationTicks
) implements IClientboundPacket {
    public static final CustomPacketPayload.Type<AnalyzerLensSigilS2CPacket> TYPE =
            IPacket.type(Identifier.fromNamespaceAndPath("essentialism", "analyzer_lens_sigil"));
    public static final StreamCodec<ByteBuf, AnalyzerLensSigilS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeDouble(packet.x);
                buffer.writeDouble(packet.y);
                buffer.writeDouble(packet.z);
                buffer.writeFloat(packet.radius);
                buffer.writeInt(packet.color);
                buffer.writeInt(packet.durationTicks);
            },
            buffer -> new AnalyzerLensSigilS2CPacket(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readFloat(),
                    buffer.readInt(),
                    buffer.readInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        AnalyzerLensClientExtension.INSTANCE.addSigil(this.x, this.y, this.z, this.radius, this.color, this.durationTicks);
    }
}
