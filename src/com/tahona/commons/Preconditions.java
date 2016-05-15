package com.tahona.commons;

@Deprecated
public class Preconditions {

	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
	}

	public static void notNull(Object object) {
		notNull(object, "Object cannot be null!");
	}

	public static void checkFalse(Boolean falseValue) {
		notNull(falseValue);
		if (falseValue) {
			throw new IllegalArgumentException("value shoud be false");
		}
	}

}
