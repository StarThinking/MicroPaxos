package paxos.network.messages;

import paxos.network.NodeIdentifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import io.netty.buffer.ByteBuf;

/* 
 * This is proposal
 */
 
public class Accept extends Message {

	private Integer slotIndex;
	private Integer epochId;
	private Request proposal;

	protected Accept(){}
	
	public Accept(NodeIdentifier sender, Integer slotIndex, Integer epochId, Request proposal) {
		super(Message.MSG_TYPE.Accept, sender);
		this.slotIndex = slotIndex;
		this.epochId = epochId;
		this.proposal = proposal;
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeInt(slotIndex);
		buf.writeInt(epochId);
		proposal.serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		slotIndex = buf.readInt();
		epochId = buf.readInt();
        proposal = (Request) Message.deserializeRaw(buf);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Accept<src=").append(super.getSender())
			.append(" slotIndex=").append(slotIndex).append(">")
			.append(" epochId = " + epochId);
		return sb.toString();
	}
	
	public Request getProposal() {
		return proposal;
	}

	public Integer getSlotIndex(){
		return slotIndex;
	}
	
	public Integer getEpochId() {
		return epochId;
	}
}
