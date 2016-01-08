package org.openxsp.cdn.connector.repo;

import java.util.Set;

public interface RetrieveCallback {
	
	void onRetrieve(Set<String> files);
	
	void onRetrieveError(String message);

}
