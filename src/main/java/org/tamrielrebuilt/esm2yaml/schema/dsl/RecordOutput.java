package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.tamrielrebuilt.esm2yaml.schema.Context;

import com.fasterxml.jackson.core.JsonGenerator;

public class RecordOutput implements RecordInstruction {
	private static final Set<Class<?>> STRING_WRITABLE = new HashSet<>(Arrays.asList(
			Character.class, Double.class, Float.class, Integer.class, Long.class, Short.class, String.class));
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
		Object value = variables == null ? context.getVariable(true) : context.getVariable(variables, true);
		if(value != null) {
			if(type == Type.YAML) {
				writeYaml(context, value);
			} else {
				writeRaw(context, value);
			}
		}
	}

	private void writeYaml(Context context, Object value) throws IOException {
		try(JsonGenerator generator = context.openYaml(file)) {
			generator.writeObject(value);
		}
	}

	private void writeRaw(Context context, Object value) throws IOException {
		byte[] buffer = null;
		if(STRING_WRITABLE.contains(value.getClass())) {
			buffer = value.toString().getBytes(StandardCharsets.UTF_8);
		} else if(value instanceof Byte) {
			buffer = new byte[] { (Byte) value };
		} else if(value instanceof byte[]) {
			buffer = (byte[]) value;
		}
		if(buffer != null) {
			try(OutputStream output = context.open(file)) {
				output.write(buffer);
			}
		} else {
			throw new IllegalStateException("Cannot write " + value.getClass() + " to " + file);
		}
	}

	public enum Type {
		RAW, YAML
	}
}
