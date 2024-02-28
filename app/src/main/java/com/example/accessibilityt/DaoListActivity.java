package com.example.accessibilityt;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accessibilityt.dao.AppCodeInfo;
import com.example.accessibilityt.dao.AppCodeInfoDao;
import com.example.accessibilityt.view.AppListTouchListener;
import com.example.accessibilityt.view.FloadWindowService;
import com.example.accessibilityt.view.VDataTools;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

//显示所有数据库数据
public class DaoListActivity extends AppCompatActivity {
    enum STATU{
        FIRST,
        REFRESH
    }
    static DaoListAdapter adapter=null;
    RecyclerView mRecyclerView;
    private static Context mContext;
    private static boolean isFirstEnter = true;
    private static Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            List<AppCodeInfo> list
                    =msg.getData().getParcelableArrayList("list");
            if (list!=null){
                adapter.setInfos(list);
            }else{
                Toast.makeText(mContext,"刷新失败", Toast.LENGTH_SHORT).show();
                Log.d(MainActivity.TAG,"DaoListActivity handle list is null");
                return;
            }
            if (msg.what==STATU.REFRESH.ordinal())
                Toast.makeText(mContext,"已刷新", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        mRecyclerView=findViewById(R.id.a_list_rv);
        mContext=getApplicationContext();
        adapter= new DaoListAdapter(this,new ArrayList<AppCodeInfo>());
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        RefreshLayout refreshLayout=findViewById(R.id.a_list_rl);
        if (isFirstEnter) {
            isFirstEnter = false;
            refreshLayout.autoRefresh();//第一次进入触发自动刷新，演示效果
        }
        getAppListData(STATU.FIRST);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(2000);
                getAppListData(STATU.REFRESH);
            }
        });
    }

    private void getAppListData(STATU statu){
        if (VDataTools.rDatabase==null){
            VDataTools.daoInit(FloadWindowService.mContext);
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                List<AppCodeInfo> list=null;
                try {
                    AppCodeInfoDao dao=VDataTools.rDatabase.getDao();
                    list=dao.getAll();
                }catch (Exception e){
                    Log.d(AppNameListActivity.TAG,"database table Query fail for all Data");
                }
                Message message=Message.obtain();
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("list",new ArrayList<>(list));
                message.setData(bundle);
                message.what=statu.ordinal();
                handler.sendMessage(message);
            }
        }.start();
    }
    class DaoListAdapter extends RecyclerView.Adapter<DaoListAdapter.ViewHolder>{
        List<AppCodeInfo> infos;
        Context mContext;

        public DaoListAdapter(Context mContext, List<AppCodeInfo> infos){
            this.mContext=mContext;
            this.infos=infos;
        }

        public void setInfos(List<AppCodeInfo> infos) {
            this.infos = infos;
        }

        public List<AppCodeInfo> getInfos() {
            return infos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=View.inflate(mContext,R.layout.d_list,null);
            ViewHolder viewHolder=new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppCodeInfo info=infos.get(position);
            holder.cName.setText(info.className);
            holder.aName.setText(info.appName);
        }

        @Override
        public int getItemCount() {
            return infos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView aName;
            TextView cName;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                aName=itemView.findViewById(R.id.d_aname);
                cName=itemView.findViewById(R.id.d_cname);
            }
        }
    }
}
