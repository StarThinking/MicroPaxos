package paxos.node;

import java.util.Queue;
import paxos.network.messages.Request;

public class RequestHandler extends Thread {
	
	Queue<Request> requestQueue;
	Leader leader = null;
	
	public RequestHandler(Leader leader, Queue<Request> requestQueue) {
		this.leader = leader;
		this.requestQueue = requestQueue;
	}
	
	@Override
	public void run() {
		while(true) {
			// System.out.println("RequestHandler run");	
			if(requestQueue.size() > 0) {
				Request req = requestQueue.poll();
				leader.handleRequest(req);	
			} else {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {	
					e.printStackTrace();
				}
			}	
		}	
	}	
}
