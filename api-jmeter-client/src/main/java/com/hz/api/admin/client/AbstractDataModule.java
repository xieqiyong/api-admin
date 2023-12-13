package com.hz.api.admin.client;

import java.util.concurrent.atomic.AtomicReference;

public class AbstractDataModule {

	protected DataServerClient client;

	protected AtomicReference<State> state = new AtomicReference<State>(State.None);

	final void initialize(DataServerClient client) {
		this.client = client;
		internalInitialize(client);
		state.set(State.Initialize);
	}

	protected void internalInitialize(DataServerClient client) {
	}

	final void start() {
		internalStart();
		state.set(State.Start);
	}

	protected void internalStart() {
	}

	final void stop() {
		internalStop();
		state.set(State.Stop);
	}

	protected void internalStop() {
	}

	protected boolean isStoped() {
		return state.get() == State.Stop;
	}

	public enum State {
		None, Initialize, Start, Stop
	}

}
