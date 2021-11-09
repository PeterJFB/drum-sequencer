package sequencer.ui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import sequencer.core.Composer;

public class RemoteTrackAccess implements ITrackAcces {
  Composer composer;
  String baseUrl;
  URL url;
  HttpURLConnection connection;

  public RemoteTrackAccess(Composer composer) {
    this.composer = composer; 
    baseUrl = "http://localhost:8080/api"; // TODO: find out what the base url for our api is
  }

  private void setConnection(String path, String requestMethod) {
    try {
      url = new URL(baseUrl + path);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      connection.setRequestProperty("Content-Type", "application/json; utf-8");
      connection.setRequestProperty("Accept", "application/json");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (ProtocolException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String readResponse(int status) throws IOException {
    Reader inputStreamReader = null;
    if (status > 299) {
      inputStreamReader = new InputStreamReader(connection.getErrorStream());
    } else {
      inputStreamReader = new InputStreamReader(connection.getInputStream());
    }
    BufferedReader in = new BufferedReader(inputStreamReader);
    
    String inputLine;
    StringBuffer content = new StringBuffer();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();

    return content.toString();
  }

  public void saveTrack() throws IOException {
    setConnection(String.format("/track/%s", composer.getTrackName()), "POST");

    connection.setDoOutput(true);
    try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
      OutputStreamWriter writer = new OutputStreamWriter(out);
      composer.saveTrack(writer);
    }

    connection.getResponseCode();
    connection.disconnect();
  }

  public void loadTrack(String trackName) throws IOException {
    setConnection(String.format("/track/%s", trackName), "GET");
    int status = connection.getResponseCode();

    DataInputStream in = null;
    if (status > 299) {
      in = new DataInputStream(connection.getErrorStream());
    } else {
      in = new DataInputStream(connection.getInputStream());
    }
    InputStreamReader reader = new InputStreamReader(in);
    composer.loadTrack(reader);    

    connection.disconnect();
  }

  public String loadTracks() throws IOException {
    setConnection("/tracks", "GET");
    int status = connection.getResponseCode();
    String responseString = readResponse(status);
    connection.disconnect();

    return responseString;

  }
}
