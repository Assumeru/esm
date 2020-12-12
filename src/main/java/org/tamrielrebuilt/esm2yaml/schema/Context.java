package org.tamrielrebuilt.esm2yaml.schema;

import java.io.File;
import java.io.IOException;

import org.tamrielrebuilt.esm2yaml.esm.EsmInputStream;
import org.tamrielrebuilt.esm2yaml.esm.RecordListener;
import org.tamrielrebuilt.esm2yaml.schema.dsl.Scope;
import org.tamrielrebuilt.esm2yaml.schema.dsl.VariableField;

public class Context implements RecordListener {
	private final Scope globalVariables;
	private final Scope localVariables;

	public Context() {
		globalVariables = new Scope();
		localVariables = new Scope();
	}

	public void setVariable(VariableField path, boolean local, Object value, boolean overwrite) {
		(local ? localVariables : globalVariables).set(path.getPath(this), value, overwrite);
	}

	public void pushVariable(VariableField path, boolean local) {
		(local ? localVariables : globalVariables).push(path.getPath(this));
	}

	public Object getVariable(VariableField path, boolean local) {
		return (local ? localVariables : globalVariables).get(path.getPath(this));
	}

	public void unsetVariable(VariableField path, boolean local) {
		(local ? localVariables : globalVariables).delete(path.getPath(this));
	}

	public File getFile(VariableField path) {
		//TODO
		return null;
	}

	@Override
	public void onRecord(int type, int flags, int unknown) throws IOException {
		if(!localVariables.isEmpty()) {
			System.out.println(localVariables);
		}
		localVariables.clear();
		if(flags != 0) {
			localVariables.set("recordFlags", flags);
		}
		if(unknown != 0) {
			localVariables.set("unknown", unknown);
		}
	}

	@Override
	public void onSubrecord(int type, EsmInputStream input) throws IOException {
		input.skip(input.available());
	}
}
