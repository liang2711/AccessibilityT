package com.example.accessibilityt.view.setting;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.R;
import com.example.accessibilityt.view.VDataTools;

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.AppViewHoder>{
    private Context mContext;
    private List<String> list;
    public SettingAdapter(Context mContext,List<String> list){
        this.mContext=mContext;
        this.list=list;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public AppViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LinearLayout layout=new LinearLayout(parent.getContext());
//        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//        layout.setLayoutParams(layoutParams);
        View view=View.inflate(mContext, R.layout.appname_item,null);
        return new AppViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHoder holder, int position) {
        holder.textView.setText(list.get(position));
        holder.imageView.setVisibility(View.VISIBLE);
        holder.menuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> listTemporary= VDataTools.getPK();
                if (listTemporary.remove(holder.textView.getText())){
                    VDataTools.putPK(listTemporary);
                    setList(listTemporary);
                    notifyDataSetChanged();
                }else Log.e(MainActivity.TAG,"model.json PK error! SettingAdapter.onBindViewHolder");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class AppViewHoder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        TextView menuLayout;
        LinearLayout contentLayout;

        public AppViewHoder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.a_item_image);
            textView=itemView.findViewById(R.id.a_item_tv);
            menuLayout=itemView.findViewById(R.id.menu_layout_delete);
            contentLayout=itemView.findViewById(R.id.content_layout);
        }
    }
}
