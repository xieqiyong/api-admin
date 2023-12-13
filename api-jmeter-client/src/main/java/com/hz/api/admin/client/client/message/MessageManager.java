package com.hz.api.admin.client.client.message;

import com.hz.api.admin.client.AbstractDataModule;
import com.hz.api.admin.client.ClientStateListener;
import com.hz.api.admin.client.ConfigureHelper;
import com.hz.api.admin.client.DataServerClient;
import com.hz.api.admin.client.utils.ResultCallback;
import com.hz.api.admin.model.enums.ClientState;
import com.hz.api.admin.netkit.listener.ReplyListener;
import com.hz.api.admin.packet.MessagePacket;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 消息转发管理器
 *
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/07.
 */
@Slf4j
public class MessageManager<T> extends AbstractDataModule {

	private final Object                        LOCK         = new Object();
	private       BlockingQueue<MessageWrapper> bufferQueue;
	// 消息转发缓冲队列大小
	private       int                           maxQueueSize = 5000;
	private       int                           threadSize   = Runtime.getRuntime().availableProcessors() * 2;
	private       List<SendTask>                sendTasks;

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		if (maxQueueSize < 1) {
			throw new IllegalArgumentException("MaxQueueSize must be greater than 0");
		}
		this.maxQueueSize = maxQueueSize;
	}

	@Override
	protected void internalInitialize(DataServerClient client) {
		ConfigureHelper.loadToObject(client.getConfiguration(), "messageManager.", this);
		String maxQueueSize = System.getProperty("xcenter.messageManager.maxQueueSize");
		if (StringUtils.isNotBlank(maxQueueSize)) {
			this.maxQueueSize = Integer.parseInt(maxQueueSize);
		}
		String threadSize = System.getProperty("xcenter.messageManager.threadSize");
		if (StringUtils.isNotBlank(threadSize)) {
			this.threadSize = Integer.parseInt(threadSize);
		}
	}

	@Override
	protected void internalStart() {
		bufferQueue = new LinkedBlockingQueue<MessageWrapper>(maxQueueSize);
		sendTasks = new ArrayList<SendTask>(threadSize);
		for (int i = 0; i < threadSize; i++) {
			SendTask sendTask = new SendTask("MessageSendTask-" + i);
			sendTask.start();
			sendTasks.add(sendTask);
		}

		client.addClientStateListener(new ClientStateListener() {
			@Override
			public void stateChanged(DataServerClient client, ClientState newState) {
				if (newState == ClientState.REGISTERED) {
					synchronized (LOCK) {
						LOCK.notifyAll();
					}
				}
			}
		});
	}

	@Override
	protected void internalStop() {
		if (bufferQueue != null) {
			bufferQueue.clear();
		}

		for (SendTask sendTask : sendTasks) {
			sendTask.shutdown();
			sendTask.interrupt();
			// sendTask.notifyAll();
		}
	}

	/**
	 * 发送消息
	 *
	 * @param msg 消息对象
	 */
	public void send(Message msg) {
		send(msg, null, null);
	}

	/**
	 * 发送消息
	 *
	 * @param msg 消息对象
	 * @param callback 消息发送成功后回调接口
	 */
	public void send(Message msg, ResultCallback<T> callback) {
		send(msg, callback, null);
	}

	/**
	 * 发送消息
	 *
	 * @param msg 消息对象
	 * @param callback 消息发送成功后回调接口
	 * @param timeout 等待返回超时时间(单位:毫秒)
	 */
	public void send(Message msg, ResultCallback<T> callback, Long timeout) {
		Objects.requireNonNull(msg, "Message must not be null");
		Objects.requireNonNull(StringUtils.trimToNull(msg.getTopic()), "Message.topic must not be null");
		Objects.requireNonNull(msg.getBody(), "Message.body must not be null");
		if (bufferQueue.size() == maxQueueSize) {
			MessageWrapper rolledOverMessage = bufferQueue.poll();
			if (rolledOverMessage != null) {
				log.warn("消息发送缓冲队列已满, 丢弃积压消息: {}", rolledOverMessage.getMessage());
			}
		}

		bufferQueue.add(new MessageWrapper(msg, callback, timeout));
	}

	/**
	 * 同步发送消息
	 *
	 * @param msg
	 * @param timeout 超时时间，毫秒单位
	 * @throws Exception
	 */
	public void sendSync(Message msg, Long timeout) throws Exception {
		Objects.requireNonNull(msg, "Message must not be null");
		Objects.requireNonNull(timeout, "timeout must not be null");
		Objects.requireNonNull(StringUtils.trimToNull(msg.getTopic()), "Message.topic must not be null");
		Objects.requireNonNull(msg.getBody(), "Message.body must not be null");
		if (!client.isRegistered()) {
			try {
				synchronized (LOCK) {
					LOCK.wait();
				}
			} catch (InterruptedException e) {
				log.warn("Thread blocked of (Waiting for connection) was interrupted");
				throw new SendMessageException("client is not registered and blocked of (Waiting for connection) was interrupted");
			}
		}
		sendMessageToService(msg, null, timeout, true);
	}


	@Data
	class MessageWrapper {

		private Message message;
		private ResultCallback<T> callback;
		private Long timeout;

		public MessageWrapper(Message message, ResultCallback<T> callback, Long timeout) {
			this.message = message;
			this.callback = callback;
			this.timeout = timeout;
		}
	}

	class SendTask extends Thread {

		private volatile boolean shutdown = false;

		public SendTask(String name) {
			super(name);
		}

		public void shutdown() {
			this.shutdown = true;
		}

		@Override
		public void run() {
			while (!this.shutdown) {
				if (!client.isRegistered()) {
					try {
						synchronized (LOCK) {
							LOCK.wait();
						}
					} catch (InterruptedException e) {
						log.warn("Thread blocked of (Waiting for connection) was interrupted");
						break;
					}
				}

				MessageWrapper wrapper;
				try {
					wrapper = bufferQueue.take();
				} catch (InterruptedException e) {
					log.warn("Thread blocked of (bufferQueue.take()) was interrupted");
					break;
				}
				sendMessageToService(wrapper.getMessage(), wrapper.getCallback(), wrapper.getTimeout(), false);
			}
		}
	}

	private void sendMessageToService(Message message, final ResultCallback<T> callback, Long timeout, boolean waitResult) {
		MessagePacket packet = new MessagePacket();
		packet.setTopic(message.getTopic());
		packet.setBody(message.getBody());
		packet.setAcks(callback != null);
		packet.setByteArray(message.isByteArray());
		if (!waitResult) {
			if (callback != null) {
				client.getNetkitClient().sendPacket(packet, packet.createReplyFilter(), new ReplyListener<MessagePacket>() {
					@Override
					public void onPacket(MessagePacket packet) {
						callback.onCompletion((T) packet, null);
					}

					@Override
					public void onFailure(Throwable e) {
						callback.onCompletion(null, e);
					}
				}, timeout);
			} else {
				try {
					client.getNetkitClient().sendPacket(packet).addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) {
							if (!future.isSuccess()) {
								log.error("客户端[{}]发送消息失败: ", client.getClientId(), future.cause());
							}
						}
					});
				} catch (Throwable e) {
					log.error("客户端[{}]发送消息失败: ", client.getClientId(), e);
				}
			}
		} else {
			try {
				packet.setAcks(true);
				client.getNetkitClient().sendPacket(packet, packet.createReplyFilter(), timeout).getOrThrow();
			} catch (Throwable e) {
				log.error("客户端[{}]发送消息失败: ", client.getClientId(), e);
				throw new SendMessageException(e);
			}
		}
	}
}
