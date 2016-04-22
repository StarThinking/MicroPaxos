package paxos.network.messages;

import paxos.network.NodeIdentifier;
import io.netty.buffer.ByteBuf;

public class Message {

    // please feel free to add new types of messages
    public enum MSG_TYPE {
    	Request, Reply,
    	Accept,
    	Prepare, Response
    }
	
	private int type;
	private NodeIdentifier sender;
	
	protected Message(){}
	
	public Message(MSG_TYPE msgType, NodeIdentifier sender){
		this.type = msgType.ordinal();
		this.sender = sender;
	}

    public int getType() {
        return type;
    }

	public void setType(int t) {
		type = t;
	}

	public NodeIdentifier getSender(){
		return sender;
	}

	public void setSender(int hashCode){
		sender = new NodeIdentifier(hashCode);
	}
	
	public void setSender(NodeIdentifier sender){
		this.sender = sender;
	}

	public NodeIdentifier.Role getSenderRole(){
		return sender.getRole();
	}
	
	public int getSenderID(){
		return sender.getID();
	}
	
	public void serialize(ByteBuf buf){
		buf.writeInt(type);
		buf.writeInt(sender.hashCode());
	}
	
	public void deserialize(ByteBuf buf){
		type = buf.readInt();
		sender = new NodeIdentifier(buf.readInt());
	}
	
	public static Message deserializeRaw(ByteBuf buf){
		Message ret;
		buf.markReaderIndex();
		int type = buf.readInt();
		buf.resetReaderIndex();
        //System.out.println("call deserializeRaw with type: " + MSG_TYPE.values()[type] + "\n");
		switch(MSG_TYPE.values()[type]){
			case Prepare:
				ret = new Prepare();
				break;
			case Response:
				ret = new Response();
				break;
			case Accept:
				ret = new Accept();
				break;
			case Request:
				ret = new Request();
				break;
			case Reply:
				ret = new Reply();
				break;
			default:
				throw new RuntimeException("Unknown msg type "+type);
		}
		ret.deserialize(buf);
		return ret;
	}
}
