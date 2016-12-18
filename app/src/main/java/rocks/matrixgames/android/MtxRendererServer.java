package rocks.matrixgames.android;

import android.os.StrictMode;
import android.util.Log;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.InetSocketAddress;

import rocks.matrixgames.android.mtxRendererService.MtxRendererService;

/**
 * Created by jens on 30.11.16.
 */

public class MtxRendererServer extends Thread {

    private MtxRendererService.Processor processor;

    private TServerTransport transport;
    private TServer server;

    public MtxRendererServer(MtxRendererService.Iface handler, int port) throws NoServerException {
        processor = new MtxRendererService.Processor(handler);
        try {
            transport = new TServerSocket(new InetSocketAddress("0.0.0.0", port));
            TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport);
            args.processor(processor);
            server = new TSimpleServer(args);
        } catch (TTransportException transExc) {
            throw new NoServerException();
        }
    }

    @Override
    public void run() {
        Log.d(App.LOG_TAG, "starting Thread MtxRendererServer");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        server.serve();;
        transport.close();
        Log.d(App.LOG_TAG, "exiting Thread MtxRendererServer");
    }

    public void kill() {
        server.stop();
    }

    public class NoServerException extends Exception {

    }

}
