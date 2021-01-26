package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.tamrielrebuilt.esm.CloseableRecordListener;
import org.tamrielrebuilt.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.JsonLockWriter;
import org.tamrielrebuilt.esm2yaml.schema.Context;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ListenerFactory {
	private final List<Record> records;
	private YAMLFactory factory;

	private ListenerFactory(List<Record> records, YAMLFactory factory) {
		this.records = records;
		this.factory = factory;
	}

	public void setFactory(YAMLFactory factory) {
		this.factory = factory;
	}

	public CloseableRecordListener build(File directory) throws IOException {
		if(directory.exists()) {
			//TODO throw new IllegalArgumentException(directory.getAbsolutePath() + " already exists");
		}
		directory.mkdirs();
		JsonLockWriter lock = new JsonLockWriter(directory, factory);
		Context context = new Context(directory, factory, lock);
		try {
			return new Listener(context, records, lock);
		} finally {
			lock.open("lock.yaml");
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<Record.Builder> builders;

		private Builder() {
			builders = new ArrayList<>();
		}

		public Record.Builder addRecord(int type) {
			Record.Builder record = new Record.Builder(type);
			builders.add(record);
			return record;
		}

		public ListenerFactory build(YAMLFactory factory) {
			List<Record> records = new ArrayList<>(builders.size());
			builders.stream().map(Record.Builder::build).forEach(records::add);
			return new ListenerFactory(records, factory);
		}
	}

	private static class Listener implements CloseableRecordListener {
		private final Context context;
		private final List<Record> records;
		private final CloseableRecordListener lock;
		private Record current;

		private Listener(Context context, List<Record> records, CloseableRecordListener lock) {
			this.context = context;
			this.records = records;
			this.lock = lock;
		}

		@Override
		public void onRecord(int type, int flags, int unknown) throws IOException {
			for(Record record : records) {
				if(record.getType() == type) {
					current = record;
					context.onRecord(type, flags, unknown);
					return;
				}
			}
			lock.onRecord(type, flags, unknown);
		}

		@Override
		public void onSubrecord(int type, EsmInputStream input) throws IOException {
			if(current == null || !current.onSubrecord(type, input, context)) {
				lock.onSubrecord(type, input);
			}
		}

		@Override
		public void onRecordEnd() throws IOException {
			if(current != null) {
				current.execute(context);
				current = null;
				context.onRecordEnd();
			} else {
				lock.onRecordEnd();
			}
		}

		@Override
		public void close() throws IOException {
			lock.close();
		}

		@Override
		public String toString() {
			return "context: " + context + "\nrecords: " + records + "\nlock: " + lock + "\ncurrent: " + current;
		}
	}
}
