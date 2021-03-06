/*
 * iGO
 * CS 442 Project
 * 
 * The happypath server implements a simplified version of our full interface, the happyPathInterface
 * 
 * This server has basic functionality relating to creating accounts, validating logins, adding/viewing friends,
 * setting the city to search in, and searching for restraunts or hospitals in a city.
 */
package remote;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.io.IOException;
import java.net.*;
import java.sql.*;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.json.JSONException;

import yelp.Yelp;



@WebService
@SuppressWarnings("serial")
public class HappyPathServer extends UnicastRemoteObject implements
		happyPathInterface {

	int dbselection = 1;

	// static Connection conn;

	public HappyPathServer() throws RemoteException {}
	
	public static void main(String args[]) {
		Registry registry;

		// Server arugments
		int serverPort;
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";

		// mySQL connection variables
		String mySQLurl;
		String mySQLusername = "root";
		String mySQLpass = "cs411";
		String mongoHost;
		int mongoPort;

		// selection will set database type
		int databaseSelection = 0;
		if (args.length < 2) {
			System.out
					.println("Usage: ./[programname][server port][full mysql url][mongodb hostname][mongodb port]");
		}

		// read port argument if present
		if (args.length < 1) {
			serverPort = 12345;
		} else {
			// serverPort=Integer.parseInt(args[0]);
			serverPort = 12345;
		}
		if (args.length < 4) {
			mongoPort = 27017;
		} else {
			mongoPort = Integer.parseInt(args[3]);
		}
		if (args.length < 3) {
			mongoHost = "localhost";
		} else {
			mongoHost = args[2];
		}
		if (args.length < 2) {
			mySQLurl = "jdbc:mysql://localhost:3306/igo";
		} else {
			mySQLurl = ("jdbc:mysql://" + args[1]);
		}

		HappyPathServer server = createServer();
		connectRMI(serverPort);
		NoSQLServer.generateData();
		System.out.println("Rmi connected");

	}
	
	//Create an instance for static server

	public static HappyPathServer createServer() {
		try {
			HappyPathServer server = new HappyPathServer();
			// Generating Data for NoSQL
			return server;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;

	}

	//Create the RMI connection on the local host
	public static void connectRMI(int serverPort) {
		Registry registry;
		String address = null;
		// gets address of local host to deploy the server on
		try {
			address = (InetAddress.getLocalHost()).toString();
		} catch (Exception e) {
			System.out.println("can't get inet address.");
		}

		int port = serverPort;

		// displays server ip and port on console
		System.out.println("this address=" + address + ",port=" + port);
		try {

			HappyPathServer obj = new HappyPathServer();
			// ReceiveMessageInterface stub = (ReceiveMessageInterface)
			// UnicastRemoteObject.exportObject(obj, 0);
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("rmiServer", obj);
			// System.out.println("RMI connected");

		} catch (RemoteException e) {
			System.out.println("remote exception" + e);
		}

		// System.out.println("RMI connected");
	}

	/*
	 * Validate login will check the database to match up the users provided
	 * username and password and provide the client with the users id if
	 * successful, otherwise it will return a zero(non-Javadoc)
	 * 
	 * @see remote.happyPathInterface#validateLogin(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public int validateLogin(String user, String pass, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {

		if (!nosql) {

			Connection con = connectSQL();
			Statement stmt = con.createStatement();
			boolean found = false;

			String query = "SELECT * FROM users where  Name= '" + user
					+ "' limit 1;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- checking password...");
			ResultSet rs = queryMySQL(stmt, query);

			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("userID");
				String username = rs.getString("Name");
				String password = rs.getString("password");
				if (pass.equals(password)) {
					found = true;
					closeResults(stmt, rs);

					System.out.println("User" + id + " logged in");
					return id;
				}
			}
			closeResults(stmt, rs);

			if (!found) {
				closeConnection(con);
			}
			System.out.println("User" + 0 + " logged in");
			return 0;

		} else {
			// mongodb validation here
			int id = NoSQLServer.validateLogin(user, pass);

			System.out.println("User" + id + " logged in");
			return id;
		}
	}

	/*
	 * closeConnection - this is used to sever a client connection to mySQL
	 */

	private static void closeResults(Statement stmt, ResultSet rs)
			throws SQLException {
		rs.close();
		stmt.close();
		// conn.close();
	}

	private static void closeInsertion(Statement stmt) throws SQLException {

		stmt.close();
		// conn.close();
	}

	private static void closeConnection(Connection conn) throws SQLException {

		conn.close();
	}

	/*
	 * setLocation: This method is used to lookup a location id in our database
	 * and return to the client (non-Javadoc)
	 * 
	 * @see remote.happyPathInterface#setLocation(java.lang.String,
	 * java.lang.String, boolean)
	 */

	@Override
	public int setLocation(String loc, String state, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {

		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();
			// Connection conn = connectMySQL(c);
			String query = "SELECT * FROM locations where LocationName='" + loc
					+ "' limit 1;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- lookin up city...");
			ResultSet rs = queryMySQL(stmt, query);

			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("locationID");
				closeResults(stmt, rs);
				return id;
			}

			String query2 = "SELECT * FROM locations where State='" + state
					+ "' AND SubType='General';";

			// STEP 4: Execute a query
			System.out.println("City Not Found- looking up state...");
			stmt = con.createStatement();
			ResultSet rs2 = queryMySQL(stmt, query2);

			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("locationID");
				closeResults(stmt, rs);
				return id;
			}

			closeResults(stmt, rs);
			return 0;

		} else if (nosql) {
			// mongodb setLocation function called
			int id = NoSQLServer.setLocation(loc, state);
			return id;
		}
		return 0;
	}

	/*
	 * seeRestraunts: This method returns a JSON list of restaurants in the
	 * selected location (non-Javadoc)
	 * 
	 * @see remote.happyPathInterface#seeResteraunts(int, boolean)
	 */

	@Override
	public String seeRestauraunts(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {

		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder restaurantsJSON = new StringBuilder(1000);
			restaurantsJSON.append("{ Restaurants:[");
			String query = "SELECT * FROM restaurants where locationid = "
					+ locationid + " limit 100;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying resteraunts...");
			// stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int restid = rs.getInt("restrauntid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				Restaurant temp = new Restaurant(gps, name, locationid, restid,
						type);
				System.out.println(temp.toString());
				restaurantsJSON.append(temp.toString());

				restaurantsJSON.append(",");
			}

			closeResults(stmt, rs);
			restaurantsJSON.append("]}");
			closeConnection(con);

			return restaurantsJSON.toString();
		} else {
			// mongodb seeRestaurants called here
			String restaurantsJSON = NoSQLServer.seeRestaurants(locationid);
			return restaurantsJSON;
		}

	}

	private Connection connectSQL() throws ClassNotFoundException, SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.sql.Connection con;
		con = DriverManager.getConnection("jdbc:mysql://localhost/igo", "root",
				"");
		return con;
	}

	/*
	 * AddAccount: This method adds a new user with credentials to our database
	 * (non-Javadoc)
	 * 
	 * @see remote.happyPathInterface#addAccount(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void addAccount(String username, String password, String email,
			String mobile, boolean nosql) throws RemoteException,
			ClassNotFoundException, SQLException {

		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			boolean found = false;
			// Connection conn = connectMySQL();
			String query = "INSERT INTO users ( `Name`, `Email`, `MobileNumber`, `Password`) VALUES ('"
					+ username
					+ "', '"
					+ email
					+ "', '"
					+ mobile
					+ "', '"
					+ password + "');";

			// STEP 4: Execute a query
			System.out.println("Creating statement- adding user");
			insertMySQL(stmt, query);
			closeInsertion(stmt);
			closeConnection(con);
		} else {
			// mongodb seeRestaurants called here
			NoSQLServer.addAccount(username, password, email, mobile);
		}

	}

	/*
	 * queryMySQL: This method executes a query and returns the result set in an
	 * object
	 */
	private ResultSet queryMySQL(Statement stmt, String query)
			throws SQLException {
		String sql = query;
		ResultSet rs = stmt.executeQuery(sql);
		return rs;
	}

	/*
	 * insertMySQL: This method is used to execute insertion queries
	 */
	private void insertMySQL(Statement stmt, String query) throws SQLException {
		String sql = query;
		stmt.executeUpdate(sql);
	}

	/*
	 * connectMySQL: This method is used to connect to an instance of mySQL with
	 * hardcoded connection credentials and schema name
	 */

	private void connectMySQL(Connection conn) throws ClassNotFoundException,
			SQLException {
		String conURL = "jdbc:mysql://localhost:3306/igo";
		int conPort = 3306;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(conURL, "root", "");
			System.out.print("connected");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
			// System.out.println("SQL Connected");

	}

	/*
	 * recieveMessage: test RMI method (non-Javadoc)
	 * 
	 * @see remote.happyPathInterface#recieveMessage(java.lang.String)
	 */

	@Override
	public void recieveMessage(String x) throws RemoteException {
		System.out.print(x + " received!");

	}

	/*
	 * displayFriendMenu: This method displays users in our database and their
	 * ids for the client to add friends (non-Javadoc) (Unimplemented by server/client)
	 * 
	 * @see remote.happyPathInterface#displayFriendMenu(int)
	 */
	@Override
	public void displayFriendMenu(int db, boolean nosql) throws RemoteException {

	}

	/*
	 * addFriend: This method adds a friendship relationship to the database
	 * (non-Javadoc)
	 * 
	 * @see remote.happyPathInterface#addFriend(int, int)
	 */
	@Override
	public void addFriend(int userid, int friendid, boolean nosql)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getUsernamefromEmail(String email, boolean nosql)
			throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String seeHospitals(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder hospitalsJSON = new StringBuilder(10000);
			hospitalsJSON.append("{Hospitals: [");
			String query = "SELECT * FROM hospitals where locationid = "
					+ locationid + ";";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying hospitals...");

			ResultSet rs = stmt.executeQuery(query);
			int counter = 0;
			while (rs.next()) {
				int restid = rs.getInt("hospitalid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				String contact = rs.getString("contactnumber");
				Hospital temp = new Hospital(gps, name, locationid, restid,
						type, contact);
				hospitalsJSON.append(temp.toString());
				hospitalsJSON.append(",");
			}

			closeResults(stmt, rs);
			hospitalsJSON.append("]}");

			closeConnection(con);
			return hospitalsJSON.toString();
		} else {
			// mongodb seeRestaurants called here
			String hospitalsJSON = NoSQLServer.seeHospitals(locationid);
			return hospitalsJSON;
		}

		// TODO Auto-generated method stub
	}

	@Override
	public void displayMyFriends(int userid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void Logout(int userid) throws RemoteException,
			ClassNotFoundException, SQLException {
		

	}

	// WebService - Lokesh, Chetana
	@Override
	@WebMethod
	public String getData(String Term, String Location) throws IOException,
			JSONException {
		Yelp yelp = new Yelp();
		yelp.getData(Term, Location);
		return null;
	}

	@Override
	public String seeAirports(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder restaurantsJSON = new StringBuilder(1000);
			restaurantsJSON.append("{ Airports:[");
			String query = "SELECT * FROM airports where locationid = "
					+ locationid + " limit 100;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying resteraunts...");
			// stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int restid = rs.getInt("airportid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				Airport temp = new Airport(gps, name, locationid, restid);
				System.out.println(temp.toString());
				restaurantsJSON.append(temp.toString());

				restaurantsJSON.append(",");
			}

			closeResults(stmt, rs);
			restaurantsJSON.append("]}");
			closeConnection(con);

			return restaurantsJSON.toString();
		} else {
			// mongodb seeRestaurants called here
			String airportJSON = NoSQLServer.seeAirports(locationid);
			return airportJSON;
		}

	}

	@Override
	public String seeBanks(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder restaurantsJSON = new StringBuilder(1000);
			restaurantsJSON.append("{ Banks:[");
			String query = "SELECT * FROM banks where locationid = "
					+ locationid + " limit 100;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying resteraunts...");
			// stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int restid = rs.getInt("bankid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				Bank temp = new Bank(gps, name, locationid, restid);
				System.out.println(temp.toString());
				restaurantsJSON.append(temp.toString());

				restaurantsJSON.append(",");
			}

			closeResults(stmt, rs);
			restaurantsJSON.append("]}");
			closeConnection(con);

			return restaurantsJSON.toString();
		}
		
		else{
			String bankJSON = NoSQLServer.seeBanks(locationid);
			return bankJSON;
		}
			
	}
	
	/*This method queries the bar tables and returns a formatted JSON string to the client*/

	@Override
	public String seeBars(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder restaurantsJSON = new StringBuilder(1000);
			restaurantsJSON.append("{ bars:[");
			String query = "SELECT * FROM bars where locationid = "
					+ locationid + " limit 100;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying resteraunts...");
			// stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int restid = rs.getInt("barid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				Bar temp = new Bar(gps, name, locationid, restid);
				System.out.println(temp.toString());
				restaurantsJSON.append(temp.toString());

				restaurantsJSON.append(",");
			}

			closeResults(stmt, rs);
			restaurantsJSON.append("]}");
			closeConnection(con);

			return restaurantsJSON.toString();
		}
		
		else{
			String BarJSON = NoSQLServer.seeBars(locationid);
			return BarJSON;
		}
	}

	/*This method queries the hotels tables and returns a formatted JSON string to the client*/

	@Override
	public String seeHotels(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder restaurantsJSON = new StringBuilder(1000);
			restaurantsJSON.append("{ hotels:[");
			String query = "SELECT * FROM hotels where locationid = "
					+ locationid + " limit 100;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying resteraunts...");
			// stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int restid = rs.getInt("hotelid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				Hotel temp = new Hotel(gps, name, locationid, restid, type);
				System.out.println(temp.toString());
				restaurantsJSON.append(temp.toString());

				restaurantsJSON.append(",");
			}

			closeResults(stmt, rs);
			restaurantsJSON.append("]}");
			closeConnection(con);

			return restaurantsJSON.toString();
		}
		
		else{
			String HotelJSON = NoSQLServer.seeHotels(locationid);
			return HotelJSON;
		}
	}

	/*This method queries the theaters tables and returns a formatted JSON string to the client*/

	@Override
	public String seeTheaters(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder theatersJSON = new StringBuilder(1000);
			theatersJSON.append("{ Theaters:[");
			String query = "SELECT * FROM movietheaters where locationid = "
					+ locationid + " limit 100;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying theaters..");
			// stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int restid = rs.getInt("theaterid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				Hotel temp = new Hotel(gps, name, locationid, restid, type);
				System.out.println(temp.toString2());
				theatersJSON.append(temp.toString2());

				theatersJSON.append(",");
			}

			closeResults(stmt, rs);
			theatersJSON.append("]}");
			closeConnection(con);

			return theatersJSON.toString();
		}
		
		else{
			String TJSON = NoSQLServer.seeTheaters(locationid);
			return TJSON;
		}
	}

	/*This method queries the police station table and returns a formatted JSON string to the client*/

	@Override
	public String seePolice(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder policesJSON = new StringBuilder(10000);
			policesJSON.append("{PoliceStations: [");
			String query = "SELECT * FROM policestations where locationid = "
					+ locationid + ";";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying Police...");

			ResultSet rs = stmt.executeQuery(query);
			int counter = 0;
			while (rs.next()) {
				int restid = rs.getInt("policeid");
				String name = rs.getString("name");
			//	String type = rs.getString("type");
				String gps = rs.getString("gps");
				String contact = rs.getString("contactnumber");
				Hospital temp = new Hospital(gps, name, locationid, restid,
						"emergency", contact);
				policesJSON.append(temp.toString2());
				policesJSON.append(",");
			}

			closeResults(stmt, rs);
			policesJSON.append("]}");

			closeConnection(con);
			return policesJSON.toString();
		} else {
			// mongodb seeRestaurants called here
			String policesJSON = NoSQLServer.seePolice(locationid);
			return policesJSON;
		}

	}
	
	/*This method queries the school table and returns a formatted JSON string to the client*/


	@Override
	public String seeSchool(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder schoolJSON = new StringBuilder(10000);
			schoolJSON.append("{Schools: [");
			String query = "SELECT * FROM schools where locationid = "
					+ locationid + ";";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying Schools...");

			ResultSet rs = stmt.executeQuery(query);
			int counter = 0;
			while (rs.next()) {
				int restid = rs.getInt("schoolid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
			//	String contact = rs.getString("contactnumber");
				Hospital temp = new Hospital(gps, name, locationid, restid,
						type, "123");
				schoolJSON.append(temp.toString3());
				schoolJSON.append(",");
			}

			closeResults(stmt, rs);
			schoolJSON.append("]}");

			closeConnection(con);
			return schoolJSON.toString();
		} else {
			// mongodb seeRestaurants called here
			String sJSON = NoSQLServer.seeSchools(locationid);
			return sJSON;
		}
	}
	
	/*This method queries the store table and returns a formatted JSON string to the client*/


	@Override
	public String seeStore(int locationid, boolean nosql)
			throws RemoteException, ClassNotFoundException, SQLException {
		if (!nosql) {
			Connection con = connectSQL();
			Statement stmt = con.createStatement();

			StringBuilder storesJSON = new StringBuilder(1000);
			storesJSON.append("{ Stores:[");
			String query = "SELECT * FROM stores where locationid = "
					+ locationid + " limit 100;";

			// STEP 4: Execute a query
			System.out.println("Creating statement- querying stores..");
			// stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				int restid = rs.getInt("storeid");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String gps = rs.getString("gps");
				Hotel temp = new Hotel(gps, name, locationid, restid, type);
				System.out.println(temp.toString3());
				storesJSON.append(temp.toString3());

				storesJSON.append(",");
			}

			closeResults(stmt, rs);
			storesJSON.append("]}");
			closeConnection(con);

			return storesJSON.toString();
		}
		
		else{
			String TJSON = NoSQLServer.seeStores(locationid);
			return TJSON;
		}
	}

}