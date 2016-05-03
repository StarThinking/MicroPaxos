package paxos.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import paxos.Configuration;
import paxos.network.NodeIdentifier;
import paxos.network.messages.Accept;
import paxos.network.messages.Prepare;
import paxos.network.messages.Request;
import paxos.network.messages.Response;

public class Leader {
	
	Integer epochId = null;
	Acceptor acceptor = null;
	Boolean running = null;
	
	// The sequence of proposed requests <slotIndex, proposed request>
	Map<Integer, Request> slotSequence = new HashMap<Integer, Request>();
	Integer currentSlotIndex;
	
	// <clientId, <cIndex, slotIndex>>
	Map<Integer, Map<Integer, Integer>> sequenceForClients = new HashMap<Integer, Map<Integer, Integer>>();

	// <slotIndex, request> if request is null, then the leader can propose a new value
	Map<Integer, Request> preparedSlots = new HashMap<Integer, Request>();

	// <slotIndex, list of Response>
	Map<Integer, List<Response>> receivedResponse = new HashMap<Integer, List<Response>>();
	
	Thread requestHandler = null;
	Thread responseHandler = null;
	Queue<Request> requestQueue = new LinkedBlockingQueue<Request>();
	Queue<Response> responseQueue = new LinkedBlockingQueue<Response>();
	
	//int preparePace = 10;
	
	public Leader(Acceptor acceptor, Integer epochId) {
		this.currentSlotIndex = 0;
		this.acceptor = acceptor;
		this.epochId = epochId;
		this.requestHandler = new RequestHandler(this, requestQueue);
		this.responseHandler = new ResponseHandler(this, responseQueue);
	//	this.requestHandler.setDaemon(true);
	//	this.responseHandler.setDaemon(true);  
		this.requestHandler.start();
		this.responseHandler.start();
	}
	
	private void sendAccepts(int slotIndex, Request request) {
		HashMap<Integer, NodeIdentifier> acceptorIDs = Configuration.getAcceptorIDs();
		for(NodeIdentifier acceptorID : acceptorIDs.values()) {		
			Accept accept = new Accept(acceptor.myID, slotIndex, epochId, request);
			System.out.printf("server send %s => %s\n", accept, acceptorID);		
			acceptor.network.sendMessage(acceptorID, accept);
		}
	}
	
	private void sendPrepares() {
		HashMap<Integer, NodeIdentifier> acceptorIDs = Configuration.getAcceptorIDs();
		for(NodeIdentifier acceptorID : acceptorIDs.values()) {		
			Prepare prepare = new Prepare(acceptor.myID, currentSlotIndex, epochId);
			System.out.printf("server send %s => %s\n", prepare, acceptorID);
			acceptor.network.sendMessage(acceptorID, prepare);
		}
	}
	
	public void putRequest(Request request) {	
		this.requestQueue.add(request);	
	}
	
	public void handleRequest(Request request) {
		//System.out.println("handleRequest");
		int clientId = request.getClientId();
		int cIndex = request.getCIndex();	
		
		// obtain sequencePerClient
		Map<Integer, Integer> sequencePerClient = null;
		if(!sequenceForClients.containsKey(clientId)) {
			sequencePerClient = new HashMap<Integer, Integer>();
			sequenceForClients.put(clientId, sequencePerClient);
		} else {
			sequencePerClient = sequenceForClients.get(clientId);
		}
		
		// if previous cIndex is lost, then ignore
		if(cIndex != sequencePerClient.size()) {
			return;
		}
			
		// check if proposed before
		if(proposedYet(clientId, cIndex)) { 
			int slotIndex = sequencePerClient.get(cIndex);
			System.out.printf("%s proposed before with slotIndex %d\n", acceptor.myID, slotIndex);	
			sendAccepts(slotIndex, request);
			return;
		}
		
		// do something until slot is available for new proposal
		while(!(preparedSlots.containsKey(currentSlotIndex) && preparedSlots.get(currentSlotIndex) == null)) {
			// slot is prepared but has accepted request
			if(preparedSlots.containsKey(currentSlotIndex) && preparedSlots.get(currentSlotIndex) != null) {
				sendAccepts(currentSlotIndex, preparedSlots.get(currentSlotIndex));
				currentSlotIndex++;
				continue;
			}		
			// not prepared, send prepares
			if(!preparedSlots.containsKey(currentSlotIndex)) 
				sendPrepares();
			
			while(!preparedSlots.containsKey(currentSlotIndex)) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("@@@####now we can propose the new request with currentSlotIndex " + currentSlotIndex);
		
		// now we can propose the new request with currentSlotIndex
		slotSequence.put(currentSlotIndex, request);
		sequencePerClient.put(cIndex, currentSlotIndex);
		
		// send accepts(proposals)
		sendAccepts(currentSlotIndex, request);
		currentSlotIndex++;	
	}
	
	public void putResponse(Response response) {
		this.responseQueue.add(response);
	}
	
	public void handleResponse(Response response) {
		//System.out.println("!!!!!!handleResponse");
		int slotIndex = response.getSlotIndex();
		NodeIdentifier acceptor = response.getSender();
		
		if(!receivedResponse.containsKey(slotIndex)) { 
			receivedResponse.put(slotIndex, new ArrayList<Response>());
		}
		
		List<Response> receivedRes = receivedResponse.get(slotIndex);
		if(receivedRes.size() > 0) {
			for(Response r : receivedResponse.get(slotIndex)) {
				if(r.getSender().equals(acceptor)) { // have received response from this acceptor before
					return;
				}
			}
		}
		
		receivedResponse.get(slotIndex).add(response);
		if(receivedResponse.get(slotIndex).size() >= 2) { // reach majority of response
			Request acceptedReq = null;
			for(Response res :receivedResponse.get(slotIndex)) {
				if(res.getAcceptedRequest() != null) {
					acceptedReq = res.getAcceptedRequest();
				}
			}	
			preparedSlots.put(slotIndex, acceptedReq);
			System.out.println("preparedSlots put slotIndex = " + slotIndex);
		}
	}
	
	private boolean proposedYet(int clinetId, int cIndex) {
		Map<Integer, Integer> sequencePerClient = sequenceForClients.get(clinetId);
		if(sequencePerClient == null)
			return false;
		if(sequencePerClient.containsKey(cIndex))
			return true;
		else 
			return false;
	}

}
