package paxos.network.messages;

import io.netty.buffer.ByteBuf;
import paxos.network.NodeIdentifier;

public class Response extends Message {
	
	private Integer slotIndex;
	private Boolean hasAlreadyAccepted;
	private Request acceptedRequest = null;
	
	public Response(){}
	
	public Response(NodeIdentifier sender, Integer slotIndex, Boolean hasAlreadyAccepted, Request acceptedRequest) {
		super(Message.MSG_TYPE.Response, sender);
		this.slotIndex = slotIndex;
		this.hasAlreadyAccepted = hasAlreadyAccepted;
		this.acceptedRequest = acceptedRequest;
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeInt(slotIndex);
		buf.writeBoolean(hasAlreadyAccepted);
		if(hasAlreadyAccepted)
			acceptedRequest.serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		slotIndex = buf.readInt();
		hasAlreadyAccepted = buf.readBoolean();
		if(hasAlreadyAccepted)
			acceptedRequest = (Request) Message.deserializeRaw(buf);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Response<src=").append(super.getSender())
			.append(" slotIndex=").append(slotIndex).append(">");
		return sb.toString();
	}
	
	public Integer getSlotIndex(){
		return slotIndex;
	}
	
	public Request getAcceptedRequest() {
		return acceptedRequest;
	}

}
