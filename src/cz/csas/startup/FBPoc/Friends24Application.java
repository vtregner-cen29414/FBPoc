package cz.csas.startup.FBPoc;

import android.app.Application;
import com.facebook.model.GraphUser;

import java.util.List;

/**
 * Created by cen29414 on 18.4.2014.
 */
public class Friends24Application extends Application {
    private List<GraphUser> selectedFrieds;

    public List<GraphUser> getSelectedFrieds() {
        return selectedFrieds;
    }

    public void setSelectedFrieds(List<GraphUser> selectedFrieds) {
        this.selectedFrieds = selectedFrieds;
    }
}
