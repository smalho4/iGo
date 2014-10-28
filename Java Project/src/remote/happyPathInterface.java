

package remote;


import java.rmi.*;
import java.sql.SQLException;
import java.util.ArrayList;

/*
 * The ReceiveMessageInterface holds all methods to interact with the server stored accounts
 * 
 * NOTE receiveMessage, passArrayList and updateArrayList were for testing only and not implemented by the client
 */
public interface happyPathInterface extends Remote{

	void recieveMessage(String x)throws RemoteException;
	

	void addAccount(String username, String password, String email, String mobileNumber)throws RemoteException, ClassNotFoundException, SQLException;
	void displayFriendMenu(int db) throws RemoteException, ClassNotFoundException, SQLException;
	void addFriend(int userid, int friendid) throws RemoteException, ClassNotFoundException, SQLException; //Must add friend and apply to BOTH databases!
	
	int validateLogin (String user, String pass)throws RemoteException, ClassNotFoundException, SQLException;
	//Validate login only verifies using SQL
	//if validateLogin returns 0, login was unsuccessuful, do not show menu,
	//otherwise validatelogin will return the userID (int) which can be used to add other friends
	
	int setLocation (String city, String state, boolean nosql)throws RemoteException, ClassNotFoundException, SQLException;//input a city
	
	int getUsernamefromEmail(String email) throws RemoteException, ClassNotFoundException, SQLException;
	
	String seeResteraunts(int locationid, boolean nosql)throws RemoteException, ClassNotFoundException, SQLException;
	String seeHospitals(int locationid, boolean nosql)throws RemoteException, ClassNotFoundException, SQLException;

}
