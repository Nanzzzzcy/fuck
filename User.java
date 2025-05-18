package GUI8;

public class User {
	private String name;
	private String ID;
	private String pwd;

	public User(String name, String ID, String pwd) {
		this.name = name;
		this.ID = ID;
		this.pwd = pwd;
	}

	public void Browser(String filePath) {
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getID() {
		return ID;
	}

	public void setID(String newID) {
		this.ID = newID;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String newPwd) {
		this.pwd = newPwd;
	}

}
