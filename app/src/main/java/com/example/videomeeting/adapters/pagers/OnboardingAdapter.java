package com.example.videomeeting.adapters.pagers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.videomeeting.R;
import com.example.videomeeting.models.OnboardingItem;

import java.util.List;

public class OnboardingAdapter extends PagerAdapter {

    Context context;
    List<OnboardingItem> itemsList;

    public OnboardingAdapter(Context context, List<OnboardingItem> itemsList) {
        this.context = context;
        this.itemsList = itemsList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate(R.layout.item_onboarding, null);

        ImageView iconIV = layoutScreen.findViewById(R.id.iconIV);
        TextView titleTV = layoutScreen.findViewById(R.id.titleTV);
        TextView descriptionTV = layoutScreen.findViewById(R.id.descriptionTV);

        titleTV.setText(itemsList.get(position).getTitle());
        descriptionTV.setText(itemsList.get(position).getDescription());
        iconIV.setImageResource(itemsList.get(position).getImage());

        container.addView(layoutScreen);

        return layoutScreen;
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
