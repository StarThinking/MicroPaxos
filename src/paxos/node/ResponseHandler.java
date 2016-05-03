package paxos.node;

import java.util.Queue;
import paxos.network.messages.Response;

public class ResponseHandler extends Thread{
	
	Queue<Response> responseQueue;
	Leader leader = null;
	
	public ResponseHandler(Leader leader, Queue<Response> responseQueue) {
		this.leader = leader;
		this.responseQueue = responseQueue;
	}
	
	@Override
	public void run() {
		while(true) {
		    //System.out.println("ResponseHandler run");
			if(responseQueue.size() > 0) {
				Response res = responseQueue.poll();
				leader.handleResponse(res);
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
