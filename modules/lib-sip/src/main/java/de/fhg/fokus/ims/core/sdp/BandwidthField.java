package de.fhg.fokus.ims.core.sdp;

/**
 * SDP bandwidth field.
 * <p>
 * bandwidth-fields = "b=" (b-value) | b-field CRLF
 * 
 */
public class BandwidthField extends SdpField
{

	public BandwidthField(String value)
	{
		super('b', value);
	}

	public BandwidthField(SdpField sf)
	{
		super(sf);
	}

	public Object clone()
	{
		return new BandwidthField(getValue());
	}

}
