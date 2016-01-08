package de.fhg.fokus.ims.core.utils.xml;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public interface XmlSerializable
{
	void serialize(XmlSerializer writer) throws IOException;
	
	void deserialize(XmlPullParser reader) throws XmlPullParserException, IOException;
}
