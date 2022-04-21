package server;


import java.util.Vector;

public class Account {
	String id, passwd, name;
	long last_login_time;
	public long getLast_login_time() {
		return last_login_time;
	}

	public void setLast_login_time(long last_login_time) {
		this.last_login_time = last_login_time;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return passwd;
	}

	public void setPassword(String password) {
		this.passwd = password;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Vector<String> getFriends() {
		return friends;
	}

	public void setFriends(Vector<String> friends) {
		this.friends = friends;
	}

	int status;
	Vector<String> friends;
	
	public void addFriend(String id) {
		friends.add(id);
	}
	public void removeFriend(String id) {
		friends.remove(id);
	}

	public Account(String id, String pswd, String name) {
		this.id = id;
		this.passwd = pswd;
		this.name = name;
		status = RESULT.STATUS_OFFLINE;
		friends = new Vector<>();
		last_login_time = 0;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Account(String id, String pswd, String name, int s, Vector<String> frds) {
		this(id, pswd, name);
		status = s;
		friends = frds;
	}
}
