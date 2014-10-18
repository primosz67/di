package com.tahona.commons;

public class Preconditions {

	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
	}

	public static void notNull(Object object) {
		notNull(object, "Object cannot be null!");
	}

}
