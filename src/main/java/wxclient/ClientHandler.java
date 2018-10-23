package wxclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ChannelHandler.Sharable
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final ScheduledExecutorService scheduledExec = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    private byte[] packageData = null;
    private MsgCallBack callBack;
    private SocketClient client;

    public interface MsgCallBack {
        void msgCallBack(int req, byte[] data);

        void newMsgCallBack();
    }

    public ClientHandler(SocketClient client, MsgCallBack callBack) {
        this.client = client;
        this.callBack = callBack;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        packageData = null;
        client.doConnect();
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf buf) throws Exception {
        byte[] bys = new byte[buf.readableBytes()];
        buf.readBytes(bys);
        int x = bys.length;

        if (packageData != null) {
            byte[] temp = new byte[packageData.length + x];
            System.arraycopy(packageData, 0, temp, 0, packageData.length);
            System.arraycopy(bys, 0, temp, packageData.length, x);
            packageData = temp;
        } else {
            packageData = new byte[x];
            System.arraycopy(bys, 0, packageData, 0, x);
        }

        while (packageData != null && packageData.length > 16) {
            int newPackageLength = CommonUtil.byteArrayToInt(packageData, 0);
            if (newPackageLength <= 0 || newPackageLength > 8000000) {
                log.info("包长度不合法" + newPackageLength);
                log.info(Arrays.toString(bys));
                packageData = null;
            } else if (newPackageLength <= packageData.length) {
                byte[] newPackage = new byte[newPackageLength];
                System.arraycopy(packageData, 0, newPackage, 0, newPackageLength);
                if (newPackageLength < packageData.length) {
                    byte[] temData = new byte[packageData.length - newPackageLength];
                    System.arraycopy(packageData, newPackageLength, temData, 0, packageData.length - newPackageLength);
                    packageData = temData;
                } else {
                    packageData = null;
                }
                handlePackage(newPackage);
            } else {
                break;
            }
        }
    }

    public void handlePackage(byte[] bys) {
        if (bys.length == 20 && bys[3] == 20 && bys[5] == 16 && bys[7] == 1) {
            // 有新消息就会接受到此包
            callBack.newMsgCallBack();
            return;
        } else {
            if (bys.length >= 16 && bys[16] != (byte) 191
                    && !(bys[3] == 58 && bys[5] == 16 && bys[7] == 1 && bys.length == 58)
                    && !(bys[3] == 47 && bys[5] == 16 && bys[7] == 1 && bys.length == 47)) {
//                log.debug(Arrays.toString(bys));
                return;
            }
            scheduledExec.submit(() -> {
                try {
                    int seq = CommonUtil.byteArrayToInt(bys, 12);     // 12 - 16 seq标识
                    callBack.msgCallBack(seq, bys);
                } catch (Exception e) {
                    log.error("handlePackage:scheduledExec", e);
                }
            });
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
                log.info("读超时");
            } else if (e.state() == IdleState.WRITER_IDLE) {
                ctx.close();
                log.info("写超时");
            } else {
                ctx.close();
                log.info("其他超时");
            }
        }
    }
}
