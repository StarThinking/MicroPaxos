package paxos.node;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import paxos.Configuration;
import paxos.network.NettyNetwork;
import paxos.network.Network;
import paxos.network.NodeIdentifier;
import paxos.network.messages.Accept;
import paxos.network.messages.Message;
import paxos.network.messages.Request;

public class Learner implements EventHandler {
	
	NodeIdentifier myID = null;
	Network network = null;
	Map<Integer, Map<Integer, List<NodeIdentifier>>> acceptStore = new HashMap<Integer, Map<Integer, List<NodeIdentifier>>>();
	Map<Integer, Request> requestSequence = new HashMap<Integer, Request>();
	Integer nextExecutingIndex;
	Thread executortThread = new ExecutorThread();
	Map<Integer, Integer> KVStore = new HashMap<Integer, Integer>();
	
	public Learner(NodeIdentifier myID){
		this.myID = myID;
		this.network = new NettyNetwork(myID, this);
		this.nextExecutingIndex = 1;
		this.executortThread.start();
	}
	
	private class ExecutorThread extends Thread {
		
		private boolean running = true;
		private final int sleepInterval = 1000;
		
		@Override
        public void run() {
			while(running) {
				try {
					Request r = null;
					while((r = requestSequence.get(nextExecutingIndex)) == null) {
						Thread.sleep(sleepInterval);
					}
					int cmd = r.getCmd();
					int key = r.getKey();
					int value = r.getValue();
					if(cmd == Request.CMD_TYPE.GET.ordinal()) {
						//System.out.println(Request.CMD_TYPE.values()[cmd] + " key = " + key + " value = " + value);
						int v = KVStore.get(key);
						System.out.println("index = " + nextExecutingIndex + " key = " + key + " value = " + v);
					} else if(cmd == Request.CMD_TYPE.PUT.ordinal()) {
						//System.out.println(Request.CMD_TYPE.values()[cmd] + " key = " + key + " value = " + value);
						KVStore.put(key, value);
					}	
					nextExecutingIndex++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void sendReply(NodeIdentifier receiver, int value){
		//System.out.printf("server send %s => %s\n", new P2bMessage(myID, value), receiver);
		//network.sendMessage(receiver, new P2bMessage(myID, value));
	}
	
	private void updateAndCheckMajority(Accept accept) {
		int indexID = (int) accept.getIndex();
		Map<Integer, List<NodeIdentifier>> acceptMap = null;
		if(acceptStore.get(indexID) != null) {
			acceptMap = acceptStore.get(indexID);
		} else {
			acceptMap = new HashMap<Integer, List<NodeIdentifier>>();
			acceptStore.put(indexID, acceptMap);
		}
		
		int proposalID = (int) accept.getProposalID();
		List<NodeIdentifier> acceptList = null;
		if(acceptMap.get(proposalID) != null) {
			acceptList = acceptMap.get(proposalID);
		} else {
			acceptList = new ArrayList<NodeIdentifier>();
			acceptMap.put(proposalID, acceptList);
		}
		
		NodeIdentifier accceptor = accept.getSender();
		//System.out.println("accceptor = " + accceptor);
		if(!acceptList.contains(accceptor)) {
			acceptList.add(accceptor);
		} 
		
		// check if reaching majority
		int acceptorSize = Configuration.getAcceptorSize();
		int majorityOfAcceptorSize = (acceptorSize / 2) + 1;
		//System.out.println(myID + "'acceptList.size() = " + acceptList.size());
		if(acceptList.size() >= majorityOfAcceptorSize) {
			System.out.println("~~~~~~~" + myID + " acheive Consensus for slot " + indexID + " with proposalID = " + proposalID);
			Request decidedRequest = requestSequence.get(indexID);	
			if(decidedRequest != null) { // check if conflict exists
				if(!decidedRequest.equals(accept.getProposal())) {
					System.out.println("Conflict happens!");
					System.exit(1);		
				}	
			} else {
				requestSequence.put(indexID, accept.getProposal());
			}
		}	
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (msg instanceof Accept) {
			System.out.printf("%s receive %s\n", myID, (Accept) msg);
			Accept accept = (Accept) msg;
			updateAndCheckMajority(accept);
		}
	}

	@Override
	public void handleTimer() {
	}

	@Override
	public void handleFailure(NodeIdentifier node, Throwable cause) {
		if (cause instanceof ClosedChannelException) {
			System.out.printf("%s handleFailure get %s\n", myID, cause);
		}
	}
	
	@Override
	public NodeIdentifier getMyID() {
		return myID;
	}
}
