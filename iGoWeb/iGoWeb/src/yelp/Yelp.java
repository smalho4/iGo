package yelp;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import remote.NoSQLServer;

import java.io.IOException;

public class Yelp {

	private static final String API_HOST = "api.yelp.com";
	private static final int SEARCH_LIMIT = 20;
	private static final String SEARCH_PATH = "/v2/search";
	private static final String BUSINESS_PATH = "/v2/business";

	OAuthService service;
	Token accessToken;

	public Yelp() {
		final String consumerKey = "5in8lbmn3uf_8SFMaVg1QA";
		final String consumerSecret = "VSEKLFD5bWDbJQFunxeIOWOE6h0";
		final String token = "i0E7MgkHXpCaqPj8337DXic4lUHoP-LD";
		final String tokenSecret = "JOipNcXavLPOgwgKSpDQMK5YQ-o";

		this.service = new ServiceBuilder().provider(TwoStepOAuth.class)
				.apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
	}

	public String searchForBusinessesByLocation(String term, String location) {
		OAuthRequest request = createOAuthRequest(SEARCH_PATH);
		request.addQuerystringParameter("term", term);
		request.addQuerystringParameter("location", location);
		request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
		return sendRequestAndGetResponse(request);
	}

	public String searchByBusinessId(String businessID) {
		OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/"
				+ businessID);
		return sendRequestAndGetResponse(request);
	}

	private OAuthRequest createOAuthRequest(String path) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST
				+ path);
		return request;
	}

	private String sendRequestAndGetResponse(OAuthRequest request) {
		System.out.println("Querying " + request.getCompleteUrl() + " ...");
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		return response.getBody();
	}

	private static String queryAPI(Yelp yelpApi, String Term, String Location,
			String TableType) throws IOException, JSONException {
		String searchResponseJSON = yelpApi.searchForBusinessesByLocation(Term,
				Location);

		JSONParser parser = new JSONParser();
		JSONObject response = null;
		try {
			response = (JSONObject) parser.parse(searchResponseJSON);

			System.out.println("Hi.. I am the first response: \n" + response
					+ "\n .... I end here");
		}

		catch (ParseException pe) {
			System.out.println("Error: could not parse JSON response:");
			System.out.println(searchResponseJSON);
			System.exit(1);
		}
		// Here is my new changes to the code.. Lokesh
		System.out.println(response);
		JSONArray businesses = (JSONArray) response.get("businesses");
		JSONObject client = new JSONObject();
		JSONArray clients = new JSONArray();
		String lattitude = ((JSONObject) ((JSONObject) response.get("region"))
				.get("center")).get("latitude").toString();
		String longitude = ((JSONObject) ((JSONObject) response.get("region"))
				.get("center")).get("longitude").toString();
		String gpscoor = lattitude + "," + longitude;
		System.out.println(gpscoor);
		
		

		for (int i = 0; i < businesses.size(); i++) {
			JSONObject s = (JSONObject) businesses.get(i);
			//Inserting into MySql DB
			int currLocationId = Integer.parseInt(dataInsert.add2DB(s,
					TableType, gpscoor));
			
			//now insert these into NoSQL
			String catType = ((JSONArray) (((JSONArray) (s.get("categories")))
					.get(0))).get(0).toString();
			s.remove("categories");
			s.put("type", catType);
			JSONObject location;
			try {
				location = (JSONObject) (s.get("location"));
				lattitude = ((JSONObject) location.get("coordinate")).get(
						"latitude").toString();
				longitude = ((JSONObject) location.get("coordinate")).get(
						"longitude").toString();
			} catch (Exception e) {
				longitude = "NA";
				lattitude = "NA";
			}
			s.remove("location");
			s.put("latitude",lattitude);
			s.put("longitude",longitude);
			if (currLocationId != -1) {
				s.put("locationid", currLocationId);
				if(TableType.equals("restaurants"))
				{
				int restaurantId= NoSQLServer.getNextId("restaurants");
				s.put("restaurantid", restaurantId);
				}
				
			else if(TableType.equals("hospitals"))
				{
					int hospitalId= NoSQLServer.getNextId("hospitals");
					s.put("hospitalid", hospitalId);
				}
				NoSQLServer.insertNoSQL(TableType, s.toString());
			}
			String restid = s.get("id").toString();
			String restType = s.get("type").toString();
			String name = s.get("name").toString();
			client = new JSONObject();
			client.put("id", restid);
			client.put("type", restType);
			client.put("longitude", longitude);
			client.put("lattitude", lattitude);
			client.put("name", name);
			clients.add(client);
		}
		// Here my new changes ends.
		return clients.toString();
	}

	/*
	 * private static class YelpAPICLI {
	 * 
	 * @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
	 * public String term = DEFAULT_TERM;
	 * 
	 * @Parameter(names = {"-l", "--location"}, description =
	 * "Location to be Queried") public String location = DEFAULT_LOCATION;
	 * final String term = "Subway"; final String location = "Chicago, IL";
	 * 
	 * }
	 */

	public String getData(String Term, String Location, String TableType)
			throws IOException, JSONException {
		// YelpAPICLI yelpApiCli = new YelpAPICLI();
		// new JCommander(yelpApiCli, args);

		Yelp yelpApi = new Yelp();
		String SearchTerm = Term + "  " + TableType;
		String Str = queryAPI(yelpApi, SearchTerm, Location, TableType);
		return (Str);
	}

	public static int getData1(int i) {
		return i * i;
	}
}