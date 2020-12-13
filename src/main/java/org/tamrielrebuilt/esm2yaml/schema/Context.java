package org.tamrielrebuilt.esm2yaml.schema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.tamrielrebuilt.esm2yaml.schema.dsl.Scope;
import org.tamrielrebuilt.esm2yaml.schema.dsl.VariableField;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class Context {
	private final File directory;
	private final JsonFactory factory;
	private final LockFileWriter lock;
	private final Scope globalVariables;
	private final Scope localVariables;

	public Context(File directory, JsonFactory factory, LockFileWriter lock) {
		this.directory = directory;
		this.factory = factory;
		this.lock = lock;
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

	public Object getVariable(boolean local) {
		return (local ? localVariables : globalVariables).get();
	}

	public void unsetVariable(VariableField path, boolean local) {
		(local ? localVariables : globalVariables).delete(path.getPath(this));
	}

	private File getFile(VariableField path) throws IOException {
		File file = directory;
		for(String segment : path.getPath(this)) {
			file = new File(file, segment);
		}
		file.getParentFile().mkdirs();
		lock.writeRecord(file);
		return file;
	}

	public OutputStream open(VariableField path) throws IOException {
		return new FileOutputStream(getFile(path));
	}

	public JsonGenerator openYaml(VariableField path) throws IOException {
		return factory.createGenerator(open(path));
	}

	public void onRecord(int type, int flags, int unknown) throws IOException {
		if(flags != 0) {
			localVariables.set("recordFlags", flags);
		}
		if(unknown != 0) {
			localVariables.set("unknown", unknown);
		}
	}

	public void onRecordEnd() throws IOException {
		localVariables.clear();
	}
}
