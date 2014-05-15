package cz.csas.startup.FBPoc.service;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: cen29414
 * Date: 22.8.13
 * Time: 7:50
 * To change this template use File | Settings | File Templates.
 */
public interface JsonResponseParser<RES> {
    public RES parseResponseObject(JSONObject object) throws JSONException;
}
