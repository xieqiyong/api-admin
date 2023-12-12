package com.hz.api.admin.netkit;

import java.util.concurrent.*;

public abstract class NetkitFuture<V, E extends Throwable> implements Future<V> {

	protected V       result;
	protected E       exception;
	private   boolean cancelled;

	@Override
	public final synchronized boolean cancel(boolean mayInterruptIfRunning) {
		if (isDone()) {
			return false;
		}

		cancelled = true;
		if (mayInterruptIfRunning) {
			notifyAll();
		}
		return true;
	}

	@Override
	public final synchronized boolean isCancelled() {
		return cancelled;
	}

	@Override
	public final synchronized boolean isDone() {
		return result != null || exception != null || cancelled;
	}

	private V getOrThrowExecutionException() throws ExecutionException {
		if (result != null) {
			return result;
		}
		if (exception != null) {
			throw new ExecutionException(exception);
		}

		throw new CancellationException();
	}

	@Override
	public final synchronized V get() throws InterruptedException, ExecutionException {
		while (result == null && exception == null && !cancelled) {
			futureWait();
		}

		return getOrThrowExecutionException();
	}

	public final synchronized V getOrThrow() throws E, InterruptedException {
		while (result == null && exception == null && !cancelled) {
			futureWait();
		}

		if (exception != null) {
			throw exception;
		}

		if (cancelled) {
			throw new CancellationException();
		}

		return result;
	}

	@Override
	public final synchronized V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		/*final long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
		while (result != null && exception != null) {
			final long waitTimeRemaining = deadline - System.currentTimeMillis();
			if (waitTimeRemaining > 0) {
				futureWait(waitTimeRemaining);
			}
		}*/
		futureWait(unit.toMillis(timeout));

		if (cancelled) {
			throw new CancellationException();
		}

		if (result == null || exception == null) {
			throw new TimeoutException();
		}

		return getOrThrowExecutionException();
	}

	public V getIfAvailable() {
		return result;
	}

	protected final void futureWait() throws InterruptedException {
		futureWait(0);
	}

	@SuppressWarnings("WaitNotInLoop")
	protected void futureWait(long timeout) throws InterruptedException {
		wait(timeout);
	}

	public static class InternalNetkitFuture<V, E extends Throwable> extends NetkitFuture<V, E> {

		public final synchronized void setResult(V result) {
			this.result = result;
			this.notifyAll();
		}

		public final synchronized void setException(E exception) {
			this.exception = exception;
			this.notifyAll();
		}
	}

	// public static class SocketFuture extends InternalSmackFuture<Socket, IOException> {
	// 	private final Socket socket;
	//
	// 	private final Object wasInterruptedLock = new Object();
	//
	// 	private boolean wasInterrupted;
	//
	// 	public SocketFuture(SocketFactory socketFactory) throws IOException {
	// 		socket = socketFactory.createSocket();
	// 	}
	//
	// 	@Override
	// 	protected void futureWait(long timeout) throws InterruptedException {
	// 		try {
	// 			super.futureWait(timeout);
	// 		} catch (InterruptedException interruptedException) {
	// 			synchronized (wasInterruptedLock) {
	// 				wasInterrupted = true;
	// 				if (!socket.isClosed()) {
	// 					closeSocket();
	// 				}
	// 			}
	// 			throw interruptedException;
	// 		}
	// 	}
	//
	// 	// public void connectAsync(final SocketAddress socketAddress, final int timeout) {
	// 	// 	AbstractXMPPConnection.asyncGo(new Runnable() {
	// 	// 		@Override
	// 	// 		public void run() {
	// 	// 			try {
	// 	// 				socket.connect(socketAddress, timeout);
	// 	// 			}
	// 	// 			catch (IOException e) {
	// 	// 				setException(e);
	// 	// 				return;
	// 	// 			}
	// 	// 			synchronized (wasInterruptedLock) {
	// 	// 				if (wasInterrupted) {
	// 	// 					closeSocket();
	// 	// 					return;
	// 	// 				}
	// 	// 			}
	// 	// 			setResult(socket);
	// 	// 		}
	// 	// 	});
	// 	// }
	//
	// 	private void closeSocket() {
	// 		try {
	// 			socket.close();
	// 		}
	// 		catch (IOException ioException) {
	// 			LOGGER.log(Level.WARNING, "Could not close socket", ioException);
	// 		}
	// 	}
	// }

	// public abstract static class InternalProcessStanzaSmackFuture<V, E extends Exception> extends InternalSmackFuture<V, E>
	// 		implements StanzaListener, ExceptionCallback<E> {
	//
	// 	/**
	// 	 * This method checks if the given exception is <b>not</b> fatal. If this method returns <code>false</code>,
	// 	 * then the future will automatically set the given exception as failure reason and notify potential waiting
	// 	 * threads.
	// 	 *
	// 	 * @param exception the exception to check.
	// 	 * @return <code>true</code> if the exception is not fatal, <code>false</code> otherwise.
	// 	 */
	// 	protected abstract boolean isNonFatalException(E exception);
	//
	// 	protected abstract void handleStanza(Stanza stanza);
	//
	// 	@Override
	// 	public final synchronized void processException(E exception) {
	// 		if (!isNonFatalException(exception)) {
	// 			this.exception = exception;
	// 			this.notifyAll();
	//
	// 			maybeInvokeCallbacks();
	// 		}
	// 	}
	//
	// 	/**
	// 	 * Wrapper method for {@link #handleStanza(Stanza)}. Note that this method is <code>synchronized</code>.
	// 	 */
	// 	@Override
	// 	public final synchronized void processStanza(Stanza stanza) {
	// 		handleStanza(stanza);
	// 	}
	// }
	//
	// /**
	//  * A simple version of InternalSmackFuture which implements isNonFatalException(E) as always returning
	//  * <code>false</code> method.
	//  *
	//  * @param <V> the return value of the future.
	//  */
	// public abstract static class SimpleInternalProcessStanzaSmackFuture<V, E extends Exception>
	// 		extends InternalProcessStanzaSmackFuture<V, E> {
	// 	@Override
	// 	protected boolean isNonFatalException(E exception) {
	// 		return false;
	// 	}
	// }
	//
	// public static <V, E extends Exception> SmackFuture<V, E> from(V result) {
	// 	InternalSmackFuture<V, E> future = new InternalSmackFuture<>();
	// 	future.setResult(result);
	// 	return future;
	// }
	//
	// public static boolean await(Collection<? extends SmackFuture<?, ?>> futures, long timeout, TimeUnit unit) throws InterruptedException {
	// 	CountDownLatch latch = new CountDownLatch(futures.size());
	// 	for (SmackFuture<?, ?> future : futures) {
	// 		future.onCompletion(f -> latch.countDown());
	// 	}
	//
	// 	return latch.await(timeout, unit);
	// }
}
