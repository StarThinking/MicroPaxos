package paxos.network.messages;

import paxos.network.NodeIdentifier;


import io.netty.buffer.ByteBuf;
 
public class Reply extends Message {

	private Integer cIndex;
	private Integer cmd;
	public enum CMD_TYPE {
	    GET, PUT,
	}
	private Integer key;
	private Integer value;

	protected Reply(){}
	
	public Reply(NodeIdentifier sender, Integer cIndex, Integer cmd, Integer key, Integer value) {
		super(Message.MSG_TYPE.Reply, sender);
		this.cIndex = cIndex;
		this.cmd = cmd;
		this.key = key;
		this.value = value;
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeInt(cIndex);
		buf.writeInt(cmd);
		buf.writeInt(key);
		buf.writeInt(value);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		cIndex = buf.readInt();
		cmd = buf.readInt();
		key = buf.readInt();
		value = buf.readInt();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Reply<src=").append(super.getSender())
			.append(" cIndex=").append(cIndex).append(">")
			.append(" cmd = ").append(CMD_TYPE.values()[cmd])
			.append(" key = ").append(key)
			.append(" value = ").append(value);
		return sb.toString();
	}
	
	public Integer getcIndex() {
		return cIndex;
	}

	public Integer getCmd() {
		return cmd;
	}

	public Integer getKey() {
		return key;
	}

	public Integer getValue() {
		return value;
	}
}