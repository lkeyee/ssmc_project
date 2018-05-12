package com.smcc.sensorrecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter{
    // 数据集
    private List<Bean> list = new ArrayList<Bean>();
    // 上下文
    private Context mContext;
    // 存储勾选框状态的map集合
    private Map<Integer, Boolean> isCheck = new HashMap<Integer, Boolean>();

    // 构造方法
    public ListAdapter(Context mContext) {
        super();
        this.mContext = mContext;
        // 默认为不选中
        initCheck(false);
    }

    // 初始化map集合
    public void initCheck(boolean flag) {
        // map集合的数量和list的数量是一致的
        for (int i = 0; i < list.size(); i++) {
            // 设置默认的显示
            isCheck.put(i, flag);
        }
    }
    // 设置数据
    public void setData(List<Bean> data) {
        this.list = data;
    }

    // 添加数据
    public void addData(Bean bean) {
        // 下标 数据
        list.add(0, bean);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        // 如果为null就返回一个0
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        View view = null;
        // 判断是不是第一次进来
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.tvTitle);
            viewHolder.cbCheckBox = (CheckBox) view
                    .findViewById(R.id.cbCheckBox);
            // 标记，可以复用
            view.setTag(viewHolder);
        } else {
            view = convertView;
            // 直接拿过来用
            viewHolder = (ViewHolder) view.getTag();
        }
        // 拿到对象
        Bean bean = list.get(position);
        // 填充数据
        viewHolder.title.setText(bean.getTitle().toString());
        // 勾选框的点击事件
        viewHolder.cbCheckBox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        // 用map集合保存
                        isCheck.put(position, isChecked);
                    }
                });
        // 设置状态
        if (isCheck.get(position) == null) {
            isCheck.put(position, false);
        }
        viewHolder.cbCheckBox.setChecked(isCheck.get(position));
        return view;
    }

    // 优化
    public static class ViewHolder {
        public TextView title;
        public CheckBox cbCheckBox;
    }

    // 全选按钮获取状态
    public Map<Integer, Boolean> getMap() {
        // 返回状态
        return isCheck;
    }

    // 删除一个数据
    public void removeData(int position) {
        list.remove(position);
    }

}
