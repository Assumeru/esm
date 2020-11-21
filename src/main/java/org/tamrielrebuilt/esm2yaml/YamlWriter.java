package org.tamrielrebuilt.esm2yaml;

import java.io.File;
import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.esm.jackson.JsonEsmWriter;
import org.tamrielrebuilt.esm2yaml.esm.records.ScriptWriterFactory;
import org.tamrielrebuilt.esm2yaml.esm.records.Tes3WriterFactory;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public class YamlWriter extends JsonEsmWriter {
	public YamlWriter(File directory) throws IOException {
		super(directory, new YAMLFactory().enable(Feature.LITERAL_BLOCK_STYLE));
		register(new Tes3WriterFactory());
		register(new ScriptWriterFactory());
	}
}
