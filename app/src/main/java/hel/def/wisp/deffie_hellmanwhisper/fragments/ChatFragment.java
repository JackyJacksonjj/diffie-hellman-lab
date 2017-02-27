package hel.def.wisp.deffie_hellmanwhisper.fragments;

import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

import hel.def.wisp.deffie_hellmanwhisper.R;
import hel.def.wisp.deffie_hellmanwhisper.ServerActivity;
import hel.def.wisp.deffie_hellmanwhisper.ServerUtility;
import hel.def.wisp.deffie_hellmanwhisper.adapters.P2pArrayAdapter;

/**
 * Created by jenea on 2/24/17.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = ChatFragment.class.getSimpleName();

    private ClientThread socketCore;
    private ArrayList<WifiP2pDevice> list = new ArrayList<>();
    private P2pArrayAdapter adapter;
    private volatile ServerUtility serverUtility;
    private String serverIpAddress = "";
    private EditText serverIp;
    private TextView serverStatus;
    private Button connectPhones;
    private boolean connected = false;
    private volatile boolean send;
    private View.OnClickListener connectListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!connected) {
                serverIpAddress = serverIp.getText().toString();
                if (!TextUtils.isEmpty(serverIpAddress)) {
                    if (!connected) {
                        socketCore = new ClientThread();
                        Thread cThread = new Thread(socketCore);
                        cThread.start();
                        connectPhones.setText("disconnect");
                        serverIp.setText("");
                        serverIp.setHint("Message ...");
                    } else {
                        if (socketCore.disconnect()) {
                            connectPhones.setText("connect to ip");
                            serverIp.setText(serverIpAddress);
                            serverIp.setHint("Server IP");
                        }
                    }
                }
            }
        }
    };

    public static void createKey() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");

        DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp.getPublic(),
                DHPublicKeySpec.class);
    }

    public static void createSpecificKey(BigInteger p, BigInteger g) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

        DHParameterSpec param = new DHParameterSpec(p, g);
        kpg.initialize(param);
        KeyPair kp = kpg.generateKeyPair();

        KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");

        DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp.getPublic(),
                DHPublicKeySpec.class);
    }

    public void calculate(int pValue, int gValue, int XaValue, int XbValue) {
        BigInteger p = new BigInteger(Integer.toString(pValue));
        BigInteger g = new BigInteger(Integer.toString(gValue));
        BigInteger Xa = new BigInteger(Integer.toString(XaValue));
        BigInteger Xb = new BigInteger(Integer.toString(XbValue));

        try {
            createKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int bitLength = 512; // 512 bits
        SecureRandom rnd = new SecureRandom();
        p = BigInteger.probablePrime(bitLength, rnd);
        g = BigInteger.probablePrime(bitLength, rnd);

        try {
            createSpecificKey(p, g);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment, null);
        serverIp = (EditText) view.findViewById(R.id.server_ip);
        serverStatus = (TextView) view.findViewById(R.id.server_status);
        connectPhones = (Button) view.findViewById(R.id.connect_to_ip);
        connectPhones.setOnClickListener(connectListener);
        view.findViewById(R.id.listen_to_ip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverUtility == null) {
                    serverUtility = new ServerUtility(serverStatus, getContext());
                } else {
                    serverUtility.onStop();
                    serverUtility = null;
                }
            }
        });
        adapter = new P2pArrayAdapter(getContext(), R.layout.p2p_info_adapter_item, list);
        ListView p2pDevsListView = (ListView) view.findViewById(R.id.p2p_list_view);
        p2pDevsListView.setAdapter(adapter);
        p2pDevsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = list.get(position).deviceAddress;
            }
        });

        view.findViewById(R.id.send_message).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_message:
                if (!TextUtils.isEmpty(serverIp.getText().toString())) {
                    if(serverUtility != null)
                        Toast.makeText(getContext(), "SerUtil != nul", Toast.LENGTH_SHORT).show();
                    if(socketCore == null && serverUtility == null){
                        Toast.makeText(getContext(), "No connection", Toast.LENGTH_SHORT).show();
                    } else {
                        if(socketCore != null)
                            socketCore.sendMessage(serverIp.getText().toString());
                        else
                            serverUtility.sendMessage(serverIp.getText().toString());
                    }
                } else {
                    Toast.makeText(getContext(), "Empty message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class ClientThread implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        public void sendMessage(String msg) {
            send = true;
            Toast.makeText(getContext(), "SocketCore = msg sent", Toast.LENGTH_SHORT).show();
//            out.write(msg);
//            out.flush();
        }

        public boolean disconnect() {
            try {
                socket.close();
                connected = false;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        public OutputStream getOutputStream() {
            try {
                return socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "getOutputStream: Seems like socket is not connected!", e);
                e.printStackTrace();
            }
            return null;
        }

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "C: Connecting...");
                socket = new Socket(serverAddr, ServerActivity.SERVERPORT);
                Log.d("ClientActivity", "C: Connected");
                connected = true;

                if (in == null) {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                }

                if (out == null) {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                            .getOutputStream())), true);
                }
                while (connected) {
                    try {
                        // WHERE YOU ISSUE THE COMMANDS
                        if(send) {
                            send = false;
                            out.println(serverIp.getText().toString());
                            Log.d(TAG, serverIp.getText().toString());
                        }

                        if(in.readLine() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        serverStatus.setText(in.readLine());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
//                        Log.d("ClientActivity", "C: Sent.");
                    } catch (Exception e) {
                        Log.e("ClientActivity", "S: Error", e);
                    }
                }
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
                connected = false;
            }
        }
    }
}
