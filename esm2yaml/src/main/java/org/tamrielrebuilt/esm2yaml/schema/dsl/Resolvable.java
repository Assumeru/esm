package org.tamrielrebuilt.esm2yaml.schema.dsl;

import java.util.stream.Stream;

import org.tamrielrebuilt.esm2yaml.schema.Context;

interface Resolvable {
	Stream<String> resolve(Context context);
}