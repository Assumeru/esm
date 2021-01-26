package org.tamrielrebuilt.esm2yaml.schema.dsl;

import org.tamrielrebuilt.esm2yaml.schema.Context;

public class PushInstruction implements SubrecordInstruction {
	private final VariableField field;
	private final boolean local;

	public PushInstruction(VariableField field, boolean local) {
		this.field = field;
		this.local = local;
	}

	@Override
	public void execute(Context context, Object value) {
		context.pushVariable(field, local);
	}
}
