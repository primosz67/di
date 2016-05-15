package com.tahona.helper.manager;

import java.util.ArrayList;
import java.util.List;

public class ServiceManager {

	private static List<Object> list = new ArrayList<Object>();

	public static <T> void addService(T service) {
		list.add(service);
	}

	public static <T> T getService(Class<T> claz) {
		for (Object object : list) {
			if (claz.isInstance(object)) {
				return (T) object;
			}
		}

		return null;
	}
}
