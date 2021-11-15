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
import java.util.ArrayList;
import java.util.List;

import sequencer.core.Composer;

// Implementation of ITrackAccess that saves/loads tracks from a remote api
public class RemoteTrackAccess implements ITrackAcces {
  Composer composer;
  String baseUrl;
  URL url;
  HttpURLConnection connection;

  /**
   * The constructor for LocalTrackAccess.
   * @param composer the composer for the track you want to save/
   *        the composer you want to load new tracks to
   */
  public RemoteTrackAccess(Composer composer) {
    this.composer = composer; 
    baseUrl = "http://localhost:8080/api";
  }

  /**
   * Prepares a connection to the api.
   * @param path the path (relative to baseUrl) for the endpoint you want to connect to
   * @param requestMethod the requestMethod for the connection (e.g. GET, POST, etc.)
   */
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

  /**
   * Reads the response after a call to one of the endpoints of the api has been made.
   * @param status the statusCode for the call made
   * @return a string with the response from the call
   * @throws IOException if the response can not be read
   */
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

  /**
   * Saves the track that the composer is currently holding.
   * @throws IOException if something went wrong while saving the track
   */
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

  /**
   * Loads the track with the given trackName to the composer.
   * @param trackName the name of the track you want to load
   * @throws IOException if something went wrong while loading the track
   */
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

  /**
   * Loads saved tracks.
   * @return a list trackNames for all the saved tracks.
   * @throws IOException if something went wrong while loading the tracks
   */
  public List<String> loadTracks() throws IOException {
    setConnection("/tracks", "GET");
    int status = connection.getResponseCode();
    String responseString = readResponse(status);
    connection.disconnect();

    List<String> tracksList = new ArrayList<>();
    tracksList.add(responseString);

    return tracksList;
  }
}
