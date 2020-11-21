package org.tamrielrebuilt.esm2yaml.esm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.tamrielrebuilt.esm2yaml.Util;

public class TypedRecordListener implements CloseableRecordListener {
	private final Map<Integer, CloseableRecordListener> listeners;
	private final CloseableRecordListener fallback;
	private RecordListener current;

	public TypedRecordListener(CloseableRecordListener fallback) {
		listeners = new HashMap<>();
		this.fallback = fallback;
	}

	public void register(int type, CloseableRecordListener listener) {
		listeners.put(type, listener);
	}

	@Override
	public void onRecord(int type, int flags, int unknown) throws IOException {
		current = listeners.getOrDefault(type, fallback);
		current.onRecord(type, flags, unknown);
	}

	@Override
	public void onSubrecord(int type, EsmInputStream input) throws IOException {
		current.onSubrecord(type, input);
	}

	@Override
	public void close() throws IOException {
		try {
			Throwable t = null;
			for(CloseableRecordListener listener : listeners.values()) {
				try {
					listener.close();
				} catch(Throwable e) {
					if(t == null) {
						t = e;
					} else {
						t.addSuppressed(e);
					}
				}
			}
			if(t != null) {
				Util.sneakyThrow(t);
			}
		} finally {
			fallback.close();
		}
	}
}
