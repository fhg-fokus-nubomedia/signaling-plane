package de.fhg.fokus.ims.core;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holds a list of feature tags and provides matching capabilities.
 * 
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public class FeatureTagSet
{
	private ArrayList features = new ArrayList();

	/**
	 * Creates an empty feature set.
	 */
	public FeatureTagSet()
	{

	}

	/**
	 * Creates a feature tag set with all features in the given array.
	 * @param features 
	 */
	public FeatureTagSet(FeatureTag[] features)
	{
		if (features == null || features.length == 0)
			return;
		
		for (int i = 0; i < features.length; i++)
		{
			this.features.add(features[i]);
		}
	}

	/**
	 * Adds new feature tag to the list.
	 * @param tag The feature tag to add.
	 */
	public void add(FeatureTag tag)
	{
		features.add(tag);
	}

	/**
	 * Removes feature tag from the list.
	 * @param tag The feature tag to remove.
	 */
	public void remove(FeatureTag tag)
	{
		features.remove(tag);
	}

	/**
	 * Removes all feature tags from the list.
	 */
	public void clear()
	{
		features.clear();
	}
	
	/**
	 * Provides an iterator for iterating over all feature tags 
	 * @return the iterator 
	 */
	public Iterator iterator()
	{
		return features.iterator();
	}

	/**
	 * Returns a copy of all feature tags contained in this set
	 * @return
	 */
	public FeatureTag[] toArray()
	{
		return (FeatureTag[]) features.toArray(new FeatureTag[features.size()]);
	}

	/**
	 * This method matches all feature tags in this set against the features
	 * in the target. The result gives a value between 0.0 and 1.0
	 * indicating the number of feature matched. For each feature that
	 * matched a value of 1/n - where n is the number of features in this
	 * set - is added to the result.
	 * 
	 * For each feature tag the {@link FeatureTag#match(FeatureTag)} method
	 * is called.
	 * 
	 * @param target
	 *            The set of features to match this set against.
	 * @return A value between 0.0 and 1.0 which gives a value for matching.
	 */
	public float match(FeatureTagSet target)
	{
		float factor = 1f / features.size();
		float result = 0f;

		for (int i = 0; i < features.size(); i++)
		{
			FeatureTag localFeature = (FeatureTag) features.get(i);

			for (int j = 0; j < target.features.size(); j++)
			{
				FeatureTag targetFeature = (FeatureTag) target.features.get(j);

				if (localFeature.match(targetFeature))
				{
					result += factor;
					break;
				}
			}
		}
		return result;
	}
}
