package org.tamrielrebuilt.esm2yaml.schema.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.schema.Context;

public class Record {
	private final int type;
	private final List<Subrecord> subrecords;

	private Record(int type, List<Subrecord> subrecords) {
		this.type = type;
		this.subrecords = subrecords;
	}

	int getType() {
		return type;
	}

	boolean onSubrecord(int type, EsmInputStream input, Context context) throws IOException {
		for(Subrecord subrecord : subrecords) {
			if(subrecord.getType() == type) {
				subrecord.handle(input, context);
				return true;
			}
		}
		return false;
	}

	public static class Builder {
		private final int type;
		private final List<Subrecord.Builder> builders;
		private String outputFile;

		public Builder(int type) {
			this.type = type;
			builders = new ArrayList<>();
		}

		public Subrecord.Builder addSubrecord(int type) {
			Subrecord.Builder builder = new Subrecord.Builder(type, this);
			builders.add(builder);
			return builder;
		}

		public void setOutputFile(String file) {
			outputFile = file;
		}

		public String getOutputFile() {
			return outputFile;
		}

		Record build() {
			List<Subrecord> subrecords = new ArrayList<>(builders.size());
			builders.stream().map(Subrecord.Builder::build).forEach(subrecords::add);
			return new Record(type, subrecords);
		}
	}
}