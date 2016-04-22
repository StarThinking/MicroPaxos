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

	private long index;
	private long proposalID;
	private Request proposal;

	protected Accept(){}
	
	public Accept(NodeIdentifier sender, long index, long proposalID, Request proposal) {
		super(Message.MSG_TYPE.Accept, sender);
		this.index = index;
		this.proposalID = proposalID;
		this.proposal = proposal;
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeLong(index);
		buf.writeLong(proposalID);
		proposal.serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
        index = buf.readLong();
        proposalID = buf.readLong();
        proposal = (Request) Message.deserializeRaw(buf);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Accept<src=").append(super.getSender())
			.append(" index=").append(index).append(">")
			.append(" Proposal = " + proposal);

		return sb.toString();
	}
	
	public Request getProposal() {
		return proposal;
	}

	public long getIndex(){
		return index;
	}
	
	public long getProposalID() {
		return proposalID;
	}
}
