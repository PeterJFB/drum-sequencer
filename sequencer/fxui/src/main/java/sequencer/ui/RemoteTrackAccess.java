package sequencer.ui;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;
import sequencer.core.Composer;
import sequencer.json.TrackMapper;
import sequencer.json.TrackSearchResult;

/**
 * Implementation of {@link TrackAccessInterface} that saves/loads tracks from a remote api.
 */
public class RemoteTrackAccess implements TrackAccessInterface {
  String baseUrl;
  URL url;
  HttpURLConnection connection;

  public RemoteTrackAccess() {
    baseUrl = "http://localhost:8080/api";
  }

  /**
   * Prepares a connection to the api.
   *
   * @param path the path (relative to baseUrl) for the endpoint you want to connect to
   * @param requestMethod the requestMethod for the connection (e.g. GET, POST, etc.)
   * @throws IOException if the url is wrongly formatted, the requestmetod is illegal or something
   *         went wrong while opening the connection
   */
  private void setConnection(String path, String requestMethod) throws IOException {
    try {
      url = new URL(baseUrl + path);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      connection.setRequestProperty("Content-Type", "application/json; utf-8");
      connection.setRequestProperty("Accept", "application/json");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
    } catch (MalformedURLException e) {
      throw new IOException("The endpoint " + baseUrl + path + " is not on the correct format", e);
    } catch (ProtocolException e) {
      throw new IOException("The requestmethod " + requestMethod + " is not allowed", e);
    } catch (IOException e) {
      throw new IOException("The program was unable to preparing a connection to the server", e);
    }

  }

  /**
   * Saves the track that the composer is currently holding.
   *
   * @param composer the composer holding the track you want to save
   * @throws IOException if something went wrong while saving the track
   */
  @Override
  public void saveTrack(Composer composer) throws IOException {
    setConnection("/tracks", "POST");
    connection.setDoOutput(true);

    try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
      final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
      composer.saveTrack(writer);
      connection.getResponseCode();
    } catch (IOException e) {
      throw new IOException("The program was unable to save the current track", e);
    }

    connection.disconnect();
  }

  /**
   * Loads the track with the given trackName to the composer.
   *
   * @param composer the composer you want to load the track to
   * @param id the id of the track you want to load
   * @throws IOException if something went wrong while loading the track
   */
  @Override
  public void loadTrack(Composer composer, int id) throws IOException {
    final String path = String.format("/tracks/%d", id);
    setConnection(path, "GET");

    try {
      final int status = connection.getResponseCode();
      DataInputStream in;

      if (status == 200) {
        in = new DataInputStream(connection.getInputStream());

        final InputStreamReader reader = new InputStreamReader(in, "UTF-8");
        composer.loadTrack(reader);

      } else {
        String errorBody = readResponse(status);

        throw new IOException(
            "Request to server gave unexpected status: %s body: %s".formatted(status, errorBody));
      }
    } catch (IOException e) {
      throw new IOException("The program was unable to load track with id " + id, e);
    }

    connection.disconnect();
  }

  /**
   * Loads saved tracks.
   *
   * @param trackName the trackName (or part of the name) you want the returned tracks to match
   * @param artistName the artistName (or part of the name) you want the returned tracks to match
   * @return a list of all maps where each map is a saved track that matches the given search quary
   * @throws IOException if something went wrong while loading the tracks
   */
  @Override
  public List<TrackSearchResult> loadTracks(String trackName, String artistName)
      throws IOException {
    final String path = String.format("/tracks?name=%s&artist=%s", trackName, artistName);
    setConnection(path, "GET");

    String responseString;
    try {
      final int status = connection.getResponseCode();
      responseString = readResponse(status);
    } catch (IOException e) {
      throw new IOException("The program was unable to load list of tracks", e);
    }

    connection.disconnect();

    TrackMapper tm = new TrackMapper();
    return tm.readFromString(responseString, new TypeReference<List<TrackSearchResult>>() {});
  }

  /**
   * Reads the response after a call to one of the endpoints of the api has been made.
   *
   * @param status the statusCode for the call made
   * @return a string with the response from the call
   * @throws IOException if the response can not be read
   */
  private String readResponse(int status) throws IOException {
    Reader inputStreamReader;
    if (status <= 299) {
      inputStreamReader = new InputStreamReader(connection.getInputStream(), "UTF-8");
    } else {
      try {
        inputStreamReader = new InputStreamReader(connection.getErrorStream(), "UTF-8");
      } catch (IOException e) {
        throw new IOException("Could not get inputstream from connection to server", e);
      }
    }

    StringBuilder content = new StringBuilder();
    try (BufferedReader in = new BufferedReader(inputStreamReader)) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
    } catch (IOException e) {
      throw new IOException("Something went wrong while reading response from server", e);
    }
    return content.toString();
  }
}
