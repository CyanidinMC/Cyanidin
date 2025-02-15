package gg.earthme.cyanidin.common.communicating;

import gg.earthme.cyanidin.common.communicating.codec.MessageDecoder;
import gg.earthme.cyanidin.common.communicating.codec.MessageEncoder;
import io.netty.channel.Channel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.jetbrains.annotations.NotNull;

public class DefaultChannelPipelineLoader {

    public static void loadDefaultHandlers(@NotNull Channel channel){
        channel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4))
                .addLast(new LengthFieldPrepender(4))
                .addLast(new MessageEncoder())
                .addLast(new MessageDecoder());
    }

}
