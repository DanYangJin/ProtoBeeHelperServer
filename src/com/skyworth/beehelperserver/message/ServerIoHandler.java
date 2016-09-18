package com.skyworth.beehelperserver.message;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.skyworth.beehelperserver.protocol.RequestInfoMsg.RequestInfo;

import android.content.Context;
import android.util.Log;

/**
 * @Author : DanBin
 * @Date : 2016年9月8日下午5:41:02
 */
public class ServerIoHandler extends IoHandlerAdapter{


	private static final String TAG = "ServerIoHandler";

	private Context mCtx = null;
	
	public ServerIoHandler(Context mCtx) {
		this.mCtx = mCtx;
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		super.exceptionCaught(session, cause);
		Log.e(TAG, "@@@exceptionCaught@@@" + cause.getMessage());
		ServerSession.getInstance(mCtx).removeClientSession(session.getRemoteAddress().toString());
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		super.inputClosed(session);
		Log.e(TAG, "@@@inputClosed@@@");
	}

	@Override
	public void messageReceived(IoSession session, Object object) throws Exception {
		super.messageReceived(session, object);
		byte[] msg = (byte[])object;
		Log.i("info", " received length : " + msg.length);
		RequestInfo requestInfo = RequestInfo.parseFrom(msg);
		Log.i(TAG, " msg : " + requestInfo.toString());
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
		Log.e(TAG, "@@@messageSent@@@");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		Log.e(TAG, "@@@sessionClosed@@@");
		ServerSession.getInstance(mCtx).removeClientSession(session.getRemoteAddress().toString());
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
		Log.e(TAG, "@@@sessionCreated@@@");
		ServerSession.getInstance(mCtx).addClientSession(session.getRemoteAddress().toString(), session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
		Log.e(TAG, "@@@sessionIdle@@@");
		
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		Log.e(TAG, "@@@sessionOpened@@@");
	}
	
}
