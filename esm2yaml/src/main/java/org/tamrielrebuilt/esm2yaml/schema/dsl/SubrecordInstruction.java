package org.tamrielrebuilt.esm2yaml.schema.dsl;

import org.tamrielrebuilt.esm2yaml.schema.Context;

public interface SubrecordInstruction {
	void execute(Context context, Object value);
}
