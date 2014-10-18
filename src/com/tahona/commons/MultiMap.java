package com.tahona.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiMap<T1, T2> {

	private final HashMap<T1, List<T2>> map = new HashMap<T1, List<T2>>();

	public void put(final T1 key, final T2 value) {
		if (false == map.containsKey(key)) {
			map.put(key, new ArrayList<T2>());
		}
		map.get(key).add(value);
	}

	public Map<T1, Collection<T2>> asMap() {
		return new HashMap<T1, Collection<T2>>(map);
	}

	public List<T2> getAsList(final T1 key) {
		return new ArrayList<T2>(map.get(key));
	}

	public boolean containsKey(T1 key) {
		return map.containsKey(key);
	}

	public T2 getFirst(T1 key) {
		return getAsList(key).get(0);
	}

	public void clear() {
		map.clear();
	}

}
