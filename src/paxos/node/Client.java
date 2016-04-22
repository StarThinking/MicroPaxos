package paxos.node;

import java.nio.channels.ClosedChannelException;

import paxos.network.NettyNetwork;
import paxos.network.Network;
import paxos.network.NodeIdentifier;
import paxos.network.messages.Message;
import paxos.network.messages.Request;

public class Client implements EventHandler {
	NodeIdentifier myID = null;
	Network network = null;
	
	public Client(NodeIdentifier myID){
		this.myID = myID;
		network = new NettyNetwork(myID, this);
	}

	public void sendRequest(NodeIdentifier receiver, int cmd, int key, int value){
		Request msg = new Request(myID, cmd, key, value);
		System.out.printf("server send %s => %s\n", msg , receiver);
		network.sendMessage(receiver, msg);
	}

	@Override
	public void handleMessage(Message msg) {
		/*if (msg instanceof P2bMessage) {
			System.out.printf("%s receive %s\n", myID, (P2bMessage) msg);
		}*/
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