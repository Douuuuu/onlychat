package server;

class RESULT {
	public String result(int i) {
		String s;
		switch(i) {
		case -3:
			s = "unknow";
		case -2:
			s = "tobemade";
		case -1:
			s = "success";
		case 2:
			s = "id exist";
		case 3:
			s = "password not standard";
		case 4:
			s = "id not standard";
		case 5:
			s = "name not standard";
		case 6:
			s = "password unchanged";
		case 7:
			s = "password wrong";
		case 8:
			s = "not friend";
		case 9:
			s = "id not exist";
		case 10:
			s = "friend applied";
		case 11:
			s = "no applying";
		case 12:
			s = "account offline";
		case 13:
			s = "token wrong";
		default:
			s = "error";
		}
		return s;
	}
	
	public static final int UNKNOWN = -3;
	public static final int TOBEMADE = -2;
	public static final int SUCCESS = -1;
	public static final int STATUS_OFFLINE = 0, STATUS_ALIVE = 1;
	public static final int ID_EXIST = 2, PASSWD_NOT_STANDARD = 3, ID_NOT_STANDARD = 4, NAME_NOT_STANDARD = 5;
	public static final int PASSWD_UNCHANGED = 6, PASSWD_WRONG = 7;//PASSWD_NOT_STANDARD = 3;
	public static final int NOT_FRIEND = 8;
	public static final int ID_NOT_EXIST = 9;
	public static final int FRIEND_APPLIED = 10;
	public static final int NO_APPLYING = 11;
	public static final int ACCOUNT_OFFLINE = 12;
	public static final int TOKEN_WRONG = 13;
}

class CONST {
	public static final int ID_MAX_LENGTH = 10, ID_MIN_LENGTH = 1;
	public static final int NAME_MAX_LENGTH = 10, NAME_MIN_LENGTH = 1;
	public static final int PASSWD_MIN_LENGTH = 8, PASSWD_MAX_LENGTH = 16;
}

class MODE {
	public static final int MY_APPLY_FOR = 0, APPLY_FOR_ME = 1;
}