package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer{
    DatagramSocket ss;//socketServer
    DatagramPacket inPacket;
    DatagramPacket outPacket;
    Thread receiveThread;
    Thread heartbeatThread;
    static int BUFSIZE = 1024;
    ConcurrentHashMap<String, Long> heartbeatMap;
    AccountManager AC;
    
    public ChatServer(){
        try{
        	init();
            receiveThread.start();
            heartbeatThread.start();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    void init() throws Exception{
    	AC = new AccountManager();
        ss = new DatagramSocket(null);
        ss.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 20000));
        initReceiveThread();
        byte[] buf = new byte[BUFSIZE];
        outPacket = new DatagramPacket(buf,0);
        heartbeatMap = new ConcurrentHashMap<String, Long>();
        initHeartbeatThread();
    }
    
    //initialize the receive thread
    void initReceiveThread(){
        receiveThread = new Thread(() -> {
            byte[] buf = new byte[BUFSIZE];
            inPacket = new DatagramPacket(buf, BUFSIZE);
            InetAddress addr = null;
            int port;
            String msg = null;
            try{
                while(true){
                    System.out.println("waiting");
                    ss.receive(inPacket);
                    addr = inPacket.getAddress();
                    port = inPacket.getPort();
                    msg = new String(inPacket.getData(), 0, inPacket.getLength());
                    //call the solve function
                    solve(new InetSocketAddress(addr, port),msg);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }
    
    void initHeartbeatThread() {
    	heartbeatThread = new Thread(() -> {
            try{
                while(true){
                    Thread.sleep(1000);
                    //println("Heartbeat "+heartbeatMap);
                    for(HashMap.Entry<String, Long> entry : heartbeatMap.entrySet()) {
                    	if(getTime() - entry.getValue() > 5000) {
                    		clearHeartbeat(entry.getKey());
                    		println(entry.getKey()+" clear heartbeat");
                    		//AC.printall();
                    	}
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }
    
    
    
    public static void main(String[] args) {
		ChatServer CS = new ChatServer();
		
		
	}
    public synchronized void send(InetSocketAddress addr, String msg) {
    	outPacket.setSocketAddress(addr);
    	byte[] buf = msg.getBytes();
    	outPacket.setData(buf, 0, buf.length);
    	try {
			ss.send(outPacket);
			println("sendto: "+addr.getHostString()+":"+addr.getPort()+"with msg:"+msg);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
    }
    
    //deal with the received message
    void solve(InetSocketAddress addr, String msg){
    	write(msg+"\n");
        System.out.println("ip:"+addr.getHostString()+" port:"+addr.getPort()+"  msg:"+msg);
        try {
        	JSONObject json = JSONObject.parseObject(msg,JSONObject.class);
        	String act = (String)json.get("ACT");
        	String id = (String)json.get("ID");
        	String token = (String)json.get("TOKEN");
        	if(AC.getTempinfoById(id) != null)println(AC.getTempinfoById(id).getToken());
        	if(token != null && id != null && AC.getTempinfoById(id) != null && token.equals(AC.getTempinfoById(id).getToken())) {
        		AC.getTempinfoById(id).setAddr(addr);
        		println("save new addr");
        	}else {
        		println("keep old addr");
        	}
        	println(act);
        	switch(act) {
	        	case "SIGNUP":
	        		signup(addr, json);
	        		break;
	        	case "CHANGE_PASSWD":
	        		changePassword(addr, json);
	        		break;
	        	case "LOGIN":
	        		login(addr, json);
	        		break;
	        	case "SEND":
	        		sendMsg(addr, json);
	        		break;
	        	case "RECEIVE_SUCCESS":
	        		break;
	        	case "ADD_FRIEND":
	        		addFriend(addr, json);
	        		break;
	        	case "AGREE_ADD":
	        		acceptApply(addr, json);
	        	case "REFUSE_ADD":
	        		rejectApply(addr, json);
	        		break;
	        	case "GET_INFO_ALL":
	        		sendFriendInfo(addr, json);
	        		break;
	        	case "GET_INFO":
	        		break;
	        	case "HEART_BEAT":
	        		receiveHeartbeat(addr, json);
	        		break;
        	}
        	AC.printall();
        }catch(JSONException e) {
        	println("Not a JSON string");
        	e.printStackTrace();
        }catch(FormatException e) {
        	println("Wrong format");
        }catch(Exception e){
        	println("Other error occurs");
        	e.printStackTrace();
        }
        
    }
    
    void receiveHeartbeat(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String token, id;
    	try {
			token = (String)json.get("TOKEN");
			id = (String)json.get("ID");
			if(token == null || id == null) {
				throw new FormatException();
			}
		} catch (Exception e) {
			throw new FormatException();
		}
    	if(true) {//token.equals(AC.getTempinfoById(id).getToken())) {
    		println(id+" receiving heartbeat");
    		updateHeartbeat(id);
    	}
    }
    
    void updateHeartbeat(String id) {
    	if(heartbeatMap.containsKey(id)) {
    		heartbeatMap.replace(id, getTime());
    	}else {
    		heartbeatMap.put(id,getTime());
    	}
    }
    
    void logout(String id) {
    	Account acct = AC.getAccountById(id);
    	if(acct == null) {
    		return;
    	}
    	AC.forceLogout(id);
    	for(String fId: acct.getFriends()) {
    		acct = AC.getAccountById(fId);
    		if(acct != null && acct.getStatus() == RESULT.STATUS_ALIVE) {
    			String msg = getFriendInfo(fId);
    			send(AC.getTempinfoById(fId).getAddr(),msg);
    		}
    	}
    }
    
    void clearHeartbeat(String id) {
    	heartbeatMap.remove(id);
    	//to be changed
    	logout(id);
    }
    
    
    void signup(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String id, name, password;
    	try {
	    	id = (String)json.get("ID");
	    	name = (String)json.get("NAME");
	    	password = (String)json.get("PASSWD");
	    	if(id == null || name == null || password == null) {
	    		throw new FormatException();
	    	}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	int res = AC.signup(id, password, name);
    	if(res == RESULT.SUCCESS) {
    		AC.getTempinfoById(id).setAddr(addr);
    		Account account = AC.getAccountById(id);
    		JSONArray friendArray = new JSONArray();
	    	for(String fId : account.getFriends()) {
	        	JSONObject temp = new JSONObject();
	    		temp.put("ID",fId);
	    		temp.put("NAME",AC.getAccountById(fId).getName());
	    		String status = null;
	    		switch(AC.getAccountById(fId).getStatus()){
	    			case RESULT.STATUS_ALIVE:
	    				status = "ALIVE";
	    				break;
	    			case RESULT.STATUS_OFFLINE:
	    				status = "OFFLINE";
	    				break;
	    		}
	    		temp.put("STATUS",status);
	    		friendArray.add(temp);
	    	}
    		send(addr, getString("ACT","SIGNUP_SUCCESS","ID",id,"TIME",System.currentTimeMillis()));
    	}else {
    		send(addr, getString("ACT","SIGNUP_FAILED","ID","id","REASON",res,"TIME",getTime()));
    	}
    }
    
    void changePassword(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String id, oldPassword, newPassword;
    	try {
	    	id = (String)json.get("ID");
	    	oldPassword = (String)json.get("PASSWD");
	    	newPassword = (String)json.get("NEW_PASSWD");
	    	if(id == null || oldPassword == null || newPassword == null) {
	    		throw new FormatException();
	    	}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	int res = AC.changePassword(id, oldPassword, newPassword);
    	if(res == RESULT.SUCCESS) {
    		send(addr, getString("ACT","CHANGE_SUCCESS","ID",id,"TIME",System.currentTimeMillis()));
    	}else {
			send(addr, getString("ACT","CHANGE_FAILED","ID",id,"REASON",res,"TIME",getTime()));
		}
    }
    
    void login(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String id, password;
    	try {
	    	id = (String)json.get("ID");
	    	password = (String)json.get("PASSWD");
	    	if(id == null || password == null) {
	    		throw new FormatException();
	    	}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	int res = AC.login(id, password);
    	if(res == RESULT.SUCCESS) {
    		AC.getTempinfoById(id).setAddr(addr);
    		//to be removed
    		send(addr, getString("ACT","LOGIN_SUCCESS","ID",id,"NAME",AC.getAccountById(id).getName(),"TOKEN",AC.getTempinfoById(id).getToken(),"TIME",getTime()));
    		//to be change
    		new Thread(()->{
    			try {
    				Thread.sleep(2000);	
				} catch (Exception e) {
					// TODO: handle exception
				}
    			sendTempMsg(id);
    		}).start();
    		//
    		//send(addr, getString("ACT","LOGIN_SUCCESS","ID",id,"TOKEN",AC.getTempinfoById(id).getToken(),"TIME",getTime()));
    		updateHeartbeat(id);
    	}else {
			send(addr, getString("ACT","LOGIN_FAILED","REASON",res,"TIME",getTime()));
		}
    }
    //send message literally
    void sendMsg(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String token, id, targetId, message;
    	try {
	    	token = (String)json.get("TOKEN");
	    	id = (String)json.get("ID");
	    	targetId = (String)json.get("TARGET_ID");
	    	message = (String)json.get("MSG");
	    	if(token == null || id == null || targetId == null || message == null) {
	    		throw new FormatException();
	    	}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	int res = AC.sendMessage(id, targetId, message);
    	sendTempMsg(targetId);
    }
    
    
    void sendTempMsg(String id) {
    	Tempinfo info = AC.getTempinfoById(id);
    	if(AC.getAccountById(id) == null || AC.getAccountById(id).getStatus() != RESULT.STATUS_ALIVE){
    		return;
    	}
    	while(info.checkMsg() != null) {
    		HashMap<String, String> msg = info.popMsg();
    		String act = msg.get("ACT");
    		String outMsg;
    		if(act == "FRIEND_APPLY") {
    			String applyingId = msg.get("ID");
    			outMsg = getString("ACT","FRIEND_APPLY","ID",applyingId,"TIME",getTime());
    		}else if(act == "RECEIVE") {
    			String sendId = msg.get("SEND_ID");
    			String myId = msg.get("ID");
    			String sendMsg = msg.get("MSG");
    			String msgId = msg.get("MSG_ID");
    			String msgSendTime = msg.get("MSG_SEND_TIME");
    			//to be removed
    			outMsg = getString("ACT","RECEIVE","SEND_ID",sendId,"SEND_NAME",AC.getAccountById(sendId).getName(),"ID",myId,"MSG",sendMsg,"MSG_ID",msgId,"MSG_SEND_TIME",msgSendTime,"TIME",getTime());
    			//outMsg = getString("ACT","RECEIVE","SEND_ID",sendId,"ID",myId,"MSG",sendMsg,"MSG_ID",msgId,"MSG_SEND_TIME",msgSendTime,"TIME",getTime());
    		}else {
    			outMsg = getString("ACT","SEND_WRONG","ID",id,"TIME",getTime());
    		}
			send(info.getAddr(),outMsg);
    	}
    }
    //format
    //"ACT"="FRIEND_APPLY","ID"=id
    //"ACT"="RECEIVE","SEND_ID"=id,"ID"=targetId,"MSG"=message,"MSG_ID"=msgId,"MSG_SEND_TIME"=getTime()

    void addFriend(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String token,id,targetId;
    	try {
    		token = (String)json.get("TOKEN");
    		id = (String)json.get("ID");
    		targetId = (String)json.get("TARGET_ID");
    		if(token == null || id == null || targetId == null) {
	    		throw new FormatException();
	    	}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	int res = AC.friendApply(id, targetId);
    	sendTempMsg(targetId);
    	String msg;
    	if(res == RESULT.SUCCESS) {
    		//friend apply
    		msg = getString("ACT","FRIEND_APPLY","ID",id,"TIME",getTime());
    	}else {
    		//msg = getString("ACT","ADD_FAILED","ID",id,"REASON",res,"TIME",getTime());
    		//send(AC.getTempinfoById(targetId).getAddr(), msg);
    	}
		
    }
    
    void acceptApply(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String token,id,targetId;
    	try {
    		token = (String)json.get("TOKEN");
    		id = (String)json.get("ID");
    		targetId = (String)json.get("TARGET_ID");
    		if(token == null || id == null || targetId == null) {
	    		throw new FormatException();
	    	}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	int res = AC.acceptApply(id, targetId);
    	String  msg;
    	if(res == RESULT.SUCCESS) {
    		msg = getString("ACT","ADD_SUCCESS","ID",id,"TIME",getTime());
    	}else {
    		//reason  res
    		msg = getString("ACT","ADD_WRONG","ID",id,"TIME",getTime());
    	}
		send(addr,msg);
    }
    
    void rejectApply(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String token,id,targetId;
    	try {
    		token = (String)json.get("TOKEN");
    		id = (String)json.get("ID");
    		targetId = (String)json.get("TARGET_ID");
    		if(token == null || id == null || targetId == null) {
	    		throw new FormatException();
	    	}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	int res = AC.rejectApply(id, targetId);
    	String msg;
    	if(res == RESULT.SUCCESS) {
    		msg = getString("ACT","ADD_FAILED","ID",id,"TIME",getTime());
    	}else {
    		//reason  res
    		msg = getString("ACT","ADD_WRONG","ID",id,"TIME",getTime());
    	}
		send(addr,msg);
    }
    
    void sendFriendInfo(InetSocketAddress addr, JSONObject json) throws FormatException{
    	String token, id;
    	try {
    		token = (String)json.get("TOKEN");
    		id = (String)json.get("ID");
    		if(token == null || id == null) {
    			throw new FormatException();
    		}
    	}catch(Exception e) {
    		throw new FormatException();
    	}
    	String msg = getFriendInfo(id);
    	send(addr, msg);
    }
    
    String getFriendInfo(String id) {
    	Account account = AC.getAccountById(id);
    	JSONArray friendArray = new JSONArray();
    	for(String fId : account.getFriends()) {
        	JSONObject temp = new JSONObject();
    		temp.put("ID",fId);
    		temp.put("NAME",AC.getAccountById(fId).getName());
    		String status = null;
    		switch(AC.getAccountById(fId).getStatus()){
    			case RESULT.STATUS_ALIVE:
    				status = "ALIVE";
    				break;
    			case RESULT.STATUS_OFFLINE:
    				status = "OFFLINE";
    				break;
    		}
    		temp.put("STATUS",status);
    		friendArray.add(temp);
    	}
    	String friendString = friendArray.toJSONString();
    	String msg = getString("ACT","INFO_ALL","LIST",friendString,"TIME",getTime());
    	return msg;
    }
    
    String getString(Object ... objs) {
    	JSONObject jobj = new JSONObject();
    	int i;
    	for(i = 0; i < objs.length; i+=2) {
    		jobj.put((String)objs[i], objs[i+1]);
    	}
    	return jobj.toJSONString();
    }
    
    void println(Object obj) {
    	System.out.println(obj);
    }
    
    long getTime() {
    	return System.currentTimeMillis();
    }
    //test use
    void write(String s) {
    	try {
    		String dir = "info";
        	File file = new File(dir);
        	//如果文件不存在，创建文件
        	if (!file.exists()) 
        	    file.createNewFile();
        	//创建FileWriter对象
        	FileWriter writer = new FileWriter(file,true);
        	//向文件中写入内容
        	writer.write(s);
        	writer.flush();
        	writer.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    }
    
}

class FormatException extends Exception{
	private static final long serialVersionUID = 1L;
}
