package i.mrhua269.cyanidin.common.communicating.message.w2m;

import i.mrhua269.cyanidin.common.communicating.handler.NettyServerChannelHandlerLayer;
import i.mrhua269.cyanidin.common.communicating.message.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class W2MUpdatePlayerDataRequestMessage implements IMessage<NettyServerChannelHandlerLayer> {
    private byte[] content;
    private UUID playerUUID;

    public W2MUpdatePlayerDataRequestMessage(){}

    public W2MUpdatePlayerDataRequestMessage(UUID playerUUID, byte[] content){
        this.playerUUID = playerUUID;
        this.content = content;
    }

    @Override
    public void writeMessageData(ByteBuf buffer) {
        buffer.writeLong(this.playerUUID.getLeastSignificantBits());
        buffer.writeLong(this.playerUUID.getMostSignificantBits());
        buffer.writeBytes(this.content);
    }

    @Override
    public void readMessageData(ByteBuf buffer) {
        final long lsb = buffer.readLong();
        final long msb = buffer.readLong();
        this.content = new byte[buffer.readableBytes()];
        buffer.readBytes(this.content);

        this.playerUUID = new UUID(lsb, msb);
    }

    @Override
    public void process(NettyServerChannelHandlerLayer handler) {
        handler.savePlayerData(this.playerUUID, this.content);
    }
}