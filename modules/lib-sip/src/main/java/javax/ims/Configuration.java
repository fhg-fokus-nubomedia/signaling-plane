package javax.ims;

import java.util.HashMap;
import java.util.Iterator;

/**
 * The Configuration class realizes dynamic installation of IMS applications
 * with the pair of methods setRegistry and removeRegistry. While an IMS
 * application is installed, it has an associated Registry. The Registry is a
 * small data base that stores IMS application properties. See further the
 * package documentation and the chapter on the Registry and its role in static
 * and dynamic installation.
 * 
 * <p>
 * slight changes to the jsr281 specification here. In place of using String[][]
 * for registry, we used java.util.Properties to store the properties of each
 * application
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * 
 */
public class Configuration
{
	private HashMap configurations;
	private static Configuration instance;

	public Configuration()
	{
		configurations = new HashMap();
	}

	/**
	 * Returns a Configuration that enables dynamic installation of IMS
	 * applications.
	 * 
	 * @return - a Configuration instance that enables dynamic installation of
	 *         IMS applications
	 */
	public static Configuration getConfiguration()
	{
		if (instance == null)
			instance = new Configuration();
		return instance;
	}

	/**
	 * Sets the registry for an IMS application and binds it to a parent Java
	 * application. Any previous registry and binding for that IMS application
	 * is deleted, including all properties. The new registry is defined in the
	 * registry argument.
	 * 
	 * @param appId -
	 *            the application id
	 * @param classname -
	 *            the classname of the Java application that the IMS application
	 *            is bound to
	 * @param registry -
	 *            a property object, specifying key and value(s)
	 */
	public void setRegistry(String appId, String classname, String[][] registry)
	{
		if (configurations == null)
			configurations = new HashMap();
		configurations.put(appId, registry);
	}

	/**
	 * Returns the Properties for an IMS application with the specified appId
	 * 
	 * @param appId -
	 *            the application id
	 * @return the properties for the IMS application specified by the appId
	 *         argument
	 * @throws IllegalArgumentException -
	 *             if appId is not set in the registry
	 */
	public String[][] getRegistry(String appId) throws IllegalArgumentException
	{
		return (String[][]) configurations.get(appId);
	}

	/**
	 * Removes the registry for the IMS application and deletes the binding to a
	 * Java application. This applies to both static and dynamic installations.
	 * If this method is invoked, the local endpoint will no longer be able to
	 * create new services with the application identity specified by the appId
	 * argument, this does not affect services that are already created.
	 * 
	 * @param appId -
	 *            the application id
	 * @throws IllegalArgumentException -
	 *             if appId is not set in the registry
	 */
	public void removeRegistry(String appId) throws IllegalArgumentException
	{
		if (appId == null)
			return;
		if (!configurations.containsKey(appId))
			throw new IllegalArgumentException("Configuration.removeRegistry(): Application Identifier not set in the registry.");
		
		String[][] config = (String[][]) configurations.remove(appId);
	}

	/**
	 * Returns all AppId for the local endpoint. An empty string array will be
	 * returned if no AppId could be retrieved
	 * 
	 * @return a string array containing all AppId for the local endpoint
	 */
	public String[] getLocalAppIds()
	{
		String[] localAppIds = new String[configurations.size()];
		synchronized (configurations)
		{
			Iterator iter = configurations.keySet().iterator();
			int index = 0;
			while (iter.hasNext())
			{
				String key = (String) iter.next();
				localAppIds[index] = key;
				index++;
			}
		}
		return localAppIds;
	}
}
