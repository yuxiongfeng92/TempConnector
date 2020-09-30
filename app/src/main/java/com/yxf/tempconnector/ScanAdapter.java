package com.yxf.tempconnector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proton.temp.connector.bean.DeviceBean;

import java.util.List;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.VH> {
    private List<DeviceBean> list;
    private Context context;

    public ScanAdapter(List<DeviceBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_scan_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, final int position) {
        holder.txtMac.setText(String.format("mac:%s", list.get(position).getMacaddress()));
        holder.txtConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.connectListener(list.get(position));
                }
            }
        });

        holder.txtDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.disconnectListener(list.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == list ? 0 : list.size();
    }

    public class VH extends RecyclerView.ViewHolder {
        private TextView txtMac, txtConnect,txtDisconnect;

        public VH(@NonNull View itemView) {
            super(itemView);
            txtMac = itemView.findViewById(R.id.id_mac);
            txtConnect = itemView.findViewById(R.id.id_connect);
            txtDisconnect = itemView.findViewById(R.id.id_disconnect);
        }
    }

    private ConnectListener listener;

    public void setListener(ConnectListener listener) {
        this.listener = listener;
    }

    public interface ConnectListener {
        void connectListener(DeviceBean deviceBean);
        void disconnectListener(DeviceBean deviceBean);
    }
}
