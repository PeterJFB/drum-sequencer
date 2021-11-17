package sequencer.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sequencer.core.Track;

/**
 * The {@code TrackDeserializer} is a custom deserializer of the {@link Track} class.
 */
class TrackDeserializer extends JsonDeserializer<Track> {

  @Override
  public Track deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    TreeNode treeNode = parser.getCodec().readTree(parser);

    return deserialize(treeNode);
  }

  Track deserialize(TreeNode treeNode) {
    if (treeNode instanceof ObjectNode) {
      ObjectNode objectNode = (ObjectNode) treeNode;

      Track track = new Track();

      // Get track meta
      JsonNode nameNode = objectNode.get("name");
      if (nameNode instanceof TextNode) {
        track.setTrackName(nameNode.asText());
      }

      JsonNode artistNode = objectNode.get("artist");
      if (artistNode instanceof TextNode) {
        track.setArtistName(artistNode.asText());
      }

      // Get instruments and patterns
      JsonNode itemsNode = objectNode.get("instruments");
      if (itemsNode instanceof ObjectNode) {
        ObjectNode instrumentPatternObject = (ObjectNode) itemsNode;

        // Get pattern
        instrumentPatternObject.fieldNames().forEachRemaining((instrument) -> {

          List<Boolean> pattern = new ArrayList<>();
          JsonNode patternNode = instrumentPatternObject.get(instrument);

          if (patternNode instanceof ArrayNode) {
            for (JsonNode valueNode : patternNode) {

              if (valueNode instanceof BooleanNode) {
                pattern.add(valueNode.asBoolean());
              } else {
                pattern.add(false);
              }
            }
          }

          track.addInstrument(instrument, pattern);
        });

      }
      return track;
    }

    return null;
  }
}
