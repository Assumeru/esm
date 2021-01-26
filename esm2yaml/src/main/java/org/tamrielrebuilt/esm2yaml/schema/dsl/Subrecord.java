package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.tamrielrebuilt.esm.EsmInputStream;
import org.tamrielrebuilt.esm.RecordUtil;
import org.tamrielrebuilt.esm2yaml.schema.Context;
import org.tamrielrebuilt.esm2yaml.schema.DataHandler;

public class Subrecord implements DataHandler {
	private final int type;
	private final List<DataHandler> data;

	private Subrecord(int type, List<DataHandler> data) {
		this.type = type;
		this.data = data;
	}

	int getType() {
		return type;
	}

	@Override
	public void handle(EsmInputStream input, Context context) throws IOException {
		for(DataHandler handler : data) {
			handler.handle(input, context);
		}
	}

	@Override
	public String toString() {
		return RecordUtil.appendTo(new StringBuilder("Subrecord "), type).toString();
	}

	public static class Builder {
		private final int type;
		private final List<SubrecordDataBuilder> builders;
		private final Record.Builder parent;

		Builder(int type, Record.Builder parent) {
			this.parent = parent;
			this.type = type;
			builders = new ArrayList<>();
		}

		Record.Builder getParent() {
			return parent;
		}

		public SubrecordDataBuilder addData() {
			SubrecordDataBuilder builder = new SubrecordDataBuilder();
			builders.add(builder);
			return builder;
		}

		Subrecord build() {
			List<DataHandler> data = new ArrayList<>(builders.size());
			builders.stream().map(SubrecordDataBuilder::build).forEach(data::add);
			return new Subrecord(type, data);
		}
	}
}