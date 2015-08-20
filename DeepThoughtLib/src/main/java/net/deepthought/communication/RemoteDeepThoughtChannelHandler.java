//package net.deepthought.communication;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.util.CharsetUtil;
//import io.netty.util.ReferenceCountUtil;
//
///**
// * Created by ganymed on 19/08/15.
// */
//public class RemoteDeepThoughtChannelHandler extends ChannelInboundHandlerAdapter {
//
//  private final static Logger log = LoggerFactory.getLogger(RemoteDeepThoughtChannelHandler.class);
//
//
//  protected MessageReceivedListener listener = null;
//
//
//  public RemoteDeepThoughtChannelHandler() {
//
//  }
//
//  public RemoteDeepThoughtChannelHandler(MessageReceivedListener listener) {
//    this();
//    this.listener = listener;
//  }
//
//
//  @Override
//  public void channelRead(ChannelHandlerContext ctx, Object msg) {
//    try {
//      if(listener != null) {
//        ByteBuf in = (ByteBuf) msg;
//        String receivedMessage = in.toString(CharsetUtil.UTF_8);
//        if (receivedMessage != null) {
//          listener.messageReceived(receivedMessage);
//        }
//      }
//    } catch(Exception ex) {
//      log.error("Could not read msg", ex);
//    }
//    finally {
//      ReferenceCountUtil.release(msg);
//    }
//  }
//
//  @Override
//  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//    // Close the connection when an exception is raised.
//    cause.printStackTrace();
//    ctx.close();
//  }
//}
