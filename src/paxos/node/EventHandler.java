package paxos.node;

import paxos.network.NodeIdentifier;
import paxos.network.messages.Message;

public interface EventHandler {
	
	public NodeIdentifier getMyID();
	
	/*
	 * Handle a message from another node
	 */
	public void handleMessage(Message msg);
	
	/*
	 * Handle a timer event. A timer event is triggered if
	 * there is no other event in a given amount of time (100ms).
	 */
	public void handleTimer();
	
	/*
	 * Handle a failure event. A failure event is triggered
	 * if the corresponding connection is broken.
	 */
	// should catch ClosedChannelException for test purpose
	public void handleFailure(NodeIdentifier node, Throwable cause);
}
