/*
 * Created by zhangzxiang91@gmail.com on 2021/05/07.
 */
package com.hz.api.admin.netkit.server;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端连接管理器
 *
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/07.
 */
@Slf4j
public class ConnectionManager {

	private final AtomicInteger                 connectionsCounter   = new AtomicInteger(0);
	private final Set<ConnectionListener>       connectionListeners  = new CopyOnWriteArraySet<ConnectionListener>();
	private final Map<String, NetkitConnection> channelIdConnections = new ConcurrentHashMap<String, NetkitConnection>();
	private final ExecutorService               packetProcessExecutor;
	private final ExecutorService               connectionNotifyExecutor;

	public ConnectionManager(ExecutorService processPacketExecutor, ExecutorService connectionNotifyExecutor) {
		this.packetProcessExecutor = processPacketExecutor;
		this.connectionNotifyExecutor = connectionNotifyExecutor;
	}

	public NetkitConnection createConnection(Channel channel) {
		NetkitConnection connection = new NetkitConnection(channel, packetProcessExecutor);
		// 注册连接关闭监听器，连接关闭时从当前关系表删除连接信息
		connection.registerCloseListener(new ConnectionCloseListener<NetkitConnection>() {
			@Override
			public void onConnectionClose(NetkitConnection handback) {
				removeConnection(handback);
			}
		}, connection);
		channelIdConnections.put(channel.id().asLongText(), connection);
		connectionsCounter.incrementAndGet();
		log.info("CreatedConnection: channel=[id:{} addr:{}] connections={}", connection.getChannel().id(), connection.getChannel().remoteAddress(),
				connectionsCounter.get());
		invokeCreatedNotifyListeners(connection);
		return connection;
	}

	private void removeConnection(NetkitConnection connection) {
		boolean removed = channelIdConnections.remove(connection.getChannelId()) != null;
		if (removed) {
			connectionsCounter.decrementAndGet();
			log.info("RemovedConnection: channel=[id:{} addr:{}] connections={}", connection.getChannel().id(),
					connection.getChannel().remoteAddress(), connectionsCounter.get());
			invokeRemovedNotifyListeners(connection);
		}
	}

	private void invokeCreatedNotifyListeners(final NetkitConnection connection) {
		connectionNotifyExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners) {
					try {
						listener.created(connection);
					} catch (Throwable e) {
						// log.error("Error notifying listener: " + entry.getKey(), e);
						log.warn("An exception was thrown by {}.created() error: {}", listener.getClass().getName(), e.getMessage());
					}
				}
			}
		});
	}

	private void invokeRemovedNotifyListeners(final NetkitConnection connection) {
		connectionNotifyExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners) {
					try {
						listener.removed(connection);
					} catch (Throwable e) {
						log.warn("An exception was thrown by {}.removed() error: {}", listener.getClass().getName(), e.getMessage());
					}
				}
			}
		});
	}

	public NetkitConnection getConnection(String channelId) {
		return channelIdConnections.get(channelId);
	}

	public Set<ConnectionListener> getConnectionListeners() {
		return connectionListeners;
	}

	public void addConnectionListener(final ConnectionListener connectionListener) {
		if (connectionListener == null) {
			return;
		}

		connectionListeners.add(connectionListener);
		if (!channelIdConnections.isEmpty()) {
			final List<NetkitConnection> connections = new ArrayList<NetkitConnection>(channelIdConnections.values());
			connectionNotifyExecutor.execute(new Runnable() {
				@Override
				public void run() {
					for (NetkitConnection connection : connections) {
						try {
							connectionListener.created(connection);
						} catch (Throwable e) {
							// log.error("Error notifying listener: " + entry.getKey(), e);
							log.warn("An exception was thrown by {}.created() error: {}", connectionListener.getClass().getName(), e.getMessage());
						}
					}
				}
			});
		}
	}

	public void removeConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
	}

}
