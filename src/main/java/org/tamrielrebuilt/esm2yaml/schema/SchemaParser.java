package org.tamrielrebuilt.esm2yaml.schema;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.tamrielrebuilt.esm2yaml.esm.RecordListener;
import org.tamrielrebuilt.esm2yaml.esm.RecordUtil;
import org.tamrielrebuilt.esm2yaml.io.ThrowingConsumer;
import org.tamrielrebuilt.esm2yaml.schema.builder.Record;
import org.tamrielrebuilt.esm2yaml.schema.builder.RecordListenerBuilder;
import org.tamrielrebuilt.esm2yaml.schema.builder.Subrecord;
import org.tamrielrebuilt.esm2yaml.schema.builder.SubrecordDataBuilder;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

public class SchemaParser implements Closeable {
	private static final YAMLFactory FACTORY = new YAMLFactory();
	private final YAMLParser parser;

	public static Function<Context, RecordListener> getBuilder() throws IOException {
		try(SchemaParser schema = new SchemaParser(SchemaParser.class.getResourceAsStream("/schema.yaml"))) {
			return schema.parse();
		}
	}

	public SchemaParser(InputStream input) throws IOException {
		parser = FACTORY.createParser(Objects.requireNonNull(input));
	}

	public Function<Context, RecordListener> parse() throws IOException {
		RecordListenerBuilder builder = new RecordListenerBuilder();
		parseObject(recordName -> {
			if(recordName.length() != 4) {
				throw new IllegalStateException("Expected record, found " + recordName);
			}
			int record = RecordUtil.getValue(recordName);
			parseRecord(builder.addRecord(record));
		});
		return builder.build();
	}

	private void parseRecord(Record.Builder builder) throws IOException {
		parseObject(key -> {
			if("subrecords".equals(key)) {
				parseSubrecords(builder);
			} else if("output".equals(key)) {
				parseObject(field -> {
					if("file".equals(field)) {
						builder.setOutputFile(parser.nextTextValue());
					} else {
						throw new IllegalStateException("Unexpected record output key " + field);
					}
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
					parseObject(field -> {
						if("file".equals(field)) {
							builder.setOutputFile(parser.nextTextValue());
						} else {
							throw new IllegalStateException("Unexpected data output key " + key);
						}
					});
				} else if("length".equals(key)) {
					expect(JsonToken.VALUE_NUMBER_INT);
					builder.setLength(parser.getIntValue());
				} else if("name".equals(key)) {
					Field name = parseDataName();
					builder.setOutputField(name);
				} else if("value".equals(key)) {
					parser.nextValue();
					builder.setOutputValue(parser.getCurrentValue());
				} else {
					throw new IllegalStateException("Unexpected data key " + key);
				}
			}, false);
		});
	}

	private Field parseDataName() throws IOException {
		if(parser.nextToken() == JsonToken.VALUE_STRING) {
			return new Field(parser.getText());
		} else if(parser.currentToken() == JsonToken.START_ARRAY) {
			List<String> segments = new ArrayList<>();
			parseArray(t -> {
				segments.add(parser.getText());
			}, false);
			return new Field(segments);
		}
		throw new IllegalStateException("Invalid data name " + parser.currentToken());
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
