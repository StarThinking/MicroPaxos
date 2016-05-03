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
import paxos.network.messages.Prepare;
import paxos.network.messages.Request;
import paxos.network.messages.Response;

public class Acceptor implements EventHandler {
	
	NodeIdentifier myID = null;
	Network network = null;
	Leader leader = null;
	boolean isLeader = false;
	int preparePace = 10;

	// <slotIndex, largest epochId promised>
	Map<Integer, Integer> promiseSequence = new HashMap<Integer, Integer>();
	
	// <slotIndex, accepted accept>
	Map<Integer, Request> acceptedSequence = new HashMap<Integer, Request>();

	public Acceptor(NodeIdentifier myID, Integer epochId) {
		this.myID = myID;
		this.network = new NettyNetwork(myID, this);
		this.leader = new Leader(this, epochId);
	}

	public void handlePrepare(Prepare prepare) {
		int slotIndex = prepare.getSlotIndex();
		int epochIdOfSender = prepare.getEpochId();
		NodeIdentifier proposer = prepare.getSender();
		if(!promiseSequence.containsKey(slotIndex)) { // not receive any prepare for this slot before, send back null
			promiseSequence.put(slotIndex, epochIdOfSender);
			network.sendMessage(proposer, new Response(myID, slotIndex, false, null));	// send null
		} else {
			int currentMaxEpochId = promiseSequence.get(slotIndex);
			if(epochIdOfSender >= currentMaxEpochId) {
				promiseSequence.put(slotIndex, epochIdOfSender);
				if(!acceptedSequence.containsKey(slotIndex)) { // not accepted yet, send null	
					network.sendMessage(proposer, new Response(myID, slotIndex, false, null));	
				} else { // accepted before, send accepted proposal
					network.sendMessage(proposer, new Response(myID, slotIndex, true, acceptedSequence.get(slotIndex)));	
				}
			} else {
				// ignore if epochIdOfSender < currentMaxEpochId
			}
		}		
	}
	
	public void handleAccept(Accept accept) {
		int epochId = accept.getEpochId();
		int slotIndex = accept.getSlotIndex();	
		if(epochId >= promiseSequence.get(slotIndex) ) {
			acceptedSequence.put(slotIndex, accept.getProposal());
			accept.setSender(myID);		
			// send Accept to all learners	
			HashMap<Integer, NodeIdentifier> learnerIDs = Configuration.getLearnerIDs();
			for(NodeIdentifier learnerID : learnerIDs.values()) {
				System.out.printf("server send %s => %s\n", accept, learnerID);
				network.sendMessage(learnerID, accept);	
			}		
		}
	}	
	
	@Override
	public void handleMessage(Message msg) {
		// as leader
		if(isLeader) {
			if (msg instanceof Request) {
				Request request = (Request) msg;
				System.out.printf("%s receive %s\n", myID, request);			
				leader.putRequest(request);
			} else if(msg instanceof Response) {
				Response response = (Response) msg;
				System.out.printf("%s receive %s\n", myID, response);
				leader.putResponse(response);
			} 
		}
		
		// as acceptor
		if(msg instanceof Prepare) {
			Prepare prepare = (Prepare) msg;
			System.out.printf("%s receive %s\n", myID, prepare);
			handlePrepare(prepare);
		} else if(msg instanceof Accept) {
			Accept accept = (Accept) msg;
			System.out.printf("%s receive %s\n", myID, accept);
			handleAccept(accept);
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
	
	public boolean isLeader() {
		return isLeader;
	}

	public void setLeader(boolean isLeader) {
		this.isLeader = isLeader;
		/*if(isLeader) {
			System.out.println("start");
			leader.responseHandler.start();
			leader.requestHandler.start();
		} else {
			leader.responseHandler.stop();
			leader.requestHandler.stop();
		}*/
	}
}