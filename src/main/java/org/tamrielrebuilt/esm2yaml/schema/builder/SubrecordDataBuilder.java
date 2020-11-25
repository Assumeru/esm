package org.tamrielrebuilt.esm2yaml.schema.builder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.io.HexInputStream;
import org.tamrielrebuilt.esm2yaml.schema.Context;
import org.tamrielrebuilt.esm2yaml.schema.DataHandler;
import org.tamrielrebuilt.esm2yaml.schema.Field;

public class SubrecordDataBuilder {
	private final Subrecord.Builder parent;
	private String type;
	private Map<String, String> mappings;
	private String outputFile;
	private Field outputField;
	private Object outputValue;
	private Integer typeLength;

	SubrecordDataBuilder(Subrecord.Builder parent) {
		this.parent = parent;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setEnum(Map<String, String> mappings) {
		this.mappings = mappings;
	}

	public void setOutputFile(String file) {
		outputFile = file;
	}

	public void setOutputField(Field field) {
		outputField = field;
	}

	public void setOutputValue(Object outputValue) {
		this.outputValue = outputValue;
	}

	public void setLength(int length) {
		typeLength = length;
	}

	DataHandler build() {
		if(outputFile == null) {
			outputFile = parent.getParent().getOutputFile();
		}
		DataHandler handler = null;
		if("int".equals(type)) {
			handler = new IntDataHandler(outputFile, outputField, mappings);
		} else if("float".equals(type)) {
			handler = new FloatDataHandler(outputFile, outputField);
		} else if("long".equals(type)) {
			handler = new LongDataHandler(outputFile, outputField);
		} else if("string".equals(type)) {
			handler = new StringDataHandler(outputFile, outputField, typeLength);
		} else if("binary".equals(type)) {
			handler = new BinaryDataHandler(outputFile);
		} else if("hex".equals(type)) {
			handler = new HexDataHandler(outputFile);
		} else if("marker".equals(type)) {
			handler = new MarkerDataHandler(outputFile, outputField, outputValue);
		}
		if(handler == null) {
			throw new IllegalStateException("Unknown type " + type);
		}
		return handler;
	}

	private static class IntDataHandler implements DataHandler {
		private final String file;
		private final Field field;
		private final Map<Integer, String> mappings;

		public IntDataHandler(String file, Field field, Map<String, String> mappings) {
			this.file = file;
			this.field = field;
			if(mappings == null) {
				this.mappings = null;
			} else {
				this.mappings = new HashMap<>(mappings.size());
				mappings.forEach((key, value) -> {
					this.mappings.put(Integer.parseInt(key), value);
				});
			}
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			int value = input.readLEInt();
			if(mappings != null) {
				String mapping = mappings.get(value);
				if(mapping != null) {
					context.writeStringField(file, field, mapping);
					return;
				}
			}
			context.writeIntField(file, field, value);
		}
	}

	private static class FloatDataHandler implements DataHandler {
		private final String file;
		private final Field field;

		public FloatDataHandler(String file, Field field) {
			this.file = file;
			this.field = field;
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			float value = input.readLEFloat();
			context.writeFloatField(file, field, value);
		}
	}

	private static class LongDataHandler implements DataHandler {
		private final String file;
		private final Field field;

		public LongDataHandler(String file, Field field) {
			this.file = file;
			this.field = field;
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			long value = input.readLELong();
			context.writeLongField(file, field, value);
		}
	}

	private static class StringDataHandler implements DataHandler {
		private final String file;
		private final Field field;
		private final Integer length;

		public StringDataHandler(String file, Field field, Integer length) {
			this.file = file;
			this.field = field;
			this.length = length;
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			String value;
			if(length != null) {
				value = input.readString(length);
			} else {
				value = input.readString();
			}
			context.writeStringField(file, field, value);
		}
	}

	private static class BinaryDataHandler implements DataHandler {
		private final String file;

		public BinaryDataHandler(String file) {
			this.file = file;
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			context.write(file, input);
		}
	}

	private static class HexDataHandler implements DataHandler {
		private final String file;

		public HexDataHandler(String file) {
			this.file = file;
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			try(InputStreamReader reader = new InputStreamReader(new HexInputStream(input), StandardCharsets.US_ASCII)) {
				context.write(file, reader);
			}
		}
	}

	private static class MarkerDataHandler implements DataHandler {
		private final String file;
		private final Field field;
		private final Object value;

		public MarkerDataHandler(String file, Field field, Object value) {
			this.file = file;
			this.field = field;
			this.value = value;
		}

		@Override
		public void handle(EsmInputStream input, Context context) throws IOException {
			if(value instanceof Boolean) {
				context.writeBooleanField(file, field, (Boolean) value);
			}
			//TODO
			input.skip(input.available());
		}
	}
}