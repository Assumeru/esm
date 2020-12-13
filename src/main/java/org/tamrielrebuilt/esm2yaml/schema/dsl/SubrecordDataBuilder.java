package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.io.HexInputStream;
import org.tamrielrebuilt.esm2yaml.schema.Context;
import org.tamrielrebuilt.esm2yaml.schema.DataHandler;

public class SubrecordDataBuilder {
	private final List<SubrecordInstruction> instructions;
	private String type;
	private Map<String, String> mappings;
	private Object outputValue;
	private Integer typeLength;

	SubrecordDataBuilder() {
		instructions = new ArrayList<>();
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setEnum(Map<String, String> mappings) {
		this.mappings = mappings;
	}

	public void setOutputValue(Object outputValue) {
		this.outputValue = outputValue;
	}

	public void setLength(int length) {
		typeLength = length;
	}

	public void addInstruction(SubrecordInstruction instruction) {
		instructions.add(instruction);
	}

	DataHandler build() {
		ValueReader reader = null;
		Function<String, Object> enumMapper = null;
		if("int".equals(type)) {
			enumMapper = Integer::parseInt;
			reader = EsmInputStream::readLEInt;
		} else if("float".equals(type)) {
			reader = EsmInputStream::readLEFloat;
		} else if("long".equals(type)) {
			enumMapper = Long::parseLong;
			reader = EsmInputStream::readLELong;
		} else if("short".equals(type)) {
			enumMapper = Short::parseShort;
			reader = EsmInputStream::readLEShort;
		} else if("byte".equals(type)) {
			enumMapper = Byte::parseByte;
			reader = EsmInputStream::read;
		} else if("string".equals(type)) {
			if(typeLength == null) {
				reader = EsmInputStream::readString;
			} else {
				reader = createStringReader(typeLength);
			}
		} else if("binary".equals(type)) {
			reader = EsmInputStream::readAllBytes;
		} else if("hex".equals(type)) {
			reader = ValueReader.HEX_READER;
		} else if("marker".equals(type)) {
			reader = createMarkerReader(outputValue);
		}
		if(reader == null) {
			throw new IllegalStateException("Unknown type " + type);
		}
		if(enumMapper != null && mappings != null && !mappings.isEmpty()) {
			reader = createEnumReader(mappings, enumMapper, reader);
		}
		return new InstructionDataHandler(instructions, reader);
	}

	@FunctionalInterface
	private interface ValueReader {
		Object read(EsmInputStream input) throws IOException;

		static ValueReader HEX_READER = input -> {
			StringWriter writer = new StringWriter(input.available() * 2);
			try(InputStreamReader reader = new InputStreamReader(new HexInputStream(input), StandardCharsets.US_ASCII)) {
				reader.transferTo(writer);
			}
			return writer.toString();
		};
	}

	private static <T> ValueReader createEnumReader(Map<String, String> mappings, Function<String, T> mapper, ValueReader reader) {
		Map<T, String> parsed = new HashMap<>(mappings.size());
		mappings.forEach((key, value) -> {
			parsed.put(mapper.apply(key), value);
		});
		return input -> {
			Object value = reader.read(input);
			String mapping = parsed.get(value);
			if(mapping != null) {
				return mapping;
			}
			return value;
		};
	}

	private static ValueReader createStringReader(int length) {
		return input -> input.readString(length);
	}

	private static ValueReader createMarkerReader(Object value) {
		return input -> {
			input.skip(input.available());
			return value;
		};
	}

	private static class InstructionDataHandler implements DataHandler {
		private final List<SubrecordInstruction> instructions;
		private final ValueReader reader;

		public InstructionDataHandler(List<SubrecordInstruction> instructions, ValueReader reader) {
			this.instructions = instructions.isEmpty() ? Collections.emptyList() : instructions;
			this.reader = reader;
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			Object value = reader.read(input);
			for(SubrecordInstruction instruction : instructions) {
				instruction.execute(context, value);
			}
		}
	}
}