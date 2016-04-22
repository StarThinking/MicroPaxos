package paxos.node;

import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;

import paxos.Configuration;
import paxos.network.NettyNetwork;
import paxos.network.Network;
import paxos.network.NodeIdentifier;
import paxos.network.messages.Accept;
import paxos.network.messages.Message;
import paxos.network.messages.Request;

public class Acceptor implements EventHandler {
	
	NodeIdentifier myID = null;
	Network network = null;
	boolean isLeader = false;
	Map<Integer, Request> requestSequence = new HashMap<Integer, Request>();
	Integer nextProposingIndex;
	
	public boolean isLeader() {
		return isLeader;
	}

	public void setLeader(boolean isLeader) {
		this.isLeader = isLeader;
	}

	public Acceptor(NodeIdentifier myID) {
		this.myID = myID;
		this.nextProposingIndex = 1;
		this.network = new NettyNetwork(myID, this);
	}
	
	private void sendAccepts(Request proposal) {
		long proposalID = 1;
		HashMap<Integer, NodeIdentifier> acceptorIDs = Configuration.getAcceptorIDs();
		for(NodeIdentifier acceptorID : acceptorIDs.values()) {		
			Accept accept = new Accept(myID, nextProposingIndex, proposalID, proposal);
			System.out.printf("server send %s => %s\n", accept, acceptorID);
			network.sendMessage(acceptorID, accept);
		}
		nextProposingIndex++;
	}
	
	private boolean shouldAgree() {
		return true;
	}

	private void sendAgrees(Accept accept) {
		accept.setSender(myID);
		HashMap<Integer, NodeIdentifier> learnerIDs = Configuration.getLearnerIDs();
		for(NodeIdentifier learnerID : learnerIDs.values()) {
			System.out.printf("server send %s => %s\n", accept, learnerID);
			network.sendMessage(learnerID, accept);	
		}
	}
	
	@Override
	public NodeIdentifier getMyID() {
		return myID;
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (msg instanceof Request) {
			Request request = (Request) msg;
			System.out.printf("%s receive %s\n", myID, (Request) msg);			
			if(isLeader) {
				sendAccepts(request); // send proposal to ALL acceptors as Proposer
			}
		} else if(msg instanceof Accept) {
			Accept accept = (Accept) msg;
			System.out.printf("%s receive %s\n", myID, (Accept) msg);
			if(shouldAgree()) 
				sendAgrees(accept); // send agreed accepts to ALL learner as Acceptor
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
}