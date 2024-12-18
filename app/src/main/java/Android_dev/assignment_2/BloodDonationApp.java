package Android_dev.assignment_2;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class BloodDonationApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}