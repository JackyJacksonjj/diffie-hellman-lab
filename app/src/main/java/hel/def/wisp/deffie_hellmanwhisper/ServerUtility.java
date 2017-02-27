package hel.def.wisp.deffie_hellmanwhisper;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by jenea on 2/10/17.
 */
public class ServerUtility {

    // DESIGNATE A PORT
    public static final int SERVERPORT = 8080;
    private static final String TAG = ServerUtility.class.getSimpleName();
    // DEFAULT IP
    public static String SERVERIP = "0.0.0.0";
    private Handler handler = new Handler();

    private ServerSocket serverSocket;

    private TextView serverStatus;
    private Context context;

    private volatile String msg;
    private volatile boolean send;

    public ServerUtility(TextView serverStatus, Context context) {
        SERVERIP = getLocalIpAddress();
        this.context = context;
        this.serverStatus = serverStatus;
        Thread fst = new Thread(new ServerThread());
        fst.start();
    }

    public void sendMessage(String msg) {
        this.msg = msg;
        send = true;
    }

    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    public void onStop() {
        try {
            // MAKE SURE YOU CLOSE THE SOCKET UPON EXITING
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + "Listening on IP: " + SERVERIP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        Socket client = serverSocket.accept();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: Connected.");
                                serverStatus.setText("Connected");
                            }
                        });

                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//                            PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                            String line = null;
                            while ((line = in.readLine()) != null) {
                                Log.d("ServerActivity", String.format("incoming message = %s", line));
                                final String line2 = line;

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        if(send) {
                                            serverStatus.setText(serverStatus.getText().toString() + " : " + line2);
                                        }
                                        // DO WHATEVER YOU WANT TO THE FRONT END
                                        // THIS IS WHERE YOU CAN BE CREATIVE
                                        serverStatus.setText(line2);
                                    }
                                });

//                                if(send) {
//                                    send = false;
//                                    out.println(msg);
//                                    msg = null;
//                                    Log.d(TAG, "run: " + msg);
//                                }
                            }
                            break;
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "run: Oops. Connection interrupted. Please reconnect your phones.");
                                    serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: Couldn't detect internet connection.");
                            serverStatus.setText("Couldn't detect internet connection.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: Error");
                        serverStatus.setText("Error");
                    }
                });
                e.printStackTrace();
            }
        }
    }

}

