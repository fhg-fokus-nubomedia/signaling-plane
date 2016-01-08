package de.fhg.fokus.ims.core.auth.AKA;

public interface DigestAKAResponse {

	public String getResponse();

	public String getAuts();

	public byte[] getRes();

	public byte[] getCk();

	public byte[] getIk();
	
	public boolean isSynchronization();

	public boolean isUnauthorizedChallenge();

	public byte[] getNewSQN();

	public boolean isSqnMS();

	
	public String toString();
}
