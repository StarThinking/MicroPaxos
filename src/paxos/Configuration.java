package paxos;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;

import paxos.network.NodeIdentifier;
import paxos.node.Client;

public class Configuration {

	/*
	 * This is usually the max number of nodes per role.
	 * For example, if you have 1000 clients, 5 acceptors, and
	 * 5 learners, then set this number to 1000.
	 */
	public static final int MAX_NODE_ID = 1000;

	/* timeout in millisecond */
	public static long pingTimeout;

	public static int checkpointInterval;  //checkpoint interval

	public static int maxLogCacheSize;     //max cache size

	public static int numClients;
	public static int numLearners;
	public static int numAcceptors;

    public final static HashMap<Integer, NodeIdentifier> clientIDs = new HashMap<>(); //index [1,...numClients]
    public final static HashMap<Integer, NodeIdentifier> acceptorIDs = new HashMap<>();
    public final static HashMap<Integer, NodeIdentifier> learnerIDs = new HashMap<>();
    
    public static HashMap<Integer, NodeIdentifier> getClientIDs() {
    	return clientIDs;
    }
    
    public static HashMap<Integer, NodeIdentifier> getAcceptorIDs() {
    	return acceptorIDs;
    }
    
    public static HashMap<Integer, NodeIdentifier> getLearnerIDs() {
    	return learnerIDs;
    }
    
    public static int getAcceptorSize() {
    	return acceptorIDs.size();
    }

	public static int debugLevel;

	public static int testIndexA;
	public static int testIndexB;

	public static PropertiesConfiguration gpConf;

	/* Logger configuration */
	private static HashMap<String, Integer> activeLogger = new HashMap<String, Integer>();
	
	public static boolean isLoggerActive(String name){
		return activeLogger.containsKey(name);
	}
	
	public static int getLoggerLevel(String name){
		return activeLogger.get(name);
	}
	
	public static void addActiveLogger(String name, int level){
		activeLogger.put(name, level);
	}
	
	public static void removeActiveLogger(String name){
		activeLogger.remove(name);
	}
	
	/* Network configuration */
	private static HashMap<NodeIdentifier, InetSocketAddress> nodes =
			new HashMap<NodeIdentifier, InetSocketAddress>();
	
	public static InetSocketAddress getNodeAddress(NodeIdentifier node){
		return nodes.get(node);
	}
	
	public static void addNodeAddress(NodeIdentifier node, InetSocketAddress address){
		nodes.put(node, address);
	}

    public static void initConfiguration(String confFile) throws ConfigurationException {
        gpConf = new PropertiesConfiguration(confFile);

		configNodeAddress(NodeIdentifier.Role.CLIENT, "client", gpConf.getInt("clientPort"));
        configNodeAddress(NodeIdentifier.Role.ACCEPTOR, "acceptor", gpConf.getInt("acceptorPort"));
        configNodeAddress(NodeIdentifier.Role.LEARNER, "learner", gpConf.getInt("learnerPort"));
		numClients = clientIDs.size();
		numAcceptors = acceptorIDs.size();
		numLearners = learnerIDs.size();

		pingTimeout = gpConf.getInt("pingTimeout", 200);
		checkpointInterval = gpConf.getInt("checkpointInterval", 2000);
		debugLevel = gpConf.getInt("debugLevel", 1); //default value is INFO

		maxLogCacheSize = gpConf.getInt("maxLogCacheSize", 400000); //default 400k

		testIndexA = gpConf.getInt("testIndexA", -1);
		testIndexB = gpConf.getInt("testIndexB", -1);
    }

	public static void showNodeConfig() {
		System.out.format("\n== show node configuration ==\n");
		System.out.format("%d clients %s\n", numClients, clientIDs.values());
		System.out.format("%d acceptors %s\n", numAcceptors, acceptorIDs.values());
		System.out.format("%d learners %s\n\n", numLearners, learnerIDs.values());
	}

    public static void configNodeAddress(NodeIdentifier.Role role, String keys, int startPort) {
        Iterator<String> names = gpConf.getKeys(keys);
        int idx = 1;
		while (names.hasNext()) {
			String name = names.next();
            InetSocketAddress iAddress = new InetSocketAddress(gpConf.getString(name), startPort+idx);
			NodeIdentifier node = new NodeIdentifier(role, idx);
		    addNodeAddress(node, iAddress);
			//System.out.format("=> add NodeAddress <%s, %s>, IDs <%d, %s>\n", 
            //        node, nodes.get(node), idx, node);

			if (role == NodeIdentifier.Role.CLIENT) {
				clientIDs.put(idx, node);
			} else if (role == NodeIdentifier.Role.ACCEPTOR) {
				acceptorIDs.put(idx, node);
			} else {
				learnerIDs.put(idx, node);
			}
            idx++;
		}
        //return idx-1;
    }

}
