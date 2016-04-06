package mset649.shs.com.visioncontrol;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class VisionServerActivity extends AppCompatActivity {
    protected static final String mTAG = "VisionServer";
    protected static final int SERVERPORT = 5000;
    protected String mServerIp;
    private Handler mHandler = new Handler();
    private ServerSocket mServerSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_server);

        /*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //startIpWebcam();
            }
        }, 5000);
        */

        mServerIp = getLocalIpAddress();
        Log.d(mTAG, "Server IP " + mServerIp);

        Thread fst = new Thread(new ServerThread());
        fst.start();
    }

    private void switchToIpWebcam() {
        /*
        Intent intent = new Intent(this, StartWorkingActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        */
    }

    private void switchToVision() {
        Log.d(mTAG, "Bringing Application to Front");

        Intent notificationIntent = new Intent(this, VisionServerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


    private void startIpWebcam() {
        Log.d(mTAG, "Bringing IpWebcam to Front");
        Intent launcher = new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        Intent ipwebcam =
                new Intent()
                        .setClassName("com.pas.webcam", "com.pas.webcam.Rolling")
                        .putExtra("cheats", new String[] {
                                "set(Video,320,240)",
                                "reset(Photo)",
                                "set(Awake,true)",
                                "reset(Port)",                 // Use default port 8080
                        })
                        .putExtra("hidebtn1", true)                // Hide help button
                        .putExtra("caption2", "Run in background") // Change caption on "Actions..."
                        .putExtra("intent2", launcher)             // And give button another purpose
                        .putExtra("returnto", new Intent().setClassName(VisionServerActivity.this, VisionServerActivity.class.getName())); // Set activity to return to
        startActivity(ipwebcam);
    }


    // Gets the ip address of phone's network
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (mServerIp != null) {
                    Log.d(mTAG, "Listening on IP: " + mServerIp);

                    mServerSocket = new ServerSocket(SERVERPORT);
                    mServerSocket.setReuseAddress(true);
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        Socket client = mServerSocket.accept();
                        Log.d(mTAG, "Connected.");

                        try {
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(client.getInputStream()));
                            String command = null;
                            while ((command = in.readLine()) != null) {
                                Log.d("ServerActivity", command);
                                mHandler.post(new ProcessCommandRunnable(command));
                            }
                        } catch (Exception e) {
                            Log.d(mTAG, "Oops. Connection interrupted. Please reconnect your phones.");
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(mTAG, "Couldn't detect internet connection.");
                }

            } catch (final Exception e) {
                Log.d(mTAG, "Error" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void processCommand(String command) {
        if (command.equals("webcam")) {
            startIpWebcam();
        } else if (command.equals("vision")) {
            switchToVision();
        } else {
            Log.e(mTAG, "Unknown command" + command);
        }
    }


    private class ProcessCommandRunnable implements Runnable {
        private final String mCommand;

        ProcessCommandRunnable(final String command) {
            mCommand = command;
        }

        public void run() {
            System.out.println(mCommand);
            if (mCommand.equals("webcam")) {
                startIpWebcam();
            } else if (mCommand.equals("vision")) {
                switchToVision();
            } else {
                Log.e(mTAG, "Unknown command" + mCommand);
            }
        }
    }
}
