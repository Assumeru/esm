package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.schema.Context;

public class RecordOutput implements RecordInstruction {
	private final VariableField file;
	private final Type type;
	private final VariableField variables;

	public RecordOutput(VariableField file, Type type, VariableField variables) {
		this.file = file;
		this.type = type;
		this.variables = variables;
	}

	@Override
	public void execute(Context context) throws IOException {
		//TODO
		context.getFile(file);
	}

	public enum Type {
		RAW, YAML
	}
}
