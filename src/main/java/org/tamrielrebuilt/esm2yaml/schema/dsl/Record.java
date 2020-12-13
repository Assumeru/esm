package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.schema.Context;

public class Record implements RecordInstruction {
	private final int type;
	private final List<Subrecord> subrecords;
	private final List<RecordInstruction> output;

	private Record(int type, List<Subrecord> subrecords, List<RecordInstruction> output) {
		this.type = type;
		this.subrecords = subrecords;
		this.output = output;
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

	@Override
	public void execute(Context context) throws IOException {
		for(RecordInstruction o : output) {
			o.execute(context);
		}
	}

	public static class Builder {
		private final int type;
		private final List<Subrecord.Builder> subBuilders;
		private final List<RecordInstruction.Builder> outputBuilders;

		public Builder(int type) {
			this.type = type;
			subBuilders = new ArrayList<>();
			outputBuilders = new ArrayList<>();
		}

		public Subrecord.Builder addSubrecord(int type) {
			Subrecord.Builder builder = new Subrecord.Builder(type, this);
			subBuilders.add(builder);
			return builder;
		}

		public RecordInstruction.Builder addOutput() {
			RecordInstruction.Builder builder = new RecordInstruction.Builder();
			outputBuilders.add(builder);
			return builder;
		}

		Record build() {
			List<Subrecord> subrecords = new ArrayList<>(subBuilders.size());
			subBuilders.stream().map(Subrecord.Builder::build).forEach(subrecords::add);
			List<RecordInstruction> output = new ArrayList<>(outputBuilders.size());
			outputBuilders.stream().map(RecordInstruction.Builder::build).forEach(output::add);
			return new Record(type, subrecords, output);
		}
	}
}