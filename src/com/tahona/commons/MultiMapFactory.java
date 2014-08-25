package com.tahona.commons;

public class MultiMapFactory {

	public static <T1, T2> MultiMap<T1, T2> create() {
		return new MultiMap<T1, T2>();
	}

}
