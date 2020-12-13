package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.util.stream.Stream;

import org.tamrielrebuilt.esm2yaml.schema.Context;

public class ConcatVariable implements Resolvable {
	private final VariableField field;

	public ConcatVariable(VariableField field) {
		this.field = field;
	}

	@Override
	public Stream<String> resolve(Context context) {
		StringBuilder sb = new StringBuilder();
		field.getPath(context).forEach(sb::append);;
		return Stream.of(sb.toString());
	}
}
