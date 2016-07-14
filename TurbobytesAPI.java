
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;

public class TurbobytesAPI {

    private String apiKey;
    private String apiSecret;
    private String timestamp; // current time in ISO 8601 format
    private String resturl ="https://api.turbobytes.com";

    public TurbobytesAPI(String api_key, String api_secret) {
        //Contact Turbobytes support for api_key and api_secret

        this.apiKey = api_key;
        this.apiSecret = api_secret;
        this.timestamp = "";
    }

    private String[] GenerateAuthHeaders(){
        //Returns the values for X-TB-Timestamp and Authentication headers
        getServerTime();
        return new String[] {timestamp, getAuthorization()};

    }
    private String getAuthorization() {
        String auth= "";
        if(apiKey != null && timestamp != null) {
            String document = apiKey + ":" + timestamp;
            auth = apiKey + ":" + getSignature(document);

        }
        return auth;
    }

    private String getSignature(String txt) {
        String algorithm = "HmacSHA1";
        String signature = "";
        if(apiSecret != null && (txt != null && txt.length() > 0)) {
            try {
                SecretKeySpec signKey = new SecretKeySpec(apiSecret.getBytes(), algorithm);
                Mac mac = Mac.getInstance(algorithm);
                mac.init(signKey);
                byte[] byteData = mac.doFinal(txt.getBytes());
                signature = new String(Base64.encodeBase64(byteData), "UTF-8");
            } catch(Exception e) {
                signature = "";
                System.out.println("Error signing string:");
                e.printStackTrace();
            }
        }
        return signature;
    }

    private String getRequest(String path, Boolean needs_auth){
        //Makes a GET request
        String data = "";
        return request("GET", path, data, needs_auth);
    }

    private String request(String method, String path, String data, Boolean needs_auth){
        //Makes a request to the api
        String[] headers = {"",""};
        String result = "";
        String responseCode = "";
        StringBuffer response = null;
        if(needs_auth)
            headers = GenerateAuthHeaders();
        if (method.equals("POST") || method.equals("PUT")){
            try {
                String uri = resturl + path;
                response = new StringBuffer();
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("X-TB-Timestamp", headers[0]);
                conn.setRequestProperty("Authorization", headers[1]);
                conn.setRequestProperty("Content-Type", "application/json");

                conn.setRequestMethod(method);
                // Send post request
                conn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();

                responseCode = "" + conn.getResponseCode();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
                rd.close();
                result = response.toString();

            }catch(Exception e){
                ;
            }
        } else {
            try{
                String uri = resturl + path;
                response = new StringBuffer();
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("X-TB-Timestamp", headers[0]);
                conn.setRequestProperty("Authorization", headers[1]);

                conn.setRequestMethod("GET");
                responseCode = "" + conn.getResponseCode();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
                rd.close();
                result = response.toString();

            }catch(Exception e){
                ;
            }
        }

        if (!responseCode.contains("200")){
            System.out.println("Failed: " + responseCode + " " + result );
            return "";
        }
        else
            return result;
    }

    private String postRequest(String path, String data, Boolean needs_auth){
        //Makes a POST request
        return request("POST", path, data, needs_auth);
    }

    private String putRequest(String path, String data, Boolean needs_auth){
        //Makes a PUT request
        return request("PUT", path, data, needs_auth);

    }

    private void getServerTime() {
        //Gets timestamp string from the server. Usefull if timegap is over 15 mins between server and client
        String responseString = getRequest("/api/now/", false);

        this.timestamp = responseString.substring(15,responseString.length()-2);
    }

    public String who_am_i(){
        //Returns username of current user
        return getRequest("/api/whoami/", true);
    }

    public String list_all_zones(){
        //Gets all zones owned by the current user
        return getRequest("/api/zones/", true);
    }

    public String get_zone(String zoneid){
        //Gets a zone identified by zoneid
        return getRequest("/api/zone/"+zoneid+"/", true);
    }

    public String get_report(String zoneid, String day){
        //Gets Zone reports
        //day format is string yyyy-mm-dd for day report and yyyy-mm for month report
        return getRequest("/api/zone/"+ zoneid +"/report/" + day.replace("-","/") + "/", true);
    }

    public String get_log_link(String zoneid, String day){
        //Gets link to download access log for zone. If logging is enabled.
        //day format is string yyyy-mm-dd
        String path = "/api/zone/"+ zoneid +"/log/"+ day.replace("-", "/") +"/";
        return getRequest(path, true);
    }

    public String purge(String zoneid, String files){
        //Purges the list files for zoneid
        String payload="";
        String path = "/api/zone/" + zoneid + "/purge/";
        try{
            payload = "{ \"files\": [\"" + files + "\"]}";

        }catch(Exception e){
            ;
        }
        return postRequest(path, payload, true);
    }

    public String latest_purges(String zoneid){
        String path = "/api/zone/" + zoneid +"/purges/";
        return getRequest(path, true);
    }

    public String purgeAll(String zoneid){
        //Purges the list files for zoneid
        String path = "/api/zone/" + zoneid + "/purge-all/";
        return postRequest(path, "" ,true);
    }
}
