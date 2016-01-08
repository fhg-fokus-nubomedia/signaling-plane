package de.fhg.fokus.ims.core.auth;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.text.ParseException;

import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

//import de.fhg.fokus.ims.core.auth.AKAISIM.DigestAKAResponse;
import de.fhg.fokus.ims.core.auth.AKA.DigestAKAResponse;
import de.fhg.fokus.ims.core.auth.MD5.DigestMD5;
import de.fhg.fokus.ims.core.auth.MD5.DigestMD5Response;

public class Authenticator
{	
	private static StackLogger logger = CommonLogger.getLogger(Authenticator.class);
	
	/** ****** Authentication ****************** */
	private byte[] OP = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.hexa2bin("00000000000000000000000000000000");
	//private byte[] OP = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.hexa2bin("766f6461666f6e652e636f6d00000000");
	private byte[] AMF = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.hexa2bin("0000");
	private byte[] AMFStar = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.hexa2bin("0000");
	private byte[] SQN = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.hexa2bin("000000000000");
	//private byte[] SQN = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.hexa2bin("000000000000");
	private boolean useAK = true;
	private boolean simulateISIM = false;
	private boolean realISIM = false;
	
	private int indLen = 5;
	private int maxIndexNo = (1 << 5);
	private int delta = (1 << 28);
	private int L = (1 << 5);
	private byte[][] sqnVector = null;
	private int sqnVectorCurrentIndex = 0;
	private HeaderFactory headerFactory;
	private String domain;
	private String privateId;
	private String secretKey;
	private String lastRecievedNonce=null;
	private String lastRecievedResponse=null;
	private DigestAKAResponse respAKA = null;
	public SipURI domainURI;
	
	public Authenticator(HeaderFactory headerFactory, AddressFactory addressFactory, String domain, String privateId, String secretKey)
	{
		this.headerFactory = headerFactory;
		this.domain = domain;
		this.secretKey = secretKey;
		this.privateId = privateId;
		try {
			this.domainURI = (SipURI) addressFactory.createURI("sip:"+domain);
		} catch (ParseException e) {
			logger.logError("Error creating the realmURI", e);
		}
	}
	
	public Authenticator(HeaderFactory headerFactory, AddressFactory addressFactory, String domain, String privateId, String secretKey, boolean realISIM)
	{
		this.headerFactory = headerFactory;
		this.domain = domain;
		this.secretKey = secretKey;
		this.privateId = privateId;
		this.realISIM = realISIM;
		try {
			this.domainURI = (SipURI) addressFactory.createURI("sip:"+domain);
		} catch (ParseException e) {
			logger.logError("Error creating the realmURI", e);
		}
	}
	
	public AuthorizationHeader authorize(Request request, Response response) throws AuthenticationException
	{
		if (secretKey == null)
			throw new AuthenticationException("No secret key set!");
		
		AuthorizationHeader authHeader = null;
		WWWAuthenticateHeader wwwauthHeader = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);
		SipURI realmURI = (SipURI) request.getRequestURI();

		try
		{
			authHeader = headerFactory.createAuthorizationHeader("Digest");
			authHeader.setUsername(privateId);			
			authHeader.setRealm(realmURI.getHost());			
			authHeader.setNonce(wwwauthHeader.getNonce());
			authHeader.setURI(realmURI);			
			String opaque = wwwauthHeader.getOpaque();
			if (opaque != null)
				authHeader.setOpaque(opaque);

			String algorithm = wwwauthHeader.getAlgorithm();
			if ((algorithm == null || algorithm.length() == 0))
			{
				algorithm = DigestMD5.MD5;
			}
			authHeader.setParameter("algoritm", algorithm);
			authHeader.setResponse("-");
			//authHeader.setAlgorithm(algorithm);
			String qop = wwwauthHeader.getQop();
			if (qop != null && qop.indexOf("auth-int") >= 0)
				qop = "auth-int";
			else if (qop != null && qop.indexOf("auth") >= 0)
				qop = "auth";
			else
				qop = null;

			if ((algorithm.equalsIgnoreCase(de.fhg.fokus.ims.core.auth.AKA.DigestAKA.AKA_V1) || algorithm.equalsIgnoreCase(de.fhg.fokus.ims.core.auth.AKA.DigestAKA.AKA_V2)))
			{
				/* decides if authentication shall use the procedure of a real sim card with an isim profile*/
				if(!this.realISIM) {
					respAKA = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.getResponse(wwwauthHeader.getNonce(), secretKey, SQN, OP, AMF, AMFStar, useAK, simulateISIM, sqnVector, indLen,
							delta, L);
					if (respAKA.isUnauthorizedChallenge())
					{
						logger.logInfo("Received 401 for " + request.getMethod()
								+ " but Server is not authorized. Man-in-the-middle attack?");
						throw new AuthenticationException("Server is not authorized");
					} else
					{
						if (!respAKA.isSynchronization())
						{
							// we have to perform synchronization with the HSS!
							System.out.println("Synch of SQN-HE (HSS) with SQN-MS (IMS Client)!");
							authHeader.setResponse(respAKA.getResponse());
							authHeader.setParameter("auts", "\"" + respAKA.getAuts() + "\"");
							return authHeader;
						} else
						{
							// we took the received SQN value and memorize it in
							// profile (in SQN if is the maximum SQN and in
							// sqnVector)

							if (simulateISIM)
							{
								// we add the SQN to the sqnVector
								sqnVector[sqnVectorCurrentIndex] = respAKA.getNewSQN();
								incSqnVectorIndex();
								if (respAKA.isSqnMS())
								{
									// the SQN_MS is changed, as the new one is
									// greater
									SQN = respAKA.getNewSQN();
								}
							} else
							{
								SQN = respAKA.getNewSQN();
							}
						}
					}

					/* old AKA */
					// authHeader.setResponse(respAKA.response);
					/* new AKA */
					String contentBody = null;
					if (request.getContent() != null)
						contentBody = request.getContent().toString();
					else
						contentBody = "";
					String uri = realmURI.toString();
					DigestMD5Response respMD5 = de.fhg.fokus.ims.core.auth.AKA.DigestAKA.getMD5Response(respAKA, wwwauthHeader.getAlgorithm(),
							wwwauthHeader.getNonce(), 1, getNewCNonce(), privateId,
							domain, request.getMethod(), uri, contentBody, qop);
					authHeader.setResponse(respMD5.response);
					if (respMD5.qop != null)
					{
						authHeader.setQop(respMD5.qop);
						authHeader.setNonceCount(respMD5.nonceCount);
						authHeader.setCNonce(respMD5.cNonce);
					}
				} 
//					else {
//					respAKA = 
//						de.fhg.fokus.ims.core.auth.AKAISIM.DigestAKA.getResponse(wwwauthHeader.getNonce(), 
//								secretKey, SQN, OP, AMF, AMFStar, useAK, simulateISIM, sqnVector, indLen, delta, L);
//					
//					//System.out.println(de.fhg.fokus.ims.core.auth.AKA.DigestAKA.getResponse(wwwauthHeader.getNonce(), 
//					//		secretKey, SQN, OP, AMF, AMFStar, useAK, simulateISIM, sqnVector, indLen, delta, L).toString());
//					
//					if (respAKA.isUnauthorizedChallenge())
//					{
//						logger.logInfo("Received 401 for " + request.getMethod()
//								+ " but Server is not authorized. Man-in-the-middle attack?");
//						
//						//throw new AuthenticationException("Server is not authorized.");
//					} else
//					{
//						if (!respAKA.isSynchronization())
//						{
//							// we have to perform synchronization with the HSS!
//							System.out.println("Synch of SQN-HE (HSS) with SQN-MS (IMS Client)!");
//							authHeader.setResponse(respAKA.getResponse());
//							authHeader.setParameter("auts", "\"" + respAKA.getAuts() + "\"");
//							return authHeader;
//						} else
//						{
//							// we took the received SQN value and memorize it in
//							// profile (in SQN if is the maximum SQN and in
//							// sqnVector)
//
//							if (simulateISIM)
//							{
//								// we add the SQN to the sqnVector
//								sqnVector[sqnVectorCurrentIndex] = respAKA.getNewSQN();
//								incSqnVectorIndex();
//								if (respAKA.isSqnMS())
//								{
//									// the SQN_MS is changed, as the new one is
//									// greater
//									SQN = respAKA.getNewSQN();
//								}
//							} else
//							{
//								SQN = respAKA.getNewSQN();
//							}
//						}
//					}
//
//					/* old AKA */
//					// authHeader.setResponse(respAKA.response);
//					/* new AKA */
//					String contentBody = null;
//					if (request.getContent() != null)
//						contentBody = request.getContent().toString();
//					else
//						contentBody = "";
//					String uri = realmURI.toString();
//					DigestMD5Response respMD5 = de.fhg.fokus.ims.core.auth.AKAISIM.DigestAKA.getMD5Response(respAKA, wwwauthHeader.getAlgorithm(),
//							wwwauthHeader.getNonce(), 1, getNewCNonce(), privateId,
//							domain, request.getMethod(), uri, contentBody, qop);
//					authHeader.setResponse(respMD5.response);
//					if (respMD5.qop != null)
//					{
//						authHeader.setQop(respMD5.qop);
//						authHeader.setNonceCount(respMD5.nonceCount);
//						authHeader.setCNonce(respMD5.cNonce);
//					}
//				}

			} else
			{
				/* fallback to MD5 */
				String contentBody = null;
				if (request.getContent() != null)
					contentBody = request.getContent().toString();
				else
					contentBody = "";
				String uri = realmURI.toString();
				DigestMD5Response respMD5 = DigestMD5.getResponse(wwwauthHeader.getAlgorithm(), wwwauthHeader
						.getNonce(), privateId,
						domain, secretKey.getBytes(), 1,
						getNewCNonce(), request.getMethod(), uri, contentBody, qop);
				authHeader.setResponse(respMD5.response);
				if (respMD5.qop != null)
				{
					authHeader.setQop(respMD5.qop);
					authHeader.setNonceCount(respMD5.nonceCount);
					authHeader.setCNonce(respMD5.cNonce);
				}
			}
		} catch (ParseException e)
		{
			logger.logDebug("Error generating authorization header");
		}
		setLastRecievedNonce(authHeader.getNonce());
		setLastRecievedResponse(authHeader.getResponse());
		return authHeader;
	}
	
	
	
	public void incSqnVectorIndex()
	{
		this.sqnVectorCurrentIndex = (this.sqnVectorCurrentIndex + 1) % this.maxIndexNo;
	}

	private static byte[] hexa =
	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private String getNewCNonce()
	{
		StringBuffer stb = new StringBuffer();
		for (int i = 0; i < 8; i++)
			stb.append(hexa[(int) (Math.random() * 16)]);
		return stb.toString();
	}

	public Header getEmptyAuthenticationHeader()
	{
		AuthorizationHeader authHeader;
		try
		{
			authHeader = headerFactory.createAuthorizationHeader("Digest");
			authHeader.setUsername(privateId);
			authHeader.setRealm(domain);			
			if(lastRecievedNonce != null || lastRecievedResponse != null)
			{
				authHeader.setNonce(lastRecievedNonce);
				authHeader.setResponse(lastRecievedResponse);
			}
			else
			{
				authHeader.setNonce("");
				authHeader.setResponse("");
			}
			authHeader.setURI(domainURI);
			return authHeader;
		} catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}					
	}
	
	public DigestAKAResponse getRespAKA()
	{
	  return respAKA;
	}	
	
	private void setLastRecievedNonce(String lastRecievedNonce) 
	{
		this.lastRecievedNonce = lastRecievedNonce;
	}

	private void setLastRecievedResponse(String lastRecievedResponse) 
	{
		this.lastRecievedResponse = lastRecievedResponse;
	}
	
}
