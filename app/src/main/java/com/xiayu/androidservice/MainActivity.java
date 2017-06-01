package com.xiayu.androidservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button startService;
    private Button stopService;
    private Button bindService;
    private Button unbindService;
    private Button aidlService;

    private Button handlerthread;
    //与UI线程管理的handler
    private Handler mHandler = new Handler();



    private MyService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (MyService.DownloadBinder) service;
            downloadBinder.startDownload();
            downloadBinder.getProgress();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG,"MainThread thread id:"+ Thread.currentThread().getId());

        startService = (Button)findViewById(R.id.start_service);
        stopService = (Button)findViewById(R.id.stop_service);
        bindService = (Button)findViewById(R.id.bind_service);
        unbindService = (Button)findViewById(R.id.unbind_service);
        aidlService = (Button)findViewById(R.id.aidl_service);
        handlerthread = (Button)findViewById(R.id.handlerthread);

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(MainActivity.this, MyService.class);
                startService(startIntent);
            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(MainActivity.this, MyService.class);
                stopService(startIntent);
            }
        });

        bindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bindIntent = new Intent(MainActivity.this, MyService.class);
                bindService(bindIntent, connection, BIND_AUTO_CREATE);
            }
        });

        unbindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(connection);
            }
        });
        aidlService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToBindService();
                addBook();
            }
        });

        handlerthread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBackThread();
                mCheckMsgHandler.sendEmptyMessage(MSG_UPDATE_INFO);

            }
        });

    }


    private HandlerThread mCheckMsgThread;
    private Handler mCheckMsgHandler;
    private static final int MSG_UPDATE_INFO = 0x110;
    private TextView mTvServiceInfo;

    private void initBackThread()
    {
        mTvServiceInfo = (TextView) findViewById(R.id.id_textview);
        mCheckMsgThread = new HandlerThread("check-message-coming");
        mCheckMsgThread.start();
        mCheckMsgHandler = new Handler(mCheckMsgThread.getLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                checkForUpdate();
                Log.e(TAG,"HandlerThread thread id:"+ Thread.currentThread().getId());
                mCheckMsgHandler.sendEmptyMessageDelayed(MSG_UPDATE_INFO, 1000);

            }
        };


    }

    /**
     * 模拟从服务器解析数据
     */
    private void checkForUpdate()
    {
        try
        {
            //模拟耗时
            Thread.sleep(1000);
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.e(TAG,"mHandler thread id:"+ Thread.currentThread().getId());
                    String result = "实时更新中，当前大盘指数：<font color='red'>%d</font>";
                    result = String.format(result, (int) (Math.random() * 3000 + 1000));
                    mTvServiceInfo.setText(Html.fromHtml(result));
                }
            });

        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }




    private BookManager mBookManager = null;
    private boolean mBound = false;

    //包含Book对象的list
    private List<Book> mBooks;

    public void addBook() {
        //如果与服务端的连接处于未连接状态，则尝试连接
        if (!mBound) {
            attemptToBindService();
            Toast.makeText(this, "当前与服务端处于未连接状态，正在尝试重连，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mBookManager == null) return;

        Book book = new Book();
        book.setName("APP研发录In");
        book.setPrice(30);
        try {
            mBookManager.addBook(book);
            List<Book> books = mBookManager.getBooks();
            Log.e(TAG,"books:"+books);
            Log.e(getLocalClassName(), book.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        
        
    }

    /**
     * 尝试与服务端建立连接
     */
    private void attemptToBindService() {
        Intent intent = new Intent();
        intent.setAction("com.xiayu.aidl");
        intent.setPackage("com.xiayu.androidservice");
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(getLocalClassName(), "service connected");
            mBookManager = BookManager.Stub.asInterface(service);
            mBound = true;

            if (mBookManager != null) {
                try {
                    mBooks = mBookManager.getBooks();
                    Log.e(getLocalClassName(), mBooks.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(getLocalClassName(), "service disconnected");
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            attemptToBindService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        mCheckMsgThread.quit();
    }
}
