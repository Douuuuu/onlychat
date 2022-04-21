package server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.alibaba.fastjson.JSONObject;



public class AccountManager {
	
	private void log(String s) {
		System.out.println(s);
	}
	
	ConcurrentHashMap<String, Account> accountMap;
	ConcurrentHashMap<String, Tempinfo> infoMap;
	
	public AccountManager() {
		accountMap = new ConcurrentHashMap<>();
		infoMap = new ConcurrentHashMap<>();
		log("System started");
	}
	//return account entry by id. null if not exists.
	public Account getAccountById(String id) {
		if(accountMap.containsKey(id)) {
			return accountMap.get(id);
		}
		return null;
	}
	
	
	public Tempinfo getTempinfoById(String id) {
		if(infoMap.containsKey(id)) {
			return infoMap.get(id);
		}
		return null;
	}
	
	//sign up
	public int signup(String id, String passwd, String name) {
		if(!idIsStandard(id)) {
			log("sign up: "+id+" id is not standard");
			return RESULT.ID_NOT_STANDARD;
		}
		if(!nameIsStandard(name)) {
			log("sign up: "+id+" name is not standard");
			return RESULT.NAME_NOT_STANDARD;
		}
		if(!passwdIsStandard(passwd)) {
			log("sign up: "+id+" password is not standard");
			return RESULT.PASSWD_NOT_STANDARD;
		}
		if(accountMap.containsKey(id)) {
			log("sign up: "+id+" id exists");
			return RESULT.ID_EXIST;
		}
		addAccount(new Account(id, passwd, name));
		log("sign up: "+id+" success");
		return RESULT.SUCCESS;
	}
	
	//login and return the token.
	public int login(String id, String pswd) {
		Account acct = getAccountById(id);
		if(acct == null) {
			log("logout: "+id+" account not exist");
			return RESULT.ID_NOT_EXIST;
		}
		if(acct.getPassword().equals(pswd)) {
			//change status
			acct.setStatus(RESULT.STATUS_ALIVE);
			String tk = String.valueOf(System.currentTimeMillis());
			setToken(id, tk);
			log("login: "+id+" success with token "+tk);
			return RESULT.SUCCESS;
		}else {
			log("login: "+id+" wrong password");
			return RESULT.PASSWD_WRONG;
		}
	}
	
	public int logout(String id, String token) {
		Account account = getAccountById(id);
		if(account == null) {
			log("logout: "+id+" account not exist");
			return RESULT.ID_NOT_EXIST;
		}
		if(account.getStatus() == RESULT.STATUS_OFFLINE) {
			log("logout: "+id+" account offline");
			return RESULT.ACCOUNT_OFFLINE;
		}else {
			if(token.equals(getTempinfoById(id).getToken())) {
				log("logout: "+id+" success");
				getAccountById(id).setStatus(RESULT.STATUS_OFFLINE);
				return RESULT.SUCCESS;
			}
		}
		log("logout: "+id+" token wrong");
		return RESULT.TOKEN_WRONG;
	}
	
	public void forceLogout(String id) {
		Account acct = getAccountById(id);
		if(acct != null) {
			acct.setStatus(RESULT.STATUS_OFFLINE);
		}
	}
	
	
	public int changePassword(String id, String old_psw, String new_psw) {
		Account account = getAccountById(id);
		if(account == null) {
			log("logout: "+id+" account not exist");
			return RESULT.ACCOUNT_OFFLINE;
		}
		if(!passwdIsStandard(new_psw)) {
			log("changepassword: "+id+" password not standard");
			return RESULT.PASSWD_NOT_STANDARD;
		}
		if(account.getPassword().equals(old_psw)) {
			if(old_psw.equals(new_psw)) {
				log("changepassword: "+id+" password unchanged");
				return RESULT.PASSWD_UNCHANGED;
			}else {
				account.setPassword(new_psw);
				log("changepassword: "+id+" success with new password "+new_psw);
				return RESULT.SUCCESS;
			}
		}else {
			log("changepassword: "+id+" wrong old password");
			return RESULT.PASSWD_WRONG;
		}
	}
	
	public int sendMessage(String id, String targetId, String message) {
		Account a = getAccountById(id);
		Account target = getAccountById(targetId);
		if(a == null || target == null) {
			log("logout: "+id+" or "+targetId+" account not exist");
			return RESULT.ACCOUNT_OFFLINE;
		}
		Tempinfo info = getTempinfoById(id);
		Tempinfo targetInfo = getTempinfoById(targetId);
		if(!a.getFriends().contains(targetId)) {
			log("sendmessage: "+id+" not friend with "+targetId);
			return RESULT.NOT_FRIEND;
		}else {
			String msgId = getMessageId();
			targetInfo.addMsg("ACT", "RECEIVE", "SEND_ID", id, "ID", targetId, "MSG", message, "MSG_ID", msgId, "MSG_SEND_TIME", getTime());
			log("sendmessage: "+id+" success with megId: "+msgId);
			return RESULT.SUCCESS;
		}
	}
	
	private static int messageId = 0;
	private synchronized String getMessageId() {
		return "" + messageId++;
	}
	
	private String getTime() {
		return ""+System.currentTimeMillis();
	}
	
	
	public int friendApply(String id, String targetId) {
		Account me = getAccountById(id);
		Account target = getAccountById(targetId);
		if(target == null || me == null) {
			return RESULT.ID_NOT_EXIST;
		}else {
			Tempinfo info = getTempinfoById(targetId);
			Tempinfo myInfo = getTempinfoById(id);
			if(info.hasApplyingFriend(id) || (me.getFriends().contains(targetId) && target.getFriends().contains(id))) {
				return RESULT.FRIEND_APPLIED;
			}else {
				info.addFriendApplying(id, MODE.APPLY_FOR_ME);
				myInfo.addFriendApplying(targetId, MODE.MY_APPLY_FOR);
				info.addMsg("ACT","FRIEND_APPLY","ID",id);
				return RESULT.SUCCESS;
			}
			
		}
	}
	
	public int acceptApply(String id, String targetId) {
		Account me = getAccountById(id);
		Account target = getAccountById(targetId);
		if(me == null || target ==null) {
			return RESULT.ID_NOT_EXIST;
		}
		if(me.getStatus() == RESULT.STATUS_OFFLINE) {
			return RESULT.ACCOUNT_OFFLINE;
		}
		if(getTempinfoById(id).hasBeenAppliedFrom(targetId)) {
			getTempinfoById(id).removeFriendApplying(targetId);
			getTempinfoById(targetId).removeFriendApplying(id);
			getAccountById(id).addFriend(targetId);
			getAccountById(targetId).addFriend(id);
			return RESULT.SUCCESS;
		}
		return RESULT.NO_APPLYING;
	}
	
	public int rejectApply(String id, String targetId) {
		Account me = getAccountById(id);
		Account target = getAccountById(targetId);
		if(me == null || target ==null) {
			return RESULT.ID_NOT_EXIST;
		}
		if(me.getStatus() == RESULT.STATUS_OFFLINE) {
			return RESULT.ACCOUNT_OFFLINE;
		}
		if(getTempinfoById(id).hasBeenAppliedFrom(targetId)) {
			getTempinfoById(id).removeFriendApplying(targetId);
			getTempinfoById(targetId).removeFriendApplying(id);
			return RESULT.SUCCESS;
		}
		return RESULT.NO_APPLYING;
	}
	
	
	private boolean idIsStandard(String id) {
		if(id.length()>CONST.NAME_MAX_LENGTH || id.length()<CONST.ID_MIN_LENGTH) {
			return false;
		}
		if(!isNumOrAlpha(id)) {
			return false;
		}
		return true;
	}
	
	private boolean nameIsStandard(String name) {
		if(name.length()>CONST.NAME_MAX_LENGTH || name.length()<CONST.NAME_MIN_LENGTH) {
			return false;
		}
		return true;
	}
	
	private boolean passwdIsStandard(String psw) {
		if(psw.length()>CONST.PASSWD_MAX_LENGTH || psw.length()<CONST.PASSWD_MIN_LENGTH) {
			return false;
		}
		if(!isNumOrAlpha(psw)) {
			return false;
		}
		return true;
	}
	
	private void addAccount(Account a) {
		accountMap.put(a.getId(), a);
		infoMap.put(a.getId(), new Tempinfo());
	}
	private void setToken(String id, String tk) {
		infoMap.get(id).setToken(tk);
	}
	
	private boolean isNumOrAlpha(String s) {
		for(int c : s.toCharArray()) {
			//if id is not consists of numbers and alphabets
			if((c<48 || c>57)&&(c<65 || c>90)&&(c<97 || c>122)) {
				return false;
			}
		}
		return true;
	}

	//test use
	
	public static void print(String s) {
		System.out.print(s);
	}
	
	public static void print_(String s) {
		print(s+" ");
	}
	
	public static void println(String s) {
		print(s+"\n");
	}
	
	public void printall() {
		for(String id : accountMap.keySet()) {
			print_(id);
			Account account = getAccountById(id);
			print_(""+account.getPassword());
			print_(""+account.getName());
			print_(""+account.getStatus());
			print_(""+account.getFriends());
			print_(""+infoMap.get(id).token);
			print_(getTempinfoById(id).messageQueue.toString());
			print_(""+getTempinfoById(id).applyingFriendMap.toString());
			print_(""+getTempinfoById(id).getAddr().getHostString()+":"+getTempinfoById(id).getAddr().getPort());
			print("\n");
		}
		print("\n");
	}

}


class Tempinfo{
	
	String token;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	InetSocketAddress addr;
	public InetSocketAddress getAddr() {
		return addr;
	}
	public void setAddr(InetSocketAddress addr) {
		this.addr = addr;
	}
	public void clearAddr() {
		this.addr = null;
	}
	ConcurrentLinkedQueue<HashMap<String, String>> messageQueue;
	public Tempinfo(String token, InetSocketAddress addr, ConcurrentLinkedQueue<HashMap<String, String>> messageQueue, ConcurrentHashMap<String, Integer> applyingFriendList) {
		this.token = token;
		this.addr = addr;
		this.messageQueue = messageQueue;
		this.applyingFriendMap = applyingFriendList;
	}
	ConcurrentHashMap<String, Integer> applyingFriendMap;
	public void addFriendApplying(String id, int mode) {
		this.applyingFriendMap.put(id, mode);
	}
	public int removeFriendApplying(String id) {
		if(!hasApplyingFriend(id)) {
			//error
			return RESULT.TOBEMADE;
		}else {
			int mode = applyingFriendMap.remove(id);
			return mode;
		}
	}
	public boolean hasFriendApplyingFor(String targetId) {
		return (applyingFriendMap.containsKey(targetId) && applyingFriendMap.get(targetId) == MODE.MY_APPLY_FOR);
	}
	
	public boolean hasBeenAppliedFrom(String targetId) {
		return (applyingFriendMap.containsKey(targetId) && applyingFriendMap.get(targetId) == MODE.APPLY_FOR_ME);
	}
	
	public boolean hasApplyingFriend(String targetId) {
		return applyingFriendMap.containsKey(targetId);
	}

	public Tempinfo() {
		this(null, null, new ConcurrentLinkedQueue<>(), new ConcurrentHashMap<>());
	}
	public void addMsg(HashMap<String, String> o) {
		messageQueue.add(o);
	}
	public void addMsg(String ... strings) {
		HashMap<String, String> map = new HashMap<>();
		for(int i = 0; i < strings.length; i += 2) {
			map.put(strings[i], strings[i+1]);
		}
		addMsg(map);
	}
	public HashMap<String, String> checkMsg(){
		return messageQueue.peek();
	}
	public HashMap<String, String> popMsg() {
		return messageQueue.poll();
	}
	
	
}


//format
//"ACT"="FRIEND_APPLY","ID"=id
//"ACT"="RECEIVE","SEND_ID"=id,"ID"=targetId,"MSG"=message,"MSG_ID"=msgId,"MSG_SEND_TIME"=getTime()



