package paxos.network.messages;

import paxos.network.NodeIdentifier;


import io.netty.buffer.ByteBuf;
 
public class Reply extends Message {

	private long index;

	protected Reply(){}
	
	public Reply(NodeIdentifier sender, long index) {
		super(Message.MSG_TYPE.Reply, sender);
		this.index = index;
	}
	
	public long getIndex(){
		return index;
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeLong(index);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
        index = buf.readLong();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Reply<src=").append(super.getSender())
			.append(" index=").append(index).append(">");
		return sb.toString();
	}
}