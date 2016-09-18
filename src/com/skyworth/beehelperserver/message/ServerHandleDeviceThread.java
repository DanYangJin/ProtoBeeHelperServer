package com.skyworth.beehelperserver.message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.skyworth.beehelperserver.protocol.DeviceMsg.DeviceInfo;
import com.skyworth.beehelperserver.utils.Utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * @author 作者 : DanBin
 * @version 创建时间：2016年9月7日 下午6:39:56 类说明
 */
public class ServerHandleDeviceThread implements Runnable {

	private static final String TAG = "ServerHandleDeviceThread";
	private static final int UDP_RECEIVE_PORT = 12344; // UPD广播端口
	private static final int UDP_SEND_PORT = 12388; // UPD广播端口

	public Context mCtx = null;
	// 服务器接受客户端数据
	private DatagramSocket udpReceiveSocket = null;
	private DatagramPacket udpReceiveData = null;

	// 服务器响应客户端数据
	private DatagramSocket udpSendSocket = null;
	private DatagramPacket udpSendPacket = null;

	// 标记
	private boolean isStarting = true;

	public ServerHandleDeviceThread(Context mCtx) {
		this.mCtx = mCtx;
	}

	@Override
	public void run() {
		try {
			Log.i(TAG, "running......");
			udpReceiveSocket = new DatagramSocket(null);
			udpReceiveSocket.setReuseAddress(true);
			udpReceiveSocket.bind(new InetSocketAddress(UDP_RECEIVE_PORT));
			byte[] data = new byte[256];
			while (isStarting) {
				udpReceiveData = new DatagramPacket(data, 256);
				udpReceiveSocket.receive(udpReceiveData);
				if (udpReceiveData.getLength() != 0) {
					//ProtoBuf数据解析必须保证前后传递字节长度一致
					byte[] realData = new String(data, udpReceiveData.getOffset(), udpReceiveData.getLength())
							.getBytes();
					DeviceInfo clientDevice = DeviceInfo.parseFrom(realData);
					if (clientDevice == null) {
						continue;
					}
					// 响应客户端程序
					Log.d(TAG, "receive data:" + clientDevice.getDeviceIp());
					sendUdpData(clientDevice.getDeviceIp(), getDeviceInfo());
					Thread.sleep(50);
				}
			}
			udpReceiveSocket.close();
		} catch (SocketException e) {
			Log.i(TAG, e.getMessage());
		} catch (IOException e) {
			Log.i(TAG, e.getMessage());
		} catch (InterruptedException e) {
			Log.i(TAG, e.getMessage());
		} finally {
			isStarting = false;
			udpReceiveSocket.close();
		}
	}

	private byte[] getDeviceInfo() {
		DeviceInfo.Builder builder = DeviceInfo.newBuilder();
		builder.setDeviceUid(Utils.getRandomID());
		builder.setDeviceIp(Utils.getEthernetIp());
		builder.setDeviceName(Build.MODEL);
		return builder.build().toByteArray();
	}

	private void sendUdpData(String clientip, byte[] data) throws UnknownHostException, SocketException {
		Log.d(TAG, "sendUdpData:" + data + ",clientip:" + clientip);
		try {
			udpSendSocket = new DatagramSocket();
			udpSendSocket.setReuseAddress(true);
			SocketAddress socketAddr = new InetSocketAddress(clientip, UDP_SEND_PORT);
			udpSendSocket.connect(socketAddr);
			udpSendPacket = new DatagramPacket(data, data.length, socketAddr);
			// 构造数据报包，用来将长度为 length 的包发送到指定主机上的指定端口号
			udpSendSocket.send(udpSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
		try {
			udpSendSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}

}
