package com.xuanfeng.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DownloadAdapter extends BaseAdapter {
    
    private List<DownloadItem> items;
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    public interface OnItemClickListener {
        void onItemClick(DownloadItem item);
    }
    
    public DownloadAdapter(List<DownloadItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    
    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        DownloadItem item = items.get(position);
        
        // 文名
        holder.tvFileName.setText(item.getFileName());
        
        // 外文件
        if (item.isForeign()) {
            holder.tvFileName.setTextColor(0xFFFF4444);
            holder.tvStatus.setText("外来文件");
            holder.tvStatus.setTextColor(0xFFFF4444);
            holder.tvSize.setText(item.getFormattedSize());
            holder.tvTime.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        } else {
            holder.tvFileName.setTextColor(0xFF000000);
            
            // 状
            String statusText = "";
            switch (item.getStatus()) {
                case 1: statusText = "下载中 " + item.getProgress() + "%"; break;
                case 2: statusText = "已暂停"; break;
                case 3: statusText = "已完成"; break;
                case 4: statusText = "失败"; break;
                default: statusText = "未知";
            }
            holder.tvStatus.setText(statusText);
            holder.tvStatus.setTextColor(0xFF666666);
            
            // 大 时
            holder.tvSize.setText(item.getFormattedSize());
            holder.tvTime.setText(item.getDownloadTime());
            holder.tvTime.setVisibility(View.VISIBLE);
            
            // 进度
            if (item.getStatus() == 1) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setProgress(item.getProgress());
            } else {
                holder.progressBar.setVisibility(View.GONE);
            }
        }
        
        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
        
        return convertView;
    }
    
    static class ViewHolder {
        TextView tvFileName, tvStatus, tvSize, tvTime;
        ProgressBar progressBar;
        
        ViewHolder(View view) {
            tvFileName = view.findViewById(R.id.tv_file_name);
            tvStatus = view.findViewById(R.id.tv_status);
            tvSize = view.findViewById(R.id.tv_size);
            tvTime = view.findViewById(R.id.tv_time);
            progressBar = view.findViewById(R.id.progress_bar);
        }
    }
}