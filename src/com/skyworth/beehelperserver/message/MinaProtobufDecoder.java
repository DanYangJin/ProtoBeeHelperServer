package com.skyworth.beehelperserver.message;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * @Author : DanBin
 * @Date : 2016年9月9日上午9:51:20
 */
public class MinaProtobufDecoder extends CumulativeProtocolDecoder {

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 4) {
			return false;
		}
		in.mark();
		int bodyLength = in.getInt();
		if (in.remaining() < bodyLength) {
			in.reset();
			return false;
		} else {
			byte[] bodyBytes = new byte[bodyLength];
			in.get(bodyBytes);
			out.write(bodyBytes);
			return true;
		}
	}
}
