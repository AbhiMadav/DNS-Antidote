package Server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

@Path("/resolve")
public class DNSserverResolver {

	static InetAddress[] IPAddresses = null;
	@GET
	@Produces("application/json")
	public Response getIPs(@QueryParam("url") String clientSentence)
			throws JSONException, Exception {
		System.out.println("Got " + clientSentence);
		System.setProperty("java.net.preferIPv4Stack", "true");
		JSONObject jsonObject = new JSONObject();
		while (true) {
			URL url = new URL(clientSentence);
			String domainName = url.getHost();
			IPAddresses = Inet4Address.getAllByName(domainName);
			for (int i = 0; i < IPAddresses.length; i++) {
				jsonObject.append("ip", IPAddresses[i].toString());
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
}
