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
		test.simpleTest("./paxos.conf");
		
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
		for(Integer index : acceptorIDs.keySet()) {
			Acceptor acceptor = new Acceptor(acceptorIDs.get(index));
			acceptors.put(index, acceptor);
		}
				
		// setup learners
		HashMap<Integer, NodeIdentifier> learnerIDs = Configuration.getLearnerIDs();
		for(Integer index : learnerIDs.keySet()) {
			Learner learner = new Learner(learnerIDs.get(index));
			learners.put(index, learner);
		}
	}

	private void simpleTest(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();

		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);
		//Configuration.addNodeAddress(clientID, new InetSocketAddress("localhost", 2001));
		//Configuration.addNodeAddress(serverID, new InetSocketAddress("localhost", 5001));
			
		HashMap<Integer, Client> clinets = new HashMap<Integer, Client>();
		HashMap<Integer, Acceptor> acceptors = new HashMap<Integer, Acceptor>();
		HashMap<Integer, Learner> learners = new HashMap<Integer, Learner>();
		setup(clinets, acceptors, learners);
		
		Client client = clinets.get(1);
		Acceptor acceptor1 = acceptors.get(1);
		acceptor1.setLeader(true);
		client.sendRequest(acceptor1.getMyID(), Request.CMD_TYPE.PUT.ordinal(), 1, 100);
		client.sendRequest(acceptor1.getMyID(), Request.CMD_TYPE.GET.ordinal(), 1, 0);
		client.sendRequest(acceptor1.getMyID(), Request.CMD_TYPE.PUT.ordinal(), 1, 200);
		client.sendRequest(acceptor1.getMyID(), Request.CMD_TYPE.GET.ordinal(), 1, 0);
		client.sendRequest(acceptor1.getMyID(), Request.CMD_TYPE.PUT.ordinal(), 2, 123);
		client.sendRequest(acceptor1.getMyID(), Request.CMD_TYPE.GET.ordinal(), 2, 0);
		//server.sendValue(serverID, 100);
	}
}
