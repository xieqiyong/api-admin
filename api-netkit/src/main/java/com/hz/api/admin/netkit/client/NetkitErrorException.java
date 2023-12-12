/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.client;


import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.packet.PacketError;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public class NetkitErrorException extends Exception {

	private final PacketError error;
	private final Packet packet;

	/**
	 * The request which resulted in the XMPP protocol error response. May be {@code null}.
	 */
	private final Packet request;

	/**
	 * Creates a new XMPPErrorException with the XMPPError that was the root case of the exception.
	 *
	 * @param stanza stanza that contained the exception.
	 * @param error the root cause of the exception.
	 */
	public NetkitErrorException(Packet stanza, PacketError error) {
		this(stanza, error, null);
	}

	/**
	 * Creates a new XMPPErrorException with the XMPPError that was the root case of the exception.
	 *
	 * @param request the request which triggered the error.
	 * @param stanza stanza that contained the exception.
	 * @param error the root cause of the exception.
	 * @since 4.3.0
	 */
	public NetkitErrorException(Packet stanza, PacketError error, Packet request) {
		super();
		this.error = error;
		this.packet = stanza;
		this.request = request;
	}

	public static void ifHasErrorThenThrow(Packet packet) throws NetkitErrorException {
		ifHasErrorThenThrow(packet, null);
	}

	public static void ifHasErrorThenThrow(Packet packet, Packet request) throws NetkitErrorException {
		PacketError packetError = packet.getError();
		if (packetError != null) {
			throw new NetkitErrorException(packet, packetError, request);
		}
	}

	/**
	 * Returns the stanza error extension element associated with this exception.
	 *
	 * @return the stanza error extension element associated with this exception.
	 */
	public PacketError getPacketError() {
		return error;
	}

	/**
	 * Get the request which triggered the error response causing this exception.
	 *
	 * @return the request or {@code null}.
	 * @since 4.3.0
	 */
	public Packet getRequest() {
		return request;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();

		if (packet != null) {
			/*Jid from = packet.getFrom();
			if (from != null) {
				sb.append("XMPP error reply received from " + from + ": ");
			}*/
		}

		sb.append(error);

		if (request != null) {
			sb.append(" as result of the following request: ");
			sb.append(request);
		}

		return sb.toString();
	}
}
