package com.skyworth.beehelperserver.message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.google.protobuf.InvalidProtocolBufferException;
import com.skyworth.beehelperserver.protocol.HeartBeatMsg.HeartBeat;
import com.skyworth.beehelperserver.utils.Constants;

import android.content.Context;
import android.util.Log;

/**
 * @Author : DanBin
 * @Date : 2016年9月8日下午5:13:49
 */
public class ServerSession {

	private static final String TAG = "ServerSession";

	private static ServerSession INSTANCE = null;

	private Context mCtx = null;
	private HashMap<String, IoSession> mSessionMap = null;

	public static ServerSession getInstance(Context mCtx) {
		if (INSTANCE == null) {
			synchronized (ServerSession.class) {
				if (INSTANCE == null) {
					INSTANCE = new ServerSession(mCtx);
				}
			}
		}
		return INSTANCE;
	}

	public ServerSession(Context mCtx) {
		this.mCtx = mCtx;
		this.mSessionMap = new HashMap<String, IoSession>();
	}

	public void addClientSession(String remoteInfo, IoSession mSession) {
		synchronized (mSessionMap) {
			if (mSessionMap == null || mSessionMap.containsKey(remoteInfo)) {
				return;
			}
			mSessionMap.put(remoteInfo, mSession);
		}
	}

	public void removeClientSession(String remoteInfo) {
		synchronized (mSessionMap) {
			if (mSessionMap == null || !mSessionMap.containsKey(remoteInfo)) {
				return;
			}
			mSessionMap.remove(remoteInfo);
		}
	}

	public void closeClientSession() {
		synchronized (mSessionMap) {
			if (mSessionMap == null || mSessionMap.isEmpty()) {
				return;
			}
			Set<Entry<String, IoSession>> entrySet = mSessionMap.entrySet();
			for (Entry<String, IoSession> entry : entrySet) {
				entry.getValue().closeNow();
			}
		}
	}

	// 必须初始化所有线程
	public void initConnection() {
		new Thread(new ServerHandleDeviceThread(mCtx)).start();
		new MinaServerThread().start();
	}

	// 关闭现有连接
	public void closeConnection() {
		closeClientSession();
	}

	/**
	 * 客户端发送TCP消息方法
	 * 
	 * @param msg
	 *            : 消息内容
	 */
	public void send(byte[] msg) {
		Log.i(TAG, "msg : " + msg);
		if (mSessionMap == null || mSessionMap.isEmpty()) {
			return;
		}
		Set<Entry<String, IoSession>> entrySet = mSessionMap.entrySet();
		for (Entry<String, IoSession> entry : entrySet) {
			entry.getValue().write(msg);
		}
	}

	public class MinaServerThread extends Thread {
		@Override
		public void run() {
			try {
				NioSocketAcceptor acceptor = new NioSocketAcceptor();
				// 协议解析，采用mina现成的UTF-8字符串处理方式
				acceptor.getFilterChain().addLast("mychin", new ProtocolCodecFilter(new MinaProtobufEncoder(), new MinaProtobufDecoder()));// 过滤消息
				acceptor.getFilterChain().addLast("heartbeat", buildHeartBeatBody());
				acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, Constants.IDELTIMEOUT);
				acceptor.setHandler(new ServerIoHandler(mCtx)); // 处理
				acceptor.setReuseAddress(true);
				acceptor.bind(new InetSocketAddress(Constants.TCP_PORT));
				Log.d("info", "mina消息接收服务器已经启动...");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("info", "启动Socket异常" + e.getMessage());
			}
		}
	}

	private KeepAliveFilter buildHeartBeatBody() {
		KeepAliveMessageFactory heartBeatFactory = new KeepAliveMessageFactoryImpl();
		KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory, IdleStatus.READER_IDLE,
				KeepAliveRequestTimeoutHandlerImpl.CLOSE);
		heartBeat.setForwardEvent(true);
		heartBeat.setRequestInterval(Constants.HEARTBEATRATE);
		heartBeat.setRequestTimeout(Constants.HEARTTIMEOUT);
		return heartBeat;
	}

	public static class KeepAliveMessageFactoryImpl implements KeepAliveMessageFactory {

		/* 心跳包发送内容 */
		public HeartBeat HEARTBEATREQUEST;
		/* 心跳包相应内容 */
		public HeartBeat HEARTBEATRESPONSE;

		public KeepAliveMessageFactoryImpl() {
			HeartBeat.Builder builder1 = HeartBeat.newBuilder();
			builder1.setMsg(1);
			HEARTBEATREQUEST = builder1.build();
			HeartBeat.Builder builder2 = HeartBeat.newBuilder();
			builder2.setMsg(2);
			HEARTBEATRESPONSE = builder2.build();
		}

		@Override
		public Object getRequest(IoSession session) {
			return null;
		}

		@Override
		public Object getResponse(IoSession session, Object request) {
			return HEARTBEATRESPONSE.toByteArray();
		}

		/**
		 * 如果接受的长度为2,则表示心跳请求
		 * 
		 * */
		@Override
		public boolean isRequest(IoSession session, Object message) {
			try {
				byte[] msg = (byte[]) message;
				if (msg.length == 2) {
					HeartBeat heartBeat = HeartBeat.parseFrom(msg);
					int value = heartBeat.getMsg();
					if (value == 1) {
						return true;
					}
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			return false;
		}

		/**
		 * 如果接受的长度为2,则表示心跳响应
		 * 
		 * */
		@Override
		public boolean isResponse(IoSession session, Object message) {
			try {
				byte[] msg = (byte[]) message;
				if (msg.length == 2) {
					Log.i(TAG, "@@@isResponse is heartbeat@@@");
					HeartBeat heartBeat = HeartBeat.parseFrom(msg);
					int value = heartBeat.getMsg();
					if (value == 2) {
						return true;
					}
				}				
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			return false;
		}

	}

	public static class KeepAliveRequestTimeoutHandlerImpl implements KeepAliveRequestTimeoutHandler {

		@Override
		public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
			Log.i(TAG, "心跳超时！");
		}

	}

}
