package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.schema.Context;
import org.tamrielrebuilt.esm2yaml.schema.dsl.RecordOutput.Type;

public interface RecordInstruction {
	void execute(Context context) throws IOException;

	public class Builder {
		private VariableField file;
		private Type type;
		private VariableField variables;
		private VariableField deleteLocal;

		public void setOutput(VariableField file, Type type) {
			this.file = file;
			this.type = type;
		}
	
		public void setVariables(VariableField field) {
			variables = field;
		}

		public void deleteVariable(VariableField field) {
			deleteLocal = field;
		}

		RecordInstruction build() {
			if(deleteLocal != null) {
				return ctx -> ctx.unsetVariable(deleteLocal, true);
			} else if(file != null) {
				return new RecordOutput(file, type, variables);
			}
			throw new IllegalStateException("Missing record output");
		}
	}
}
