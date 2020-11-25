package org.tamrielrebuilt.esm2yaml;

import java.io.File;
import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.esm.jackson.JsonEsmWriter;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public class YamlWriter extends JsonEsmWriter {
	public YamlWriter(File directory) throws IOException {
		super(directory, new YAMLFactory().enable(Feature.LITERAL_BLOCK_STYLE), null);
	}
}
