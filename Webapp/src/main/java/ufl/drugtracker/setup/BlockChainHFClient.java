package ufl.drugtracker.setup;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

public class BlockChainHFClient {

	/**
	 * Blockchain Fabric client instance
	 */
	private static BlockChainHFClient instance;

	String currentUsersHomeDir = System.getProperty("user.home");

	/**
	 * Client instance constructor
	 */
	private BlockChainHFClient() {
	}

	/**
	 * Returns an instance of the Fabric client
	 * 
	 * @return instance
	 */
	public static synchronized BlockChainHFClient getInstance() {
		if (instance == null) {
			instance = new BlockChainHFClient();
		}
		return instance;
	}

	final HFClient hfClient = HFClient.createNewInstance();
	Channel channel;
	static String ROOT_MSP_ID = "manufacturerMSP"; //Default Admin for the network
	final static String PEER_ADMIN = "PeerAdmin";
	List<Peer> networkPeers = new ArrayList<Peer>();
	List<Peer> adminPeer = new ArrayList<Peer>();

	/**
	 * Get channel instance
	 * 
	 * @return channel
	 */
	public Channel getCh() {
		return channel;
	}

	public List<Peer> getAdminPeer() {
		return adminPeer;
	}

	/**
	 * Get HF client
	 * 
	 * @return HF client
	 */
	public HFClient getClient() {
		return hfClient;
	}

	// In this function, the channel, the peers and orderers are accessed through
	// the hfClient object
	public void getChannel() throws InvalidArgumentException, TransactionException, ProposalException {
		// Get channel object through the Fabric SDK Client object
		channel = hfClient.newChannel("pharmachannel");

		// Customer Peer
		Properties peerProperties = new Properties();
		peerProperties.setProperty("pemFile", currentUsersHomeDir
				+ "\\crypto-config\\peerOrganizations\\custPatient.state.com\\peers\\peer0.custPatient.state.com\\tls\\server.crt");
		peerProperties.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
		peerProperties.setProperty("hostnameOverride", "peer0.custPatient.state.com");
		peerProperties.setProperty("sslProvider", "openSSL");
		peerProperties.setProperty("negotiationType", "TLS");
		peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
		Peer peer = hfClient.newPeer("peer0.custPatient.state.com", "grpcs://localhost:10051", peerProperties);

		// FDA Peer
		Properties peer2Properties = new Properties();
		peer2Properties.setProperty("pemFile", currentUsersHomeDir
				+ "\\crypto-config\\peerOrganizations\\fda.state.com\\peers\\peer0.fda.state.com\\tls\\server.crt");
		peer2Properties.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
		peer2Properties.setProperty("hostnameOverride", "peer0.fda.state.com");
		peer2Properties.setProperty("sslProvider", "openSSL");
		peer2Properties.setProperty("negotiationType", "TLS");
		peer2Properties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
		Peer peer2 = hfClient.newPeer("peer0.fda.state.com", "grpcs://localhost:8051", peer2Properties);

		// Manufacturer Peer
		Properties peer3Properties = new Properties();
		peer3Properties.setProperty("pemFile", currentUsersHomeDir
				+ "\\crypto-config\\peerOrganizations\\manufacturer.state.com\\peers\\peer0.manufacturer.state.com\\tls\\server.crt");
		peer3Properties.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
		peer3Properties.setProperty("hostnameOverride", "peer0.manufacturer.state.com");
		peer3Properties.setProperty("sslProvider", "openSSL");
		peer3Properties.setProperty("negotiationType", "TLS");
		peer3Properties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
		Peer peer3 = hfClient.newPeer("peer0.manufacturer.state.com", "grpcs://localhost:9051", peer3Properties);

		// Hospital/Pharmacy Peer
		Properties peer4Properties = new Properties();
		peer4Properties.setProperty("pemFile", currentUsersHomeDir
				+ "\\crypto-config\\peerOrganizations\\hospitalPharma.state.com\\peers\\peer0.hospitalPharma.state.com\\tls\\server.crt");
		peer4Properties.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
		peer4Properties.setProperty("hostnameOverride", "peer0.hospitalPharma.state.com");
		peer4Properties.setProperty("sslProvider", "openSSL");
		peer4Properties.setProperty("negotiationType", "TLS");
		peer4Properties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
		Peer peer4 = hfClient.newPeer("peer0.hospitalPharma.state.com", "grpcs://localhost:7051", peer4Properties);

		// Govt Entity Peer
		Properties peer5Properties = new Properties();
		peer5Properties.setProperty("pemFile", currentUsersHomeDir
				+ "\\crypto-config\\peerOrganizations\\usgovt.state.com\\peers\\peer0.usgovt.state.com\\tls\\server.crt");
		peer5Properties.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
		peer5Properties.setProperty("hostnameOverride", "peer0.usgovt.state.com");
		peer5Properties.setProperty("sslProvider", "openSSL");
		peer5Properties.setProperty("negotiationType", "TLS");
		peer5Properties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
		Peer peer5 = hfClient.newPeer("peer0.usgovt.state.com", "grpcs://localhost:12051", peer5Properties);

		// Medic/Doctor peer
		Properties peer6Properties = new Properties();
		peer6Properties.setProperty("pemFile", currentUsersHomeDir
				+ "\\crypto-config\\peerOrganizations\\medic.state.com\\peers\\peer0.medic.state.com\\tls\\server.crt");
		peer6Properties.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
		peer6Properties.setProperty("hostnameOverride", "peer0.medic.state.com");
		peer6Properties.setProperty("sslProvider", "openSSL");
		peer6Properties.setProperty("negotiationType", "TLS");
		peer6Properties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
		Peer peer6 = hfClient.newPeer("peer0.medic.state.com", "grpcs://localhost:11051", peer6Properties);

		// Orderer
		Properties ordererProperties = new Properties();
		ordererProperties.setProperty("pemFile", currentUsersHomeDir
				+ "\\crypto-config\\ordererOrganizations\\state.com\\orderers\\orderer.state.com\\tls\\server.crt");
		ordererProperties.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
		ordererProperties.setProperty("hostnameOverride", "orderer.state.com");
		ordererProperties.setProperty("sslProvider", "openSSL");
		ordererProperties.setProperty("negotiationType", "TLS");
		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] { 5L, TimeUnit.MINUTES });
		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] { 8L, TimeUnit.SECONDS });
		Orderer orderer = hfClient.newOrderer("orderer.state.com", "grpcs://localhost:7050", ordererProperties);

		// Add the peers and orderer to the channel object and initialize
		// Now you have access to the network
		channel.addPeer(peer);
		channel.addPeer(peer2);
		channel.addPeer(peer3);
		channel.addPeer(peer4);
		channel.addPeer(peer5);
		channel.addPeer(peer6);
		channel.addOrderer(orderer);
		channel.initialize();

		adminPeer.add(peer3);
		networkPeers.add(peer);
		networkPeers.add(peer2);
		networkPeers.add(peer3);
		networkPeers.add(peer4);
		networkPeers.add(peer5);
		networkPeers.add(peer6);

	}

	// This method sets up the crypto context for the client
	public void setupCrypto() throws CryptoException, InvalidArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		hfClient.setUserContext(new User() {
			public String getName() {
				return PEER_ADMIN;
			}

			public Set<String> getRoles() {
				return null;
			}

			public String getAccount() {
				return null;
			}

			public String getAffiliation() {
				return null;
			}

			public Enrollment getEnrollment() {
				return new Enrollment() {
					public PrivateKey getKey() {
						PrivateKey privateKey = null;
						File privateKeyFile = findFileSk(currentUsersHomeDir
								+ "\\crypto-config\\peerOrganizations\\manufacturer.state.com\\users\\Admin@manufacturer.state.com\\msp\\keystore");
						try {
							privateKey = getPrivateKeyFromBytes(
									IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
						} catch (IOException e) {
							e.printStackTrace();
						} catch (NoSuchProviderException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvalidKeySpecException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						return privateKey;
					}

					public String getCert() {

						String certificate = null;
						try {
							File certificateFile = new File(currentUsersHomeDir
									+ "\\crypto-config\\peerOrganizations\\manufacturer.state.com\\users\\Admin@manufacturer.state.com\\msp\\signcerts\\Admin@manufacturer.state.com-cert.pem");
							certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)),
									"UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return certificate;
					}
				};
			}

			public String getMspId() {
				return ROOT_MSP_ID;
			}
		});

	}

	// This function is used to set crypto context for a specific peer when they
	// login
	public void setupCryptoForPeer(File convFile, String peer)
			throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		ROOT_MSP_ID = peer + "MSP";
		hfClient.setUserContext(new User() {
			public String getName() {
				return PEER_ADMIN;
			}

			public Set<String> getRoles() {
				return null;
			}

			public String getAccount() {
				return null;
			}

			public String getAffiliation() {
				return null;
			}

			public Enrollment getEnrollment() {
				return new Enrollment() {
					public PrivateKey getKey() {
						PrivateKey privateKey = null;
						File privateKeyFile = findFileSk(currentUsersHomeDir + "\\crypto-config\\peerOrganizations\\"
								+ peer + ".state.com\\users\\Admin@" + peer + ".state.com\\msp\\keystore");
						try {
							privateKey = getPrivateKeyFromBytes(
									IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
						} catch (IOException e) {
							e.printStackTrace();
						} catch (NoSuchProviderException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvalidKeySpecException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						return privateKey;
					}

					public String getCert() {

						String certificate = null;
						try {
							certificate = new String(IOUtils.toByteArray(new FileInputStream(convFile)), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
							convFile.delete();
						}
						return certificate;
					}
				};
			}

			public String getMspId() {
				return ROOT_MSP_ID;
			}
		});

	}

	//Utility Method
	public static PrivateKey getPrivateKeyFromBytes(byte[] data)
			throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
		final Reader pemReader = new StringReader(new String(data));
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		final PrivateKeyInfo pemPair;
		try (PEMParser pemParser = new PEMParser(pemReader)) {
			pemPair = (PrivateKeyInfo) pemParser.readObject();
		}

		PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.getPrivateKey(pemPair);

		return privateKey;
	}

  //Utility Method
	public static File findFileSk(String directorys) {

		File directory = new File(directorys);

		File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

		if (null == matches) {
			throw new RuntimeException(
					format("Matches returned null does %s directory exist?", directory.getAbsoluteFile().getName()));
		}

		if (matches.length != 1) {
			throw new RuntimeException(format("Expected in %s only 1 sk file but found %d",
					directory.getAbsoluteFile().getName(), matches.length));
		}

		return matches[0];
	}

}
