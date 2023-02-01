package org.litepal.litepalsample;

import android.app.Application;
import org.litepal.litepalsample.room.MyRoom;

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        // LitePal.initialize(this);
        MyRoom.INSTANCE.init(this);
    }
}
