package sequencer.ui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sequencer.core.Composer;

// Implementation of ITrackAccess that saves/loads tracks from a remote api
public class RemoteTrackAccess implements ITrackAccess {
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
  private void setConnection(String path, String requestMethod) throws UncheckedIOException {
    try {
      url = new URL(baseUrl + path);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      connection.setRequestProperty("Content-Type", "application/json; utf-8");
      connection.setRequestProperty("Accept", "application/json");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Saves the track that the composer is currently holding.
   * @throws IOException if something went wrong while saving the track
   */
  @Override
  public void saveTrack() throws UncheckedIOException {
    setConnection("/track", "POST");
    connection.setDoOutput(true);

    try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
      OutputStreamWriter writer = new OutputStreamWriter(out);
      composer.saveTrack(writer);
      connection.getResponseCode();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    connection.disconnect();
  }

  /**
   * Loads the track with the given trackName to the composer.
   * 
   * @param id the id of the track you want to load
   * @throws IOException if something went wrong while loading the track
   */
  @Override
  public void loadTrack(int id) throws UncheckedIOException {
    String path = String.format("/track/%d", id);
    setConnection(path, "GET");
    
    try {
      int status = connection.getResponseCode();

      DataInputStream in = null;
      if (status > 299) {
        in = new DataInputStream(connection.getErrorStream());
      } else {
        in = new DataInputStream(connection.getInputStream());
      }
      InputStreamReader reader = new InputStreamReader(in);
      composer.loadTrack(reader);    
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    connection.disconnect();
  }

  /**
   * Loads saved tracks.
   * @return a list trackNames for all the saved tracks.
   * @throws IOException if something went wrong while loading the tracks
   */
  @Override
  public List<Map<String, String>> 
      loadTracks(String trackName, String artistName) throws UncheckedIOException {
    String path = String.format("/tracks?name=%s&artist=%s", trackName, artistName);
    setConnection(path, "GET");

    String responseString;
    try {
      int status = connection.getResponseCode();
      responseString = readResponse(status);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    connection.disconnect();

    return deseralizeTracks(responseString);
  }

  /**
   * Reads the response after a call to one of the endpoints of the api has been made.
   * @param status the statusCode for the call made
   * @return a string with the response from the call
   * @throws IOException if the response can not be read
   */
  private String readResponse(int status) throws UncheckedIOException {
    Reader inputStreamReader = null;
    if (status > 299) {
      inputStreamReader = new InputStreamReader(connection.getErrorStream());
    } else {
      try {
        inputStreamReader = new InputStreamReader(connection.getInputStream());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    
    StringBuffer content = new StringBuffer();
    try (BufferedReader in = new BufferedReader(inputStreamReader)) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return content.toString();
  }

  private List<Map<String, String>> deseralizeTracks(String tracksString) {
    tracksString = tracksString.substring(2,tracksString.length() - 2);
    String[] tracksArray = tracksString.split("\\},\\{");

    List<Map<String, String>> tracks = new ArrayList<>();
    for (int i = 0; i < tracksArray.length; i++) {
      Map<String, String> trackMap = new HashMap<>();
      String[] trackParams = tracksArray[i].split(",");
      for (int j = 0; j < trackParams.length; j++) {
        String[] trackParamArray = trackParams[j].split(":");
        String param = trackParamArray[0].replaceAll("\"", "");
        String value = trackParamArray[1].replaceAll("\"", "");
        trackMap.put(param, value);
      }
      tracks.add(trackMap);
    }

    return tracks;
  }
}
