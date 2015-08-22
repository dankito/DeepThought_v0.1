package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import net.deepthought.R;

/**
 * Created by renard on 28/12/13.
 */
public class NavigationDrawerAdapter extends BaseAdapter implements ListAdapter {

    protected Activity context;

    public NavigationDrawerAdapter(Activity context) {
        this.context = context;
    }


    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.list_item_drawer, null);
        }
        int textId = 0;
        int iconId = 0;

        switch (position) {
            case 0:
                textId = R.string.device_registration;
//                iconId = R.drawable.ic_whats_new;
                break;
//            case 1:
//                textId = R.string.nav_drawer_download_language;
//                iconId = R.drawable.ic_action_language;
//                break;
//            case 2:
//                textId = R.string.pref_title_help;
//                iconId = R.drawable.ic_action_tips_light;
//                break;
//            case 3:
//                textId = R.string.help_to_improve;
//                iconId = R.drawable.ic_action_feedback;
//                break;
//            case 4:
//                textId = R.string.about;
//                iconId = R.drawable.ic_action_about;
//                break;
//            case 5:
//                textId = R.string.product_tour;
//                break;
        }

        TextView txtDrawerOptionName = (TextView)convertView.findViewById(R.id.txtDrawerOptionName);
        txtDrawerOptionName.setText(textId);

//        if (iconId!=0){
//            txtDrawerOptionName.setCompoundDrawablesWithIntrinsicBounds(convertView.getResources().getDrawable(iconId),null,null,null);
//        }

        return convertView;
    }
}
