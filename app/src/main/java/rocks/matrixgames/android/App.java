package rocks.matrixgames.android;

import android.app.Application;
import android.util.Log;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;

import rocks.matrixgames.android.mtxControllerService.MtxControllerService;

/**
 * Created by jens on 30.11.16.
 */

public class App extends Application {

    public static final String LOG_TAG = "mtx";

    private static App sInstance;

    private MtxControllerService.Client mtxCtrlClient;
    private TSocket transport;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        Log.d(LOG_TAG, "onCreate Application");

        //mSettings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static App getInstance() {
        return sInstance;
    }

    public boolean isConnected() {
        return mtxCtrlClient != null;
    }

    public boolean connect(String host, int port) {
        transport = new TSocket(host, port);
        Log.d(LOG_TAG, "transport created");
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Log.d(LOG_TAG, "protocol created");

        mtxCtrlClient = new MtxControllerService.Client(protocol);
        Log.d(LOG_TAG, "client created");

        try {
            transport.open();
            return true;
        } catch (TException exc) {
            Log.e(LOG_TAG, "Error trying to connect", exc);
            clearConnection();
        }
        return false;
    }

    public MtxControllerService.Iface getClient() {
        return mtxCtrlClient;
    }

    public void clearConnection() {
        transport.close();
        transport = null;
        mtxCtrlClient = null;
    }

}
