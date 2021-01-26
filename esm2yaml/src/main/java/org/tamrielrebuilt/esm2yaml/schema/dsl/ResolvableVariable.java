package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.util.List;
import java.util.stream.Stream;

import org.tamrielrebuilt.esm2yaml.schema.Context;

public class ResolvableVariable implements Resolvable {
	private final VariableField field;
	private final boolean local;

	public ResolvableVariable(VariableField field, boolean local) {
		this.field = field;
		this.local = local;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Stream<String> resolve(Context context) {
		Object resolved = context.getVariable(field, local);
		if(resolved == null) {
			throw new IllegalStateException("Missing variable " + field);
		} else if(resolved instanceof List) {
			return ((List) resolved).stream().map(Object::toString);
		}
		return Stream.of(resolved.toString());
	}
}
