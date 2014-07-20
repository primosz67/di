package com.tahona.framework;

@Deprecated
public interface Command<T> {
	interface Result {
	}

	public Result execute(T type);

}
