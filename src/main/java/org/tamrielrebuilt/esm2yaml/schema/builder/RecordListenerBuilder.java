package org.tamrielrebuilt.esm2yaml.schema.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.RecordListener;
import org.tamrielrebuilt.esm2yaml.schema.Context;

public class RecordListenerBuilder {
	private final List<Record.Builder> builders;

	public RecordListenerBuilder() {
		builders = new ArrayList<>();
	}

	public Record.Builder addRecord(int type) {
		Record.Builder record = new Record.Builder(type);
		builders.add(record);
		return record;
	}

	public Function<Context, RecordListener> build() {
		List<Record> records = new ArrayList<>(builders.size());
		builders.stream().map(Record.Builder::build).forEach(records::add);
		return context -> new Listener(context, records);
	}

	private static class Listener implements RecordListener {
		private final Context context;
		private final List<Record> records;
		private Record current;

		private Listener(Context context, List<Record> records) {
			this.context = context;
			this.records = records;
		}

		@Override
		public void onRecord(int type, int flags, int unknown) throws IOException {
			current = null;
			for(Record record : records) {
				if(record.getType() == type) {
					current = record;
					break;
				}
			}
			context.onRecord(type, flags, unknown);
		}

		@Override
		public void onSubrecord(int type, EsmInputStream input) throws IOException {
			if(current == null || !current.onSubrecord(type, input, context)) {
				context.onSubrecord(type, input);
			}
		}
	}
}
