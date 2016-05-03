package paxos.node;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import paxos.network.NettyNetwork;
import paxos.network.Network;
import paxos.network.NodeIdentifier;
import paxos.network.messages.Accept;
import paxos.network.messages.Message;
import paxos.network.messages.Reply;
import paxos.network.messages.Request;

public class Learner implements EventHandler {
	
	NodeIdentifier myID = null;
	Network network = null;
	
	// <slotIndex, <epochId, accepts>>
	Map<Integer, Map<Integer, List<Accept>>> receivedAccepts = new HashMap<Integer, Map<Integer, List<Accept>>>();
	
	// <slotIndex, request>
	Map<Integer, Request> slotSequence = new HashMap<Integer, Request>();
	
	Thread executortThread = new ExecutorThread();
	Map<Integer, Integer> KVStore = new HashMap<Integer, Integer>();
	
	public Learner(NodeIdentifier myID){
		this.myID = myID;
		this.network = new NettyNetwork(myID, this);
		this.executortThread.start();
	}
	
	public void handleAccept(Accept accept) {
		int slotIndex = accept.getSlotIndex();
		
		if(slotSequence.containsKey(slotIndex)) { // if already reached majority, ignore
			return;
		}
		
		if(!receivedAccepts.containsKey(slotIndex)) {
			receivedAccepts.put(slotIndex, new HashMap<Integer, List<Accept>>());
		}
		Map<Integer, List<Accept>> receivedAcceptsPerSlot = receivedAccepts.get(slotIndex);
		
		int epochId = accept.getEpochId();
		if(!receivedAcceptsPerSlot.containsKey(epochId)) {
			receivedAcceptsPerSlot.put(epochId, new ArrayList<Accept>());
		}
		List<Accept> receivedAcceptsPerSlotPerEpoch = receivedAcceptsPerSlot.get(epochId);
		
		NodeIdentifier sendor = accept.getSender();
		if(receivedAcceptsPerSlotPerEpoch.size() > 0) { // check if from same acceptor
			for(Accept receiedAccept : receivedAcceptsPerSlotPerEpoch) { 
				if(sendor.equals(receiedAccept.getSender())) {
					return;
				}
			}
		}
		
		receivedAcceptsPerSlotPerEpoch.add(accept);
		if(receivedAcceptsPerSlotPerEpoch.size() >= 2) {
			System.out.println("$$$$%%%%% reached majority at " + myID + " slotIndex = " + slotIndex);
			slotSequence.put(slotIndex, accept.getProposal());
		}
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (msg instanceof Accept) {
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
	
	private class ExecutorThread extends Thread {
		
		private boolean running = true;
		private final int sleepInterval = 50;
		private int slotIndex = 0;
		
		@Override
        public void run() {
			while(running) {
				try {
					Request r = null;
					while((r = slotSequence.get(slotIndex)) == null) {
						Thread.sleep(sleepInterval);
					}
					int cmd = r.getCmd();
					int key = r.getKey();
					int value = r.getValue();
					if(cmd == Request.CMD_TYPE.GET.ordinal()) {
						value = KVStore.get(key);
						System.out.println("PPPPPP Performed SlotIndex " + slotIndex + 
								" At " + myID + " " + Request.CMD_TYPE.values()[cmd] + 
								" key = " + key + " value = " + value);
					} else if(cmd == Request.CMD_TYPE.PUT.ordinal()) {
						KVStore.put(key, value);
						System.out.println("PPPPPP Performed SlotIndex " + slotIndex + 
								" At " + myID + " " + Request.CMD_TYPE.values()[cmd] + 
								" key = " + key + " value = " + value);
					}	
					slotIndex++;
					Reply reply = new Reply(myID, r.getCIndex(), cmd, key, value);
					System.out.printf("server send %s => %s\n", reply, r.getSender());
					network.sendMessage(r.getSender(), reply);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
