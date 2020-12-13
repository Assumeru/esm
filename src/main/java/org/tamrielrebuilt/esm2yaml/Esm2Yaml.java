package org.tamrielrebuilt.esm2yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.tamrielrebuilt.esm2yaml.esm.CloseableRecordListener;
import org.tamrielrebuilt.esm2yaml.esm.EsmReader;
import org.tamrielrebuilt.esm2yaml.schema.SchemaParser;
import org.tamrielrebuilt.esm2yaml.schema.dsl.ListenerFactory;

public class Esm2Yaml {
	public static void main(String[] args) throws IOException {
		File directory = new File("C:\\Users\\EE\\Desktop\\test\\src");
		String dataFiles = "F:\\Program Files (x86)\\Morrowind\\Data Files";
//		File file = new File(dataFiles, "HB_Dark_Guars_V11.ESP");
		File file = new File(dataFiles, "0PC_shrinetest.ESP");
		
		toYaml(file, directory, StandardCharsets.ISO_8859_1);
	}

	private static void toYaml(File esx, File output, Charset charset) throws IOException {
		ListenerFactory builder = SchemaParser.getBuilder();
		try(EsmReader reader = new EsmReader(esx, charset);
				CloseableRecordListener listener = builder.build(output)) {
			reader.read(listener);
		}
	}
}
