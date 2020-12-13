package org.tamrielrebuilt.esm2yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.tamrielrebuilt.esm2yaml.esm.CloseableRecordListener;
import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.EsmReader;
import org.tamrielrebuilt.esm2yaml.esm.RecordListener;
import org.tamrielrebuilt.esm2yaml.schema.SchemaParser;
import org.tamrielrebuilt.esm2yaml.schema.dsl.ListenerFactory;

public class Esm2Yaml {
	public static void main(String[] args) throws IOException {
		File directory = new File("C:\\Users\\EE\\Desktop\\test\\src");

		ListenerFactory builder = SchemaParser.getBuilder();
		
		
		

		String dataFiles = "F:\\Program Files (x86)\\Morrowind\\Data Files";
//		File file = new File(dataFiles, "HB_Dark_Guars_V11.ESP");
		File file = new File(dataFiles, "0PC_shrinetest.ESP");
		
		try(EsmReader reader = new EsmReader(file, StandardCharsets.ISO_8859_1);
				CloseableRecordListener listener = builder.build(directory)) {
			reader.read(listener);
		}
		

//		try(JsonEsmWriter writer = new YamlWriter(directory)) {
//			try(EsmReader reader = new EsmReader(file, StandardCharsets.ISO_8859_1)) {
//				reader.read(writer);
//			}
//		}
//		Counter c = new Counter();
//		for(File f : new File(dataFiles).listFiles(f -> f.getName().toLowerCase().endsWith(".esp"))) {
//			try(EsmReader reader = new EsmReader(f, StandardCharsets.ISO_8859_1)) {
//				reader.read(c);
//			}
//		}
//		System.out.println(c.getMax());
	}

	static class Counter implements RecordListener {
		private long max;
		private long current;

		@Override
		public void onRecord(int type, int flags, int unknown) throws IOException {
			max = Math.max(current, max);
			current = 0;
		}

		@Override
		public void onSubrecord(int type, EsmInputStream input) throws IOException {
			current += input.available();
			input.skip(input.available());
		}

		public long getMax() {
			return max;
		}
	}
}
