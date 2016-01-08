package eu.nubomedia.af.kurento.util.mem;

import java.util.Set;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class VertxStorage <K,V> implements Storage<K,V>{
	private static final String HZ_INSTANCE_NAME = "nubomedia.cdn.al!so8p5anfbee21a92d2"; // "random" id for the hazelcast instance

	public static final String 
		MAP_EVENT_SESSIONS = "nubomedia.cdn.map.event.sessions.h30dnv835",
		MAP_SESSION_HANDLER = "nubomedia.cdn.map.session.handler.d8nbal0cb3";

	private HazelcastInstance hc;

	private final String mapName;

	public VertxStorage(String mapName) {

		this.mapName = mapName;
		
		Config hzConfig = new Config();
		hzConfig.setInstanceName(HZ_INSTANCE_NAME);
		hc = Hazelcast.getHazelcastInstanceByName(HZ_INSTANCE_NAME);
		if (hc == null) {
			System.out.println("Creating new hazelcast instance");
			hc = Hazelcast.newHazelcastInstance(hzConfig);
		}
	}

	@Override
	public void put(K key, V value) {

		IMap<K, V> map = hc.getMap(mapName);

		if (map == null) {
			return;
		}
		try {
			map.lock(key);
			map.putAndUnlock(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			map.unlock(key);
		}
	}

	@Override
	public V get(K key) {

		IMap<K, V> map = hc.getMap(mapName);
		if (map == null)
			return null;
		if (map.containsKey(key))
			return map.getMapEntry(key).getValue();

		return null;
	}

	@Override
	public V remove(K key) {

		IMap<K, V> map = hc.getMap(mapName);
		map.lock(key);
		V value = map.remove(key);
		map.unlock(key);

		return value;
	}

	@Override
	public int size() {
		IMap<K, V> map = hc.getMap(mapName);
		return map.size();
	}

	@Override
	public Set<K> keySet() {
		IMap<K, V> map = hc.getMap(mapName);
		return map.keySet();
	}

	
}
