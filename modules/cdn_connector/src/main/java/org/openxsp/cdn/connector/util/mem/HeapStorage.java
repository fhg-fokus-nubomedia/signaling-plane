package org.openxsp.cdn.connector.util.mem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class HeapStorage<K,V> implements Storage<K,V>{
	
//	private static final String 
//			HZ_INSTANCE_NAME = "nubomedia.cdn.al!so8p5anfbee21a92d2"; //"random" id for the hazelcast instance
//	
//	public static final String 
//			MAP_EVENT_SESSIONS = "nubomedia.cdn.map.event.sessions.h30dnv835",
//			MAP_SESSION_HANDLER = "nubomedia.cdn.map.session.handler.d8nbal0cb3";
//	
	
	private Map<K,V> map;
	
	public HeapStorage(){
		map = new HashMap<>();
	}
	
	public HeapStorage(boolean concurrent){
		
		if(concurrent) map = new ConcurrentHashMap<>();
		
		else map = new HashMap<>();
	}
	
	
	@Override
	public void put(K key, V value){
	
		map.put(key, value);
	}
	
	@Override
	public V get(K key){
		return map.get(key);
		
	}
	
	@Override
	public V remove(K key){
		return  map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}
	
	@Override
	public Collection<V> values(){
		return map.values();
	}
}
