package com.hz.api.admin.netkit;


import com.hz.api.admin.netkit.filter.PacketFilter;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public class NoResponseException extends Exception {

	private final PacketFilter filter;

	private NoResponseException(String message) {
		this(message, null);
	}

	private NoResponseException(String message, PacketFilter filter) {
		super(message);
		this.filter = filter;
	}

	public static NoResponseException newWith(long timeout, PacketFilter filter) {
		final StringBuilder sb = getWaitingFor(timeout);

		sb.append(" Waited for response using: ");
		if (filter != null) {
			sb.append(filter);
		} else {
			sb.append("No filter used or filter was 'null'");
		}
		sb.append('.');
		return new NoResponseException(sb.toString(), filter);
	}

	private static StringBuilder getWaitingFor(final long replyTimeout) {
		final StringBuilder sb = new StringBuilder(256);
		//noinspection StringConcatenationInsideStringBufferAppend
		sb.append("No response received within reply timeout. Timeout was " + replyTimeout + "ms (~" + replyTimeout / 1000 + "s).");
		return sb;
	}

	/**
	 * Get the filter that was used to collect the response.
	 *
	 * @return the used filter or <code>null</code>.
	 */
	public PacketFilter getFilter() {
		return filter;
	}
}
