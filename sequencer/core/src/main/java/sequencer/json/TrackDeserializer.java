package sequencer.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import sequencer.core.Track;

class TrackDeserializer extends JsonDeserializer<Track> {

    @Override
    public Track deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        TreeNode treeNode = parser.getCodec().readTree(parser);

        return deserialize(treeNode);
    }

    Track deserialize(TreeNode treeNode) {
        if (treeNode instanceof ObjectNode objectNode) {

            Track track = new Track();

            // Get track meta
            JsonNode nameNode = objectNode.get("name");
            if (nameNode instanceof TextNode nameTextNode) {
                track.setTrackName(nameTextNode.asText());
            }

            JsonNode artistNode = objectNode.get("artist");
            if (artistNode instanceof TextNode artistTextNode) {
                track.setArtistName(artistTextNode.asText());
            }

            // Get instruments and patterns
            JsonNode itemsNode = objectNode.get("instruments");
            if (itemsNode instanceof ObjectNode instrumentPatternObject) {

                // Get pattern
                instrumentPatternObject.fieldNames().forEachRemaining((instrument) -> {

                    List<Boolean> pattern = new ArrayList<>();
                    JsonNode patternNode = instrumentPatternObject.get(instrument);

                    if (patternNode instanceof ArrayNode patternArrayNode) {
                        for (JsonNode valueNode : patternArrayNode) {

                            if (valueNode instanceof BooleanNode value) {
                                pattern.add(value.asBoolean());
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
