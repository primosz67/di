package com.tahona.commons;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiMap<T1, T2> {

	private HashMap<T1, Set<T2>> map = new HashMap<T1, Set<T2>>();

	public void put(T1 key, T2 value) {
		if (false == map.containsKey(key)) {
			map.put(key, new HashSet<T2>());
		}
		map.get(key).add(value);
	}

	public Map<T1, Collection<T2>> asMap() {
		return new HashMap<T1, Collection<T2>>(map);
	}

}
