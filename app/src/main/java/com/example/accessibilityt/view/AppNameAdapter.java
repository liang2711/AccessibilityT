package com.example.accessibilityt.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.R;
import com.example.accessibilityt.dao.AppCodeInfo;

import java.util.List;

public class AppNameAdapter extends RecyclerView.Adapter<AppNameAdapter.AppViewHoder> implements View.OnClickListener{
    Context mContext;
    List<AppCodeInfo> infos;
    public AppNameAdapter(Context context, List<AppCodeInfo> infos){
        mContext=context;
        this.infos=infos;
    }

    public List<AppCodeInfo> getInfos() {
        return infos;
    }

    public void setInfos(List<AppCodeInfo> infos) {
        this.infos = infos;
    }

    @NonNull
    @Override
    public AppViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=View.inflate(mContext, R.layout.appname_item,null);
        AppViewHoder appViewHoder=new AppViewHoder(view);
        return appViewHoder;
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHoder holder, int position) {
        AppCodeInfo info=infos.get(position);
        holder.textView.setText(info.appName);

        holder.textView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return infos.size();
    }

    @Override
    public void onClick(View v) {
        Log.d(MainActivity.TAG,"tv be click");
    }

    class AppViewHoder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;

        public AppViewHoder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.a_item_image);
            textView=itemView.findViewById(R.id.a_item_tv);
        }
    }
}
