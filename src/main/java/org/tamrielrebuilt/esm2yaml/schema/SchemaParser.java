package org.tamrielrebuilt.esm2yaml.schema;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.tamrielrebuilt.esm2yaml.esm.RecordUtil;
import org.tamrielrebuilt.esm2yaml.io.ThrowingConsumer;
import org.tamrielrebuilt.esm2yaml.schema.dsl.ListenerFactory;
import org.tamrielrebuilt.esm2yaml.schema.dsl.PushInstruction;
import org.tamrielrebuilt.esm2yaml.schema.dsl.Record;
import org.tamrielrebuilt.esm2yaml.schema.dsl.RecordInstruction;
import org.tamrielrebuilt.esm2yaml.schema.dsl.RecordOutput.Type;
import org.tamrielrebuilt.esm2yaml.schema.dsl.SetInstruction;
import org.tamrielrebuilt.esm2yaml.schema.dsl.Subrecord;
import org.tamrielrebuilt.esm2yaml.schema.dsl.SubrecordDataBuilder;
import org.tamrielrebuilt.esm2yaml.schema.dsl.VariableField;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

public class SchemaParser implements Closeable {
	private static final YAMLFactory FACTORY = new YAMLMapper().getFactory();
	private final YAMLParser parser;

	public static ListenerFactory getBuilder() throws IOException {
		try(SchemaParser schema = new SchemaParser(SchemaParser.class.getResourceAsStream("/schema.yaml"))) {
			return schema.parse();
		}
	}

	public SchemaParser(InputStream input) throws IOException {
		parser = FACTORY.createParser(Objects.requireNonNull(input));
	}

	public ListenerFactory parse() throws IOException {
		ListenerFactory.Builder builder = ListenerFactory.builder();
		parseObject(recordName -> {
			if(recordName.length() != 4) {
				throw new IllegalStateException("Expected record, found " + recordName);
			}
			int record = RecordUtil.getValue(recordName);
			parseRecord(builder.addRecord(record));
		});
		return builder.build(FACTORY);
	}

	private void parseRecord(Record.Builder builder) throws IOException {
		parseObject(key -> {
			if("subrecords".equals(key)) {
				parseSubrecords(builder);
			} else if("output".equals(key)) {
				parseArray(t -> {
					RecordInstruction.Builder output = builder.addOutput();
					parseRecordOutput(output);
				});
			} else {
				throw new IllegalStateException("Unexpected record key " + key);
			}
		});
	}

	private void parseSubrecords(Record.Builder builder) throws IOException {
		parseObject(subrecordName -> {
			if(subrecordName.length() != 4) {
				throw new IllegalStateException("Expected subrecord, found " + subrecordName);
			}
			int type = RecordUtil.getValue(subrecordName);
			Subrecord.Builder subrecord = builder.addSubrecord(type);
			parseSubrecord(subrecord);
		});
	}

	private void parseSubrecord(Subrecord.Builder subrecord) throws IOException {
		parseObject(key -> {
			if("data".equals(key)) {
				parseSubrecordData(subrecord);
			} else {
				throw new IllegalStateException("Unexpected subrecord key " + key);
			}
		});
	}

	private void parseSubrecordData(Subrecord.Builder subrecord) throws IOException {
		parseArray(t -> {
			SubrecordDataBuilder builder = subrecord.addData();
			parseObject(key -> {
				if("type".equals(key)) {
					builder.setType(parser.nextTextValue());
				} else if("enum".equals(key)) {
					Map<String, String> mappings = new HashMap<>();
					parseObject(value -> {
						String name = parser.nextTextValue();
						mappings.put(value, name);
					});
					builder.setEnum(mappings);
				} else if("output".equals(key)) {
					parseArray(t2 -> {
						parseObject(outputKey -> {
							if("push".equals(outputKey)) {
								VariableField name = parseVariableField();
								builder.addInstruction(new PushInstruction(name, true));
							} else if("set".equals(outputKey)) {
								VariableField name = parseVariableField();
								builder.addInstruction(new SetInstruction(name, true));
							} else {
								throw new IllegalStateException("Unexpected data output key " + key);
							}
						}, false);
					});
				} else if("length".equals(key)) {
					expect(JsonToken.VALUE_NUMBER_INT);
					builder.setLength(parser.getIntValue());
				} else if("name".equals(key)) {
					VariableField name = parseVariableField();
					builder.addInstruction(new SetInstruction(name, true));
				} else if("value".equals(key)) {
					parser.nextValue();
					builder.setOutputValue(parser.getCurrentValue());
				} else {
					throw new IllegalStateException("Unexpected data key " + key);
				}
			}, false);
		});
	}

	private void parseRecordOutput(RecordInstruction.Builder builder) throws IOException {
		parseObject(key -> {
			if("yaml".equals(key)) {
				VariableField file = parseVariableField();
				builder.setOutput(file, Type.YAML);
			} else if("raw".equals(key)) {
				VariableField file = parseVariableField();
				builder.setOutput(file, Type.RAW);
			} else if("value".equals(key)) {
				VariableField field = parseVariableField();
				builder.setVariables(field);
			} else if("delete".equals(key)) {
				VariableField field = parseVariableField();
				builder.deleteVariable(field);
			} else {
				throw new IllegalStateException("Unexpected record output key " + key);
			}
		}, false);
	}

	private VariableField parseVariableField() throws IOException {
		VariableField.Builder builder = VariableField.builder();
		if(parser.nextToken() == JsonToken.VALUE_STRING) {
			builder.append(parser.getText());
		} else if(parser.currentToken() == JsonToken.START_ARRAY) {
			parseArray(type -> {
				if(type.isScalarValue()) {
					builder.append(parser.getText());
				} else if(type == JsonToken.START_OBJECT) {
					parseObject(key -> {
						if("var".equals(key)) {
							builder.append(parseVariableField(), true);
						} else {
							throw new IllegalStateException("Unexpected field key " + key);
						}
					}, false);
				} else {
					throw new IllegalStateException("Invalid variable segment " + type);
				}
			}, false);
		} else {
			throw new IllegalStateException("Invalid variable field " + parser.currentToken());
		}
		return builder.build();
	}

	private void parseArray(ThrowingConsumer<JsonToken> valueListener) throws IOException {
		parseArray(valueListener, true);
	}

	private void parseArray(ThrowingConsumer<JsonToken> valueListener, boolean advance) throws IOException {
		expect(JsonToken.START_ARRAY, advance);
		JsonToken token;
		while((token = parser.nextToken()) != JsonToken.END_ARRAY && token != null) {
			valueListener.accept(token);
		}
		expect(JsonToken.END_ARRAY, false);
	}

	private void parseObject(ThrowingConsumer<String> keyListener) throws IOException {
		parseObject(keyListener, true);
	}

	private void parseObject(ThrowingConsumer<String> keyListener, boolean advance) throws IOException {
		expect(JsonToken.START_OBJECT, advance);
		String key;
		while((key = parser.nextFieldName()) != null) {
			keyListener.accept(key);
		}
		expect(JsonToken.END_OBJECT, false);
	}

	private void expect(JsonToken token) throws IOException {
		expect(token, true);
	}

	private void expect(JsonToken token, boolean advance) throws IOException {
		JsonToken found;
		if(advance) {
			found = parser.nextToken();
		} else {
			found = parser.currentToken();
		}
		if(found != token) {
			throw new IllegalStateException("Expected " + token + ", found " + found);
		}
	}

	@Override
	public void close() throws IOException {
		parser.close();
	}
}
