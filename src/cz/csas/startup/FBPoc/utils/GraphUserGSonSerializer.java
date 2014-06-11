package cz.csas.startup.FBPoc.utils;

import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.google.gson.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by cen29414 on 11.6.2014.
 */
public class GraphUserGSonSerializer implements JsonSerializer<GraphUser>, JsonDeserializer<GraphUser> {

    @Override
    public JsonElement serialize(GraphUser user, Type type, JsonSerializationContext jsonSerializationContext) {

        if (user == null) {
            return JsonNull.INSTANCE;
        }
        else {
            return new JsonPrimitive(user.getInnerJSONObject().toString());
        }
    }

    @Override
    public GraphUser deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull()) return null;
        else {
            String asString = jsonElement.getAsString();
            try {
                return GraphObject.Factory.create(new JSONObject(asString), GraphUser.class);
            } catch (JSONException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
