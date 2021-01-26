package org.tamrielrebuilt.esm2yaml.schema;

import java.io.IOException;

import org.tamrielrebuilt.esm.EsmInputStream;

public interface DataHandler {
	void handle(EsmInputStream input, Context context) throws IOException;
}
