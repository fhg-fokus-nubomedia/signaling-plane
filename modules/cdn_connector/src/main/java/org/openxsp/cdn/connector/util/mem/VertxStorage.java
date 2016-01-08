package org.openxsp.cdn.connector.util.mem;

import java.util.Collection;
import java.util.Set;

import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class VertxStorage <K,V> implements Storage<K,V>{
	
	private static Logger log = LoggerFactory.getLogger(VertxStorage.class);
	
	private static final String HZ_INSTANCE_NAME = "nubomedia.cdn.al!so8p5anfbee21a92d2"; // "random" id for the hazelcast instance

	public static final String 
		MAP_EVENT_SESSIONS = "nubomedia.cdn.map.event.sessions.h30dnv835",
		MAP_SESSION_HANDLER = "nubomedia.cdn.map.session.handler.d8nbal0cb3";

	private HazelcastInstance hc;

	private final String mapName;

	public VertxStorage(String mapName) {

		this.mapName = mapName;
		
		hc = Hazelcast.getHazelcastInstanceByName(HZ_INSTANCE_NAME);
		if (hc == null) {
			Config hzConfig = new Config();
			hzConfig.setInstanceName(HZ_INSTANCE_NAME);
			log.v("Creating new hazelcast instance");
			hc = Hazelcast.newHazelcastInstance(hzConfig);
		}
	}

	@Override
	public void put(K key, V value) {

		//IMap<K, V> map = hc.getMap(mapName);
		
		MultiMap<K, V> map = hc.getMultiMap(mapName);

		boolean success = false;
		
		if (map == null) {
			log.d("Map not found");
			return;
		}
		try {
			map.lock(key);
			//remove existing values
			if(map.containsKey(key) && map.valueCount(key)>0){
				V v = map.get(key).iterator().next();
				map.remove(key, v);
			}
//			map.putAndUnlock(key, value);
			success = map.put(key, value);
			map.unlock(key);
		} catch (Exception e) {
			e.printStackTrace();
			map.unlock(key);
		}
		
		if(!success) log.w("Could not put "+key+" with vaule "+value+" into the map");
	}

	@Override
	public V get(K key) {
		
		
		MultiMap<K, V> map = hc.getMultiMap(mapName);
		if (map == null){
			
			return null;
		}
		
		//System.out.println(map.keySet());
			
		if (map.containsKey(key)){
			return map.get(key).iterator().next();
			//return map.getMapEntry(key).getValue();
		}
		//else System.out.println("key not found");
			

		return null;
	}

	@Override
	public V remove(K key) {

//		IMap<K, V> map = hc.getMap(mapName);
		MultiMap<K, V> map = hc.getMultiMap(mapName);
		
		
		if(map.containsKey(key) && map.valueCount(key)>0){
			map.lock(key);
			V value =  map.get(key).iterator().next();
//			V value = map.remove(key);
			map.remove(key, value);
			map.unlock(key);
			
			return value;
		}
		return null;
	}

	@Override
	public int size() {
//		IMap<K, V> map = hc.getMap(mapName);
		MultiMap<K, V> map = hc.getMultiMap(mapName);
		return map.size();
	}

	@Override
	public Set<K> keySet() {
		//IMap<K, V> map = hc.getMap(mapName);
		MultiMap<K, V> map = hc.getMultiMap(mapName);
		return map.keySet();
	}

	
	@Override
	public Collection<V> values() {
		//IMap<K, V> map = hc.getMap(mapName);
		MultiMap<K, V> map = hc.getMultiMap(mapName);
		return map.values();
	}
}
