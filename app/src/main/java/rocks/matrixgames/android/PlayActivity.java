package rocks.matrixgames.android;

import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ToggleButton;

import org.apache.thrift.TException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import rocks.matrixgames.android.mtxRendererService.Direction;
import rocks.matrixgames.android.mtxRendererService.LevelInfo;
import rocks.matrixgames.android.mtxRendererService.MtxRendererService;
import rocks.matrixgames.android.mtxRendererService.Size;

public class PlayActivity extends AppCompatActivity implements MtxRendererService.Iface, View.OnTouchListener, View.OnClickListener {

    private final Handler mRefreshHandler = new Handler();

    private int port = 50506;

    private MtxRendererServer rendServer;

    private GameView gameView;
    private ToggleButton tglJump;

    private Field field;
    private boolean fieldRefresh = false;

    private Short rendererId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_play);
        gameView = (GameView) findViewById(R.id.game_view);
        tglJump = (ToggleButton) findViewById(R.id.tgl_jump);
        findViewById(R.id.btn_reset).setOnClickListener(this);

        gameView.setOnTouchListener(this);

        try {
            rendServer = new MtxRendererServer(this, port);
            rendServer.start();
        } catch (MtxRendererServer.NoServerException exc) {
            Log.e(App.LOG_TAG, "could not create renderer server", exc);
        }

        try {
            String ip = getIPAddress(true);
            Log.d(App.LOG_TAG, "request connect to " + ip);
            rendererId = App.getInstance().getClient().ConnectRenderer(ip, port);
        } catch (TException exc) {
            Log.e(App.LOG_TAG, "could not connect renderer", exc);
        }

        mRefreshHandler.removeCallbacks(mRefreshRunnable);
        mRefreshHandler.postDelayed(mRefreshRunnable, 200);
    }

    @Override
    protected void onDestroy() {
        if (rendererId != null) {
            try {
                App.getInstance().getClient().DisconnectRenderer(rendererId);
            } catch (TException exc) {
                Log.e(App.LOG_TAG, "could not connect renderer", exc);
            }
        }

        Log.d(App.LOG_TAG, "destroying");
        if (rendServer != null) {
            rendServer.kill();
            rendServer = null;
            Log.d(App.LOG_TAG, "rendServer set to null");
        }

        super.onDestroy();
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    private String getIPAddress(boolean useIPv4) {
        String result = "";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    Log.d(App.LOG_TAG, "Found address " + addr.getHostAddress());
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4) {
                                result = sAddr;
//                                return sAddr;
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return result;
    }

    private void pushLevel(List<List<List<List<Short>>>> field) {
        Field fld = new Field();

        short height = (short)field.size();
        short width = (short)(height == 0 ? 0 : field.get(0).size());

        fld.setSize(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                List<List<Short>> tmp = field.get(y).get(x);
                for (int o = 0; o < tmp.size(); o++) {
                    List<Short> rawContent = tmp.get(o);
                    fld.add((int) rawContent.get(0), (char) (rawContent.get(1).byteValue()), x, y);
                }
            }
        }
        gameView.setField(fld);
        this.field = fld;
        fieldRefresh = true;
    }


    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            if (fieldRefresh) {
                gameView.requestLayout();
                fieldRefresh = false;
            }
            gameView.invalidate();

            mRefreshHandler.removeCallbacks(mRefreshRunnable);
            mRefreshHandler.postDelayed(mRefreshRunnable, 25);
        }
    };


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(App.LOG_TAG, "Spielfeld " + view.getWidth() + ", " + view.getHeight());

        float start = view.getWidth() * 0.2f;
        float end = view.getHeight() * 0.8f;

        Log.d(App.LOG_TAG, "Bereich " + start + ", " + end);

        Log.e(App.LOG_TAG, "onTouch " + motionEvent.getX() + ", " + motionEvent.getY());
        try {
            if (motionEvent.getY() > end) {
                if (tglJump.isChecked()) {
                    App.getInstance().getClient().JumpPlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.DOWN);
                } else {
                    App.getInstance().getClient().MovePlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.DOWN);
                }
            }
            if (motionEvent.getX() < start) {
                if (tglJump.isChecked()) {
                    App.getInstance().getClient().JumpPlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.LEFT);
                } else {
                    App.getInstance().getClient().MovePlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.LEFT);
                }
            }
            if (motionEvent.getX() > end) {
                if (tglJump.isChecked()) {
                    App.getInstance().getClient().JumpPlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.RIGHT);
                } else {
                    App.getInstance().getClient().MovePlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.RIGHT);
                }
            }
            if (motionEvent.getY() < start) {
                if (tglJump.isChecked()) {
                    App.getInstance().getClient().JumpPlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.UP);
                } else {
                    App.getInstance().getClient().MovePlayer((byte) 1, rocks.matrixgames.android.mtxControllerService.Direction.UP);
                }
            }
        } catch (TException exc) {
            Log.e(App.LOG_TAG, "Whaaa", exc);
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.btn_reset) {
                App.getInstance().getClient().ResetLevel();
            }
        } catch (TException exc) {
            Log.e(App.LOG_TAG, "Error", exc);
        }

    }

    @Override
    public void Ping() throws TException {
        Log.d(App.LOG_TAG, "Ping");
    }

    @Override
    public void Shutdown() throws TException {
        Log.d(App.LOG_TAG, "Shutdown");
    }

    @Override
    public void Freeze() throws TException {
        Log.d(App.LOG_TAG, "Freeze");
    }

    @Override
    public void Thaw() throws TException {
        Log.d(App.LOG_TAG, "Thaw");
    }

    @Override
    public void Pause() throws TException {
        Log.d(App.LOG_TAG, "Pause");
    }

    @Override
    public void Resume() throws TException {
        Log.d(App.LOG_TAG, "Resume");
    }

    @Override
    public void Clear() throws TException {
        Log.d(App.LOG_TAG, "Clear");
    }

    @Override
    public Size GetPreferedFieldSize() throws TException {
        Log.d(App.LOG_TAG, "GetPreferedFieldSize");
        return new Size((byte)0, (byte)0);
    }

    @Override
    public void LoadLevel(List<List<List<List<Short>>>> field, LevelInfo levelInfo) throws TException {
        Log.d(App.LOG_TAG, "LoadLevel: " + field.toArray());
        pushLevel(field);
    }

    @Override
    public void ResetLevel(List<List<List<List<Short>>>> field) throws TException {
        Log.d(App.LOG_TAG, "ResetLevel");
        pushLevel(field);
    }

    @Override
    public void Spawn(short objId, byte symbol, short positionX, short positionY) throws TException {
        Log.d(App.LOG_TAG, "Spawn");

    }

    @Override
    public void Remove(short objectId, short sourceId) throws TException {
        Log.d(App.LOG_TAG, "Remove");
        field.remove(objectId);
    }

    @Override
    public void Collect(short objectId, short sourceId) throws TException {
        Log.d(App.LOG_TAG, "Collect");
        field.remove(objectId);
    }

    @Override
    public void TriggerEnter(short objectId, short sourceId) throws TException {
        Log.d(App.LOG_TAG, "TriggerEnter");
    }

    @Override
    public void TriggerLeave(short objectId, short sourceId) throws TException {
        Log.d(App.LOG_TAG, "TriggerLeave");
    }

    @Override
    public void Move(short objectId, Direction direction, short fromX, short fromY, short toX, short toY) throws TException {
        Log.d(App.LOG_TAG, String.format("Move %d: %d - %d,%d -> %d,%d", objectId, direction.getValue(), fromX, fromY, toX, toY));
        field.move(objectId, 0, 0, toX, toY);
    }

    @Override
    public void Jump(short objectId, Direction direction, short fromX, short fromY, short toX, short toY) throws TException {
        Log.d(App.LOG_TAG, "Jump");
        field.jump(objectId, 0, 0, toX, toY);
    }
}
