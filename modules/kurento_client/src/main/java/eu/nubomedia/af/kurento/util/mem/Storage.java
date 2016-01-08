package eu.nubomedia.af.kurento.util.mem;

import java.util.Set;


public interface Storage<K,V> {

	void put(K key, V value);
	
	V get(K key);
	
	V remove(K key);
	
	int size();
	
	Set<K> keySet();

}
