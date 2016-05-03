package paxos.network.messages;

import io.netty.buffer.ByteBuf;
import paxos.network.NodeIdentifier;

public class Request extends Message {
	
	private Integer clientId;
	private Integer cIndex;
	private Integer cmd;
	public enum CMD_TYPE {
	    GET, PUT,
	}
	private Integer key;
	private Integer value;

	protected Request(){}
	
	public Request(NodeIdentifier sender, Integer clientId, Integer cIndex, Integer cmd, Integer key, Integer value) {
		super(Message.MSG_TYPE.Request, sender);
		this.clientId = clientId;
		this.cIndex = cIndex;
		this.cmd = cmd;
		this.key = key;
		this.value = value;
	}

	@Override
	public int hashCode() {
		  return clientId.hashCode() + cIndex.hashCode() + cmd.hashCode() + key.hashCode() + value.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		Request r = (Request) o;
		return o.hashCode() == r.hashCode();
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeInt(clientId);
		buf.writeInt(cIndex);
		buf.writeInt(cmd);
		buf.writeInt(key);
		buf.writeInt(value);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		clientId = buf.readInt();
		cIndex = buf.readInt();
        cmd = buf.readInt();
        key = buf.readInt();
        value = buf.readInt();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Request<src=").append(super.getSender())
			.append(" clientId=").append(clientId).append(" cIndex=").append(cIndex).append(">")
			.append(" cmd = " + cmd).append(" key = " + key)
			.append(" value = " + value);
		return sb.toString();
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
	
	public Integer getCIndex(){
		return cIndex;
	}
	
	public Integer getClientId() {
		return clientId;
	}
}
