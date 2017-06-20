package com.xiayu.androidservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.xiayu.androidservice.R.id.id_ll_taskcontainer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button startService;
    private Button stopService;
    private Button bindService;
    private Button unbindService;
    private Button aidlService;

    private Button handlerthread;

    private Button intentservice;
    //与UI线程管理的handler
    private Handler mHandler = new Handler();


/*binder service 启动*/
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
        Log.e(TAG, "onCreate");

        int priority = android.os.Process.getThreadPriority(android.os.Process.myTid());
        Log.e(TAG,"MainThread thread id:"+ Thread.currentThread().getId()+ "   priority:"+priority);


        startService = (Button)findViewById(R.id.start_service);
        stopService = (Button)findViewById(R.id.stop_service);
        bindService = (Button)findViewById(R.id.bind_service);
        unbindService = (Button)findViewById(R.id.unbind_service);
        aidlService = (Button)findViewById(R.id.aidl_service);
        handlerthread = (Button)findViewById(R.id.handlerthread);
        intentservice = (Button)findViewById(R.id.intentservice);

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


        mLyTaskContainer = (LinearLayout) findViewById(id_ll_taskcontainer);
        registerReceiver();

        intentservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask(v);
            }
        });


        Button button = (Button)findViewById(R.id.cycle);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                startActivityForResult(intent ,1);
            }
        });



        Button startmode = (Button)findViewById(R.id.startmode);
        startmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState() called with: savedInstanceState = [" + savedInstanceState + "]");
    }

        /*intentService*/

    public static final String UPLOAD_RESULT = "com.zhy.blogcodes.intentservice.UPLOAD_RESULT";
    private LinearLayout mLyTaskContainer;
    int i;
    public void addTask(View view)
    {
        //模拟路径
        String path = "/sdcard/imgs/" + (++i) + ".png";
        UploadImgService.startUploadImg(this, path);

        TextView tv = new TextView(this);
        mLyTaskContainer.addView(tv);
        tv.setText(path + " is uploading ...");
        tv.setTag(path);
    }

    private BroadcastReceiver uploadImgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ( UPLOAD_RESULT.equals(intent.getAction())) {
                Log.e(TAG, "onReceive UPLOAD_RESULT");
                String path = intent.getStringExtra(UploadImgService.EXTRA_IMG_PATH);

                handleResult(path);

            }

        }
    };

    private void handleResult(String path)
    {
        TextView tv = (TextView) mLyTaskContainer.findViewWithTag(path);
        tv.setText(path + " upload success ~~~ ");
    }
    private void registerReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_RESULT);
        registerReceiver(uploadImgReceiver, filter);
    }




    /*handlerthread */
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
                int priority = android.os.Process.getThreadPriority(android.os.Process.myTid());
                Log.e(TAG,"HandlerThread thread id:"+ Thread.currentThread().getId()+"    priority:"+priority);
                checkForUpdate();

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
            int i=0;
/*            while(true){
                Log.e(TAG,"TMP");
                i++;
            }*/
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
        Log.d(TAG, "onStart: ");
        super.onStart();
        if (!mBound) {
            attemptToBindService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        //释放资源
        if(mCheckMsgThread!=null) {
            mCheckMsgThread.quit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        BlankFragment fragment = new BlankFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
       // transaction.add(fragment,"test");
        transaction.replace(R.id.fragment_container,fragment);
        transaction.commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");
    }

}
