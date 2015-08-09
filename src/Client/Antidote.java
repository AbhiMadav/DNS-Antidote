package Client;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Antidote {

	static InetAddress[] IPAddresses = null;
	static int flag = 0;
	static String REDIRECT_MESSAGE = "There was an error in your DNS LookUp. Possible DNS Poisoning. You are being redirected...";
	static HashMap<String, LinkedHashSet<String>> client_hmap = new HashMap<String, LinkedHashSet<String>>();

	public static boolean check_IP_Validity(
			LinkedHashSet<String> Set_FromLocalLookUp,
			LinkedHashSet<String> Set_FromLocalCache) {

		for (String s : Set_FromLocalLookUp) {
			if (!Set_FromLocalCache.contains(s)) {
				return false;
			}
		}
		return true;
	}

	public static LinkedHashSet<String> generateIPList(String URLfromUser)
			throws Exception {
		LinkedHashSet<String> IP_Addresses = new LinkedHashSet<String>();
		URL url = new URL(URLfromUser);
		String domainName = url.getHost();
		IPAddresses = null;
		IPAddresses = Inet4Address.getAllByName(domainName);

		IP_Addresses = new LinkedHashSet<String>();
		System.out.println("Local Resolution:");
		for (int i = 0; i < IPAddresses.length; i++) {
			System.out.println(IPAddresses[i]);
			IP_Addresses.add(IPAddresses[i].toString());
		}

		return IP_Addresses;
	}

	public static void main(String[] args) throws Exception {

		Object input = JOptionPane.showInputDialog(null,
				"\nWhich Secure Browsers do you want to open?\n",
				"Choose a Browser:", JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "Chrome", "Firefox", "Chrome and Firefox" },
				"Chrome");
		String input_string = input.toString();
		System.out.println(input_string);

		File chrome_file = new File("drivers/chromedriver.exe");

		System.setProperty("webdriver.chrome.driver",
				chrome_file.getAbsolutePath());

		WebDriver Chrome_Driver = null;
		WebDriver Firefox_Driver = null;

		String curl = "", furl = "";

		if (input_string.equals("Chrome")) {
			Chrome_Driver = new ChromeDriver();
			curl = Chrome_Driver.getCurrentUrl();
		}
		else if (input_string.equals("Firefox")) {
			Firefox_Driver = new FirefoxDriver();
			furl = Firefox_Driver.getCurrentUrl();
		}

		else if (input_string.equals("Chrome and Firefox")) {
			Chrome_Driver = new ChromeDriver();
			curl = Chrome_Driver.getCurrentUrl();
			Firefox_Driver = new FirefoxDriver();
			furl = Firefox_Driver.getCurrentUrl();
		}

		while (true) {

			if (input_string.equals("Chrome")) {
				if (curl.equals(Chrome_Driver.getCurrentUrl())) {
				}
				else {
					curl = Chrome_Driver.getCurrentUrl();
					StartChromeBrowser(curl, Chrome_Driver);
				}
			} else if (input_string.equals("Firefox")) {
				if (furl.equals(Firefox_Driver.getCurrentUrl())) {
				} else {
					furl = Firefox_Driver.getCurrentUrl();
					StartFirefoxBrowser(furl, Firefox_Driver);
				}
			} else if (input_string.equals("Chrome and Firefox")) {
				if (curl.equals(Chrome_Driver.getCurrentUrl())
						&& furl.equals(Firefox_Driver.getCurrentUrl())) {
				} else if ((!curl.equals(Chrome_Driver.getCurrentUrl()))
						&& furl.equals(Firefox_Driver.getCurrentUrl())) {
					curl = Chrome_Driver.getCurrentUrl();
					StartChromeBrowser(curl, Chrome_Driver);
				} else if ((curl.equals(Chrome_Driver.getCurrentUrl()))
						&& !furl.equals(Firefox_Driver.getCurrentUrl())) {
					furl = Firefox_Driver.getCurrentUrl();
					StartFirefoxBrowser(furl, Firefox_Driver);
				}
			}
		}
	}

	private static void StartFirefoxBrowser(String furl,
			WebDriver Firefox_Driver) throws Exception {

		furl = Firefox_Driver.getCurrentUrl();
		LinkedHashSet<String> Local_IP_Addresses = generateIPList(furl);

		if (client_hmap.containsKey(furl)) {

			if (!check_IP_Validity(Local_IP_Addresses, client_hmap.get(furl))) {
				System.out.println(REDIRECT_MESSAGE);

				redirect(Firefox_Driver, furl);

			}

		}

		else {

			LinkedHashSet<String> IP_Addresses_FromServer = getIP_FromServer(furl);

			if (check_IP_Validity(Local_IP_Addresses, IP_Addresses_FromServer)) {

				client_hmap.put(furl, IP_Addresses_FromServer);

			}

			else {

				client_hmap.put(furl, IP_Addresses_FromServer);

				System.out.println(REDIRECT_MESSAGE);

				redirect(Firefox_Driver, furl);
			}
		}
	}

	public static void StartChromeBrowser(String curl, WebDriver Chrome_Driver)
			throws Exception {
		curl = Chrome_Driver.getCurrentUrl();
		LinkedHashSet<String> Local_IP_Addresses = generateIPList(curl);
		if (client_hmap.containsKey(curl)) {
			if (!check_IP_Validity(Local_IP_Addresses, client_hmap.get(curl))) {
				System.out.println(REDIRECT_MESSAGE);
				redirect(Chrome_Driver, curl);
			}
		} else {
			LinkedHashSet<String> IP_Addresses_FromServer = getIP_FromServer(curl);
			if (check_IP_Validity(Local_IP_Addresses, IP_Addresses_FromServer)) {
				client_hmap.put(curl, IP_Addresses_FromServer);
			} else {
				client_hmap.put(curl, IP_Addresses_FromServer);
				System.out.println(REDIRECT_MESSAGE);
				redirect(Chrome_Driver, curl);
			}
		}
	}

	private static void redirect(WebDriver Browser_Driver, String curl) // changed
	// from
	// Chrome_Driver
			throws Exception {

		JavascriptExecutor javascript = (JavascriptExecutor) Browser_Driver;
		javascript
		.executeScript("alert('DNS Antidote: Possible DNS POISONING detected!! \\nYou are now being redirected...');");

		Thread.sleep(3500);
		Browser_Driver.switchTo().alert().accept();
		String redirect = "";
		for (String s : client_hmap.get(curl)) {
			redirect = s.split("/")[1];
			redirect = "http://" + redirect;
			break;
		}
		Browser_Driver.navigate().to(redirect);
	}

	private static LinkedHashSet<String> getIP_FromServer(String urlFromBrowser)
			throws Exception {

		LinkedHashSet<String> IPs_From_Server = new LinkedHashSet<String>();

		try {
			// Amazon Server IP - Avoiding DNS Cache poisoning attack
			String serverLink = "http://52.25.99.179";
			String midLink = "/server/resolve?url=";

			URL url = new URL(serverLink + midLink + urlFromBrowser);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				Object obj = new org.json.simple.parser.JSONParser()
				.parse(output);
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray msg = (JSONArray) jsonObject.get("ip");
				for (int i = 0; i < msg.size(); i++) {
					Object object = msg.get(i);
					System.out.println(object.toString());
					IPs_From_Server.add(object.toString());
				}
			}
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return IPs_From_Server;
	}
}
