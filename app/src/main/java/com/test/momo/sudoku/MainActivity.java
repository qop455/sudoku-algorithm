package com.test.momo.sudoku;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static int isQ=1;
    private final static int REFRESH=1;
    private final static int DONE=2;
    private final static int THREADDONE =3;
    private static String Q1 =
            "3 0 1 0 6 0 4 0 0 " +
                    "0 4 0 0 0 0 0 1 0 " +
                    "9 0 8 1 0 7 3 0 2 " +
                    "0 0 3 0 0 9 2 0 0 " +
                    "6 0 0 0 2 0 0 0 4 " +
                    "0 0 5 8 0 0 7 0 0 " +
                    "8 0 7 6 0 2 5 0 1 " +
                    "0 1 0 0 0 0 0 7 0 " +
                    "0 0 4 0 9 0 6 0 8";
    private static String Q2 =
            "0 0 1 0 0 7 5 0 0 " +
                    "0 2 0 0 8 0 0 1 0 " +
                    "3 0 0 5 0 0 0 0 6 " +
                    "4 0 0 2 0 9 7 0 0 " +
                    "0 5 0 0 0 0 0 3 0 " +
                    "0 0 6 7 0 1 0 0 9 " +
                    "9 0 0 0 0 4 0 0 5 " +
                    "0 7 0 0 6 0 0 8 0 " +
                    "0 0 4 3 0 0 2 0 0";
    private TextView showTime;
    private TextView showThreadTime;
    private TextView sudokuView;
    private Button startBtn;
    private Button startThrBtn;
    private static int[][] mySudoku=new int[81][9];
    private static String[] myStringSudoku = new String[81];
    private static String sudokuTable;
    private static boolean myContinue=true;
    private Thread thread=null;
    private Thread threadPartA=null;
    private Thread threadPartB=null;
    private static long myTime;
    private static int clickTime=0;
    private static long totalTime=0;
    private static int clickThreadTime=0;
    private static long totalThreadTime =0;

    private void initLayout(){
        setContentView(R.layout.activity_main);

        /************************************************/
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (isQ){
                    case 1:
                        isQ=2;
                        selectQ(Q2);
                        break;
                    case 2:
                        isQ=1;
                        selectQ(Q1);
                        break;
                }
                clickTime=0;
                clickThreadTime=0;
                totalTime=0;
                totalThreadTime=0;
                initSudoku();
                refreshTable();
            }
        });
        /************************************************/

        showTime = (TextView)findViewById(R.id.show_time);
        showThreadTime = (TextView)findViewById(R.id.show_init_time);
        sudokuView = (TextView)findViewById(R.id.sudoku_view);
        startBtn = (Button)findViewById(R.id.start_btn);
        startThrBtn = (Button)findViewById(R.id.start_thread_btn);
        for (int i=0;i<myStringSudoku.length;i++){
        }
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickTime++;
                initSudoku();
                myTime=System.currentTimeMillis();
                while(myContinue){
                    goThrough();
                    goEachCheck();
                    goLineCheck();
                }
                long t=System.currentTimeMillis()-myTime;
                Bundle timeBundle=new Bundle();
                timeBundle.putLong("TIME", t);
                Message msg = new Message();
                msg.what = DONE;
                msg.setData(timeBundle);
                myHandler.sendMessage(msg);
                myContinue=true;
                refreshTable();
            }
        });
        startThrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickThreadTime++;
                initSudoku();
                myTime=System.currentTimeMillis();
                threadPartA=new myThreadPartA();
                threadPartB=new myThreadPartB();
                threadPartA.start();
                threadPartB.start();
            }
        });
        refreshTable();
    }

    private void refreshTable(){
        Message msg = new Message();
        msg.what = REFRESH;
        myHandler.sendMessage(msg);
    }

    private void selectQ(String Q){
        sudokuTable = Q;
    }

    private void initSudoku(){
        if (thread!=null){
            try{
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread=null;
        }
        if (threadPartA!=null){
            try{
                threadPartA.interrupt();
                threadPartA.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadPartA=null;
        }
        if (threadPartB!=null){
            try{
                threadPartB.interrupt();
                threadPartB.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadPartB=null;
        }
        myContinue=true;
        myStringSudoku = sudokuTable.split("\\s+");

        int temp;
        for (int i=0;i< myStringSudoku.length;i++){
            temp=Integer.valueOf(myStringSudoku[i]).intValue();
            if(temp>0){
                for (int j=0;j<9;j++){
                    if (temp-1==j){
                        mySudoku[i][j]=1;
                    }else{
                        mySudoku[i][j]=0;
                    }
                }
            }else{
                for (int k=0;k<9;k++){
                    mySudoku[i][k]=1;
                }
            }
        }
    }

    private void goThrough(){
        int rowStart;
        int colStart;
        int bloStart;
        for (int k=0;k<81;k++){
            if (!myStringSudoku[k].equals("0")){
                int value=Integer.parseInt(myStringSudoku[k]);
                rowStart=(k/9)*9;
                colStart=k%9;
                bloStart=(rowStart/27)*27+(colStart/3)*3;
                if(value<1||value>9){
                    Log.d("GoThrough:","Error Value:"+value);
                    continue;
                }
                /***row***/
                for(int ka=rowStart;ka<rowStart+9;ka++){
                    if (ka!=k&&myStringSudoku[ka].equals("0")){
                        mySudoku[ka][value-1]=0;
                    }
                }
                /***column***/
                for(int kb=colStart;kb<=colStart+72;kb+=9){
                    if (kb!=k&&myStringSudoku[kb].equals("0")){
                        mySudoku[kb][value-1]=0;
                    }
                }
                /***block***/
                for(int kc=bloStart;kc<bloStart+3;kc++){
                    for (int kci=kc;kci<=kc+18;kci+=9){
                        if (!(kci%9==k%9&&kci/9==k/9)&&myStringSudoku[kci].equals("0")){
                            mySudoku[kci][value-1]=0;
                        }
                    }
                }
            }
        }
    }

    private void goEachCheck(){
        int onlyOne;
        myContinue=false;
        for (int a = 0; a < 81; a++) {
            if (!myStringSudoku[a].equals("0")){
                continue;
            }
            myContinue=true;
            onlyOne=0;
            for (int b = 0; b < 9; b++) {
                onlyOne+=mySudoku[a][b];
            }
            if (onlyOne<1){
                myContinue=false;
            }else if (onlyOne==1){
                for (int c=0;c<9;c++){
                    if (mySudoku[a][c]==1){
                        myStringSudoku[a]=String.valueOf(c+1);
                        continue;
                    }
                }
            }
        }
    }

    private void goLineCheck(){
        int onlyOne;
        int isMe = 0;
        for (int x=0;x<9;x++){
            for (int z=0;z<9;z++){//
                onlyOne=0;
                for (int y=x;y<=x+72;y+=9){
                    onlyOne+=mySudoku[y][z];
                    if(mySudoku[y][z]==1){
                        isMe=y;
                    }
                }
                if (onlyOne<1){
                    myContinue=false;
                }else if (onlyOne==1&&myStringSudoku[isMe].equals("0")){
                    myStringSudoku[isMe]=String.valueOf(z+1);
                    continue;
                }
            }
            for (int w=0;w<9;w++){
                onlyOne=0;
                for (int v=x*9;v<x*9+9;v++){
                    onlyOne+=mySudoku[v][w];
                    if(mySudoku[v][w]==1){
                        isMe=v;
                    }
                }
                if (onlyOne<1){
                    myContinue=false;
                }else if (onlyOne==1&&myStringSudoku[isMe].equals("0")){
                    myStringSudoku[isMe]=String.valueOf(w+1);
                    continue;
                }
            }
        }
    }

    private void superLog(String detail,int index){
        Log.d("superLog", "Detail:" + detail + ", index:" + index + ", value:" + myStringSudoku[index] + ", status:" +
                mySudoku[index][0] + mySudoku[index][1] + mySudoku[index][2] +
                mySudoku[index][3] + mySudoku[index][4] + mySudoku[index][5] +
                mySudoku[index][6] + mySudoku[index][7] + mySudoku[index][8]);
    }

    private class myThreadPartA extends Thread{
        @Override
        public void run() {
            while(myContinue){
                goThrough();
            }
            myContinue=true;
        }
    }

    private class myThreadPartB extends Thread{
        @Override
        public void run() {
            while(myContinue){
                goEachCheck();
                goLineCheck();
            }
            long t=System.currentTimeMillis()-myTime;
            Bundle timeBundle=new Bundle();
            timeBundle.putLong("TIME",t);
            Message msg = new Message();
            msg.what = THREADDONE;
            msg.setData(timeBundle);
            myHandler.sendMessage(msg);
            myContinue=true;
            refreshTable();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTime=System.currentTimeMillis();

        selectQ(Q1);
        initSudoku();
        initLayout();

        threadPartA=new myThreadPartA();
        threadPartB=new myThreadPartB();
    }

    @Override
    protected void onDestroy() {
        initSudoku();
        super.onDestroy();
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case REFRESH:
                    sudokuView.setText("");
                    String tempString;
                    for (int i=0;i<myStringSudoku.length;i++){
                        tempString=myStringSudoku[i];
                        sudokuView.append(tempString);
                        if (i%9==2||i%9==5){
                            sudokuView.append("。");
                        }else if (i%9==8){
                            sudokuView.append("\n");
                        }else{
                            sudokuView.append(".");
                        }
                    }
                    break;
                case DONE:
                    Bundle y=msg.getData();
                    long t=y.getLong("TIME");
                    totalTime+=t;
                    showTime.setText(t+"ms,"+"avg:"+((clickTime==0)?0:totalTime/clickTime)+"ms("+clickTime+")");
                    break;
                case THREADDONE:
                    Bundle z=msg.getData();
                    long v=z.getLong("TIME");
                    totalThreadTime+=v;
                    showThreadTime.setText(v + "ms," + "avg:" + ((clickThreadTime==0)?0:totalThreadTime / clickThreadTime )+ "ms(" + clickThreadTime + ")");
                    break;
            }
        }
    };
}
