package paxos;

import java.util.HashMap;
import paxos.network.NodeIdentifier;
import paxos.network.messages.*;
import paxos.node.Acceptor;
import paxos.node.Client;
import paxos.node.Learner;

public class Test {
	//NodeIdentifier clientID = new NodeIdentifier(NodeIdentifier.Role.CLIENT,  0);
	//NodeIdentifier serverID = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,  1);

	public static void main(String []args) throws Exception {
		Test test = new Test();
		//test.simpleTest(args[0]);
		//test.simpleTest0("./paxos.conf");
		//test.simpleTest1("./paxos.conf");	
		test.simpleTest2("./paxos.conf");	
	}
	
	private void setup(HashMap<Integer, Client> clinets, HashMap<Integer, Acceptor> acceptors,
			HashMap<Integer, Learner> learners) {
		// setup clients
		HashMap<Integer, NodeIdentifier> clinetIDs = Configuration.getClientIDs();
		for(Integer index : clinetIDs.keySet()) {
			Client client = new Client(clinetIDs.get(index));
			clinets.put(index, client);
		}
		
		// setup acceptors
		HashMap<Integer, NodeIdentifier> acceptorIDs = Configuration.getAcceptorIDs();
		Integer epochId = 1;
		for(Integer index : acceptorIDs.keySet()) {
			Acceptor acceptor = new Acceptor(acceptorIDs.get(index), epochId);
			acceptors.put(index, acceptor);
			epochId++;
		}
				
		// setup learners
		HashMap<Integer, NodeIdentifier> learnerIDs = Configuration.getLearnerIDs();
		for(Integer index : learnerIDs.keySet()) {
			Learner learner = new Learner(learnerIDs.get(index));
			learners.put(index, learner);
		}
	}

	private void simpleTest0(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();
		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);		
		HashMap<Integer, Client> clinets = new HashMap<Integer, Client>();
		HashMap<Integer, Acceptor> acceptors = new HashMap<Integer, Acceptor>();
		HashMap<Integer, Learner> learners = new HashMap<Integer, Learner>();
		setup(clinets, acceptors, learners);
		
		Acceptor acceptor1 = acceptors.get(1);
		acceptor1.setLeader(true);
		Client client = clinets.get(1);
		client.addCurrentLeader(acceptor1.getMyID());
	
		int cIndex = 0;
		int key = 1;
		for(; cIndex <= 9; ) {
			client.sendRequest(cIndex, Request.CMD_TYPE.PUT.ordinal(), key, cIndex);
			cIndex++;
			client.sendRequest(cIndex, Request.CMD_TYPE.GET.ordinal(), key, 0);
			cIndex++;
		}
	}
	
	private void simpleTest1(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();
		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);		
		HashMap<Integer, Client> clinets = new HashMap<Integer, Client>();
		HashMap<Integer, Acceptor> acceptors = new HashMap<Integer, Acceptor>();
		HashMap<Integer, Learner> learners = new HashMap<Integer, Learner>();
		setup(clinets, acceptors, learners);
		
		Acceptor acceptor1 = acceptors.get(1);
		acceptor1.setLeader(true);
		Client client = clinets.get(1);
		client.addCurrentLeader(acceptor1.getMyID());
		
		int cIndex = 0;
		int key = 1;
		for(cIndex = 0; cIndex <= 99; cIndex++) {
			client.sendRequest(cIndex, Request.CMD_TYPE.PUT.ordinal(), key, cIndex);
		}
		for(cIndex = 0; cIndex <= 99; cIndex++) {
			client.sendRequest(cIndex, Request.CMD_TYPE.PUT.ordinal(), key, cIndex);
		}
	}
	
	private void simpleTest2(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();
		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);		
		HashMap<Integer, Client> clinets = new HashMap<Integer, Client>();
		HashMap<Integer, Acceptor> acceptors = new HashMap<Integer, Acceptor>();
		HashMap<Integer, Learner> learners = new HashMap<Integer, Learner>();
		setup(clinets, acceptors, learners);
		
		Client client = clinets.get(1);
		for(Acceptor acceptor : acceptors.values()) {
			acceptor.setLeader(true);
			client.addCurrentLeader(acceptor.getMyID());
		}
		
		int cIndex = 0;
		int key = 1;
		for(; cIndex <= 9; ) {
			client.sendRequest(cIndex, Request.CMD_TYPE.PUT.ordinal(), key, cIndex);
			cIndex++;
			client.sendRequest(cIndex, Request.CMD_TYPE.GET.ordinal(), key, 0);
			cIndex++;
		}
	}
	
}
