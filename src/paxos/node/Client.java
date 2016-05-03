package paxos.node;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import paxos.network.NettyNetwork;
import paxos.network.Network;
import paxos.network.NodeIdentifier;
import paxos.network.messages.Message;
import paxos.network.messages.Reply;
import paxos.network.messages.Request;

public class Client implements EventHandler {
	
	NodeIdentifier myID = null;
	Network network = null;
	List<NodeIdentifier> currentLeaderList = null;
	
	// <cIndex, sent request>
	Map<Integer, Request> sentRequestSequence = new HashMap<Integer, Request>();
	
	// <cIndex, received reply>
	Map<Integer, Reply> receivedReplySequence = new HashMap<Integer, Reply>();
	
	public Client(NodeIdentifier myID){
		this.network = new NettyNetwork(myID, this);
		this.myID = myID;
		this.currentLeaderList = new ArrayList<NodeIdentifier>();
	}
	
	public Client(NodeIdentifier myID, NodeIdentifier currentLeader){
		this.network = new NettyNetwork(myID, this);
		this.myID = myID;
		this.currentLeaderList = new ArrayList<NodeIdentifier>();
	}
	
	public void sendRequest(Integer cIndex, int cmd, int key, int value){
		Request request = new Request(myID, myID.getID(), cIndex, cmd, key, value);
		for(NodeIdentifier leader : currentLeaderList) {
			System.out.printf("server send %s => %s\n", request , leader);
			network.sendMessage(leader, request);
			sentRequestSequence.put(cIndex, request);
		}
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg instanceof Reply) {
			Reply reply = (Reply) msg;
			System.out.printf("REPLYYYYYYYYY %s receive %s\n", myID, reply);
			receivedReplySequence.put(reply.getcIndex(), reply);
		}
	}

	@Override
	public void handleTimer() {
		//System.out.println("~~~~~~handleTimer");
		for(Integer cIndex : sentRequestSequence.keySet()) {
			if(!receivedReplySequence.containsKey(cIndex)) {
				System.out.println("~~~~~~handleTimer missing Reply for cIndex " + cIndex);
				Request req = sentRequestSequence.get(cIndex);
				sendRequest(cIndex, req.getCmd(), req.getKey(), req.getValue());
			}
		}
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
	
	public void addCurrentLeader(NodeIdentifier leader) {
		this.currentLeaderList.add(leader);
	}

}