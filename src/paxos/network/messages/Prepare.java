package paxos.network.messages;

import io.netty.buffer.ByteBuf;
import paxos.network.NodeIdentifier;

public class Prepare extends Message {
	
	private Integer slotIndex;
	private Integer epochId;

	protected Prepare(){}
	
	public Prepare(NodeIdentifier sender, Integer slotIndex, Integer epochId) {
		super(Message.MSG_TYPE.Prepare, sender);
		this.slotIndex = slotIndex;
		this.epochId = epochId;
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeInt(slotIndex);
		buf.writeInt(epochId);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		slotIndex = buf.readInt();
		epochId = buf.readInt();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Prepare<src=").append(super.getSender())
			.append(" slotIndex=").append(slotIndex).append(">")
			.append(" epochId = " + epochId);
		return sb.toString();
	}
	
	public Integer getSlotIndex(){
		return slotIndex;
	}
	
	public Integer getEpochId() {
		return epochId;
	}
}
