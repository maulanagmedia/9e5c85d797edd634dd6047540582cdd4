package gmedia.net.id.pullens.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.FormatItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.pullens.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListHomeMenuAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListHomeMenuAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_home_menu, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private ImageView ivThumbnail;
        private TextView tvItem1, tvItem2, tvItem3;
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
    }

    /*@Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {

        int hasil = 0;
        final CustomItem item = items.get(position);
        if(!item.getItem9().equals("1") || !item.getItem10().equals("1") || !item.getItem11().equals("1")){
            hasil = 0;
        }else{
            hasil = 1;
        }
        return hasil;
    }*/

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        //int tipeViewList = getItemViewType(position);

        if(convertView == null){

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_home_menu, null);

            holder.ivThumbnail = (ImageView) convertView.findViewById(R.id.iv_thumnail);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);

        ImageUtils iu = new ImageUtils();
        iu.LoadRealImage(context, itemSelected.getItem4(), holder.ivThumbnail);
        holder.tvItem1.setText(itemSelected.getItem2());
        holder.tvItem2.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemSelected.getItem3())));
        holder.tvItem3.setText(itemSelected.getItem5());

        return convertView;
    }
}
