package com.example.mena.joystick;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static android.view.View.OnTouchListener;


public class MainActivity extends Activity implements OnTouchListener {

    DrawJoyStick joystick;
    Bitmap ball, pad;
    Paint p1, p2;
    float x_leftStick, y_leftStick, x_rightStick, y_rightStick;
    float xzero_left, yzero_left, xzero_right, yzero_right, xzero, yzero;
    float theta;
    float dx, dy;
    float r;
    float dr;
    int pointerId;
    Context context;
    boolean touchstate = false;
    boolean firsttouch= false;
    float cx_leftPad, cx_rightPad, cy_leftPad, cy_rightPad;
    float cx_leftBall, cx_rightBall, cy_leftBall, cy_rightBall;
    public static final int SERVERPORT = 5000;
    static String ipaddr ="";
    String message ="";
    InetAddress serverAddr;
    DatagramSocket datagramSocket;
    LinearLayout dataArea,joyStickArea, dPadArea;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.this;
        joystick = new DrawJoyStick(this);
        joystick.setOnTouchListener(this);
        ball = BitmapFactory.decodeResource(getResources(),R.drawable.joystick_thumb);
        pad = BitmapFactory.decodeResource(getResources(),R.drawable.joystick_background);
        x_leftStick = 0; y_leftStick = 0;
        x_rightStick = 0; y_rightStick = 0;
        dx = 0; dy = 0;dr = 0;
        pointerId = 0;
        theta = 0;
        xzero = 0; yzero = 0;
        p1 = new Paint();
        p1.setTextSize(20);
        p1.setColor(Color.rgb(255,0,0));
        p2 = new Paint();
        p2.setColor(Color.rgb(36,107,36));

        // Combine Layouts
        setContentView(R.layout.joy_layout);
        LinearLayout dataArea = (LinearLayout)findViewById(R.id.data_area);     // To display the data buttons
        LinearLayout joyStickArea = (LinearLayout)findViewById(R.id.joystick_area);
        joyStickArea.addView(joystick);   // To display the joystick
        LinearLayout dPadArea = (LinearLayout)findViewById(R.id.dpad_area);    // To display dPad


        // Define D-pad buttons
        ImageButton b_down = (ImageButton) findViewById(R.id.button_down);
        ImageButton b_up = (ImageButton) findViewById(R.id.button_up);
        ImageButton b_left = (ImageButton) findViewById(R.id.button_left);
        ImageButton b_right = (ImageButton) findViewById(R.id.button_right);


        // D-pad button on-click listeners
        b_down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);

            }
        });
        b_up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        b_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        b_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });

        //Define other data buttons
        Button dash = (Button) findViewById(R.id.dbutton_0);
        Button grab = (Button) findViewById(R.id.dbutton_1);
        Button test = (Button) findViewById(R.id.dbutton_2);
        Button gps = (Button) findViewById(R.id.dbutton_3);
        Button cam = (Button) findViewById(R.id.dbutton_4);

        //Set on-click listeners for data buttons
        dash.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
                Intent dashScreen = new Intent(v.getContext(), Dashboard.class);
                startActivity(dashScreen);

            }
        });
        grab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        gps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        cam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                v.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });


        //Get ip-address to set-up connection
        final AlertDialog.Builder alert = new AlertDialog.Builder(
                context);
        alert.setTitle("Connection Set-Up");
        alert.setMessage("Enter IP Address:");
        // Setting an EditText view to get user input
        final EditText input = new EditText(context);
        alert.setView(input);
        alert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        ipaddr = input.getText().toString();
                    }


                });
        AlertDialog build = alert.create();
        build.show();


    }

    @Override
    protected void onPause() {
        //TODO
        super.onPause();
        joystick.pause();

    }

    @Override
    protected void onResume() {
        //TODO
        super.onResume();
        joystick.resume();

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

         x_leftStick = event.getX();
         y_leftStick = event.getY();

        switch (maskedAction) {

                case MotionEvent.ACTION_DOWN:

                    dx = x_leftStick - xzero_left;
                    dy = y_leftStick - yzero_left;
                    dr = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dr > r) {

                        x_leftStick = 0;
                        y_leftStick = 0;
                        dx = 0;
                        dy = 0;
                        touchstate = true;

                    } else {
                        firsttouch = true;
                        touchstate = false;
                    }
                        break;

                case MotionEvent.ACTION_POINTER_DOWN:

                    if(firsttouch == true) {
                           dx = x_leftStick - xzero_left;
                           dy = y_leftStick - yzero_left;
                           dr = (float) Math.sqrt(dx * dx + dy * dy);

                            if (dr > r) {
                                theta = (float) Math.atan2(dy, dx);
                                x_leftStick = xzero_left + (r * (float) Math.cos(theta));
                                y_leftStick = yzero_left + (r * (float) Math.sin(theta));
                                dx = 0;
                                dy = 0;
                            }
                        }
                    else{
                        firsttouch = false;
                        x_leftStick = 0;
                        y_leftStick = 0;
                        dx = 0;
                        dy = 0;

                    }

                    break;

                case MotionEvent.ACTION_UP:
                        dx = 0;
                        dy = 0;
                        x_leftStick = 0;
                        y_leftStick = 0;
                        firsttouch = false;
                      break;

                case MotionEvent.ACTION_POINTER_UP:

                    if(firsttouch == true) {
                        dx = x_leftStick - xzero_left;
                        dy = y_leftStick - yzero_left;
                        dr = (float) Math.sqrt(dx * dx + dy * dy);

                        if (dr > r) {
                            theta = (float) Math.atan2(dy, dx);
                            x_leftStick = xzero_left + (r * (float) Math.cos(theta));
                            y_leftStick = yzero_left + (r * (float) Math.sin(theta));
                            dx = 0;
                            dy = 0;

                        }
                    }
                    else{
                        x_leftStick = 0;
                        y_leftStick = 0;
                        dx = 0;
                        dy = 0;
                    }

                    break;

                case MotionEvent.ACTION_MOVE:

                    if(pointerId == 0) {
                        calculateValues(x_leftStick, y_leftStick);
                    }
                    else
                        calculateValues(x_leftStick, y_leftStick);
                    break;

                case MotionEvent.ACTION_CANCEL:

                    break;

            }

            return true;
        }

    private void calculateValues(float x_in, float y_in) {

        if (touchstate == false || firsttouch == true ) {
            dx = x_in - xzero_left;
            dy = y_in - yzero_left;
            theta = (float) Math.atan2(dy, dx);
            dr = (float) Math.sqrt(dx * dx + dy * dy);

            if (dr > r) {

                x_in = xzero_left + (r * (float) Math.cos(theta));
                y_in = yzero_left + (r * (float) Math.sin(theta));

            } else {

                x_in = xzero_left + dx;
                y_in = yzero_left + dy;
            }


            x_leftStick = x_in;
            y_leftStick = y_in;
            dx = 0;
            dy = 0;

        }

        if (touchstate == true || firsttouch == false){

            x_leftStick = 0;
            y_leftStick = 0;
            dx = 0;
            dy = 0;

        }

    }

    public class DrawJoyStick extends SurfaceView implements Runnable {

        Thread t = null;
        boolean runflag = false;
        SurfaceHolder holder;

       public DrawJoyStick(Context context) {
           super(context);
           holder = getHolder();

       }


       @Override
        public void run() {
            while (runflag == true){
                if (!holder.getSurface().isValid()){
                    continue;
                }

                // Otherwise proceed to create Canvas
                Canvas c = holder.lockCanvas();
                c.drawARGB(255,51,102,153);

                float width = c.getWidth();
                float height = c.getHeight();

                //Ball cx, cy
                //cx_rightBall = c.getWidth() - 150 - ball.getWidth();
                //cy_rightBall = c.getHeight() - 300;
                cx_leftBall = 150;
                cy_leftBall = c.getHeight() - 300;

                //x and y center points LEFT
                xzero_left = cx_leftBall + ball.getWidth()/2;
                yzero_left = cy_leftBall + ball.getHeight()/2;

                //x and y center points RIGHT
               // xzero_right = cx_rightBall + ball.getWidth()/2;
              //  yzero_right = cy_rightBall + ball.getHeight()/2;

                //Pad cx,cy,r
                cx_leftPad = xzero_left;
                cy_leftPad = yzero_left;
               // cx_rightPad = cx_rightBall + ball.getWidth()/2;
              //  cy_rightPad = cy_rightBall + ball.getHeight()/2;
                r = 150;

                //Draw outer Circle
                c.drawCircle(cx_leftPad,cy_leftPad,r,p2);
              //c.drawCircle(cx_rightPad,cy_rightPad,r,p2);

                //Draw inner Circles
                if (x_leftStick == 0 && y_leftStick == 0){
                c.drawBitmap(ball,cx_leftBall,cy_leftBall,null);
                }
                else{
                c.drawBitmap(ball,x_leftStick-ball.getWidth()/2,y_leftStick-ball.getWidth()/2,null);
                }

               // c.drawBitmap(ball,cx_rightBall,cy_rightBall,null);
                float xdisplayMax = xzero_left + r;
                float xdisplayMin = xzero_left - r;
                float ydisplayMin = yzero_left + r;
                float ydisplayMax = yzero_left - r;
                float NewMax = 32768;
                float NewMin = -32768;
                float NewRange = (NewMax - NewMin);
                float NewXValue = 0;
                float NewYValue = 0;

                if(x_leftStick == 0 && y_leftStick == 0){
                    NewXValue = 0;
                    NewYValue = 0;
                }
                else {
                    // Convert x value to new scaling
                    float OldXRange = (xdisplayMax - xdisplayMin);
                    NewXValue = (((x_leftStick - xdisplayMin) * NewRange) / OldXRange) + NewMin;

                    // Convert y value to new scaling
                    float OldYRange = (ydisplayMax - ydisplayMin);
                    NewYValue = (((y_leftStick - ydisplayMin) * NewRange) / OldYRange) + NewMin;

                }

                message = ""+ NewXValue +"," + NewYValue;
               // Log.v("ME","message= "+ message);

                //ipaddr = "192.168.1.204";
              //  ipaddr = "192.168.1.89";

                try {
                    datagramSocket = new DatagramSocket(SERVERPORT);
                    serverAddr = InetAddress.getByName(ipaddr);
                    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(),serverAddr,SERVERPORT);
                    datagramSocket.send(packet);
                    datagramSocket.close();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                holder.unlockCanvasAndPost(c);

            }
        }

        public void pause(){

            runflag = false;
            while(true){
                try{
                    t.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                break;
            }
            t = null;
        }

        public void resume(){
            runflag = true;
            t = new Thread(this);
            t.start();
        }

    }

}


