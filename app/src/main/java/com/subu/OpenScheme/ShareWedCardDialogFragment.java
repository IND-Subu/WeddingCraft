package com.subu.OpenScheme;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.subu.weddingcraft.R;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;


public class ShareWedCardDialogFragment extends DialogFragment {
    private static final String TAG = "ShareWedCardDialog";
    private LinearLayout shareAppsContainer;
    private File wedcardFile;

    public static ShareWedCardDialogFragment newInstance(File file) {
        ShareWedCardDialogFragment fragment = new ShareWedCardDialogFragment();
        Bundle args = new Bundle();
        args.putString("file_path", file.getAbsolutePath());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sharing_to_contacts_layout, container, false);

        assert getArguments() != null;
        String filePath = getArguments().getString("file_path");
        assert filePath != null;
        wedcardFile = new File(filePath);

        shareAppsContainer = view.findViewById(R.id.shareAppsContainer);
        View closeButton = view.findViewById(R.id.closeButton);

        closeButton.setOnClickListener(v -> dismiss());

        loadSystemShareApps();

        return view;
    }

    private void loadSystemShareApps() {
        PackageManager manager = requireContext().getPackageManager();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/octet-stream");

        List<ResolveInfo> targets = manager.queryIntentActivities(shareIntent, 0);

        // Limit to most common apps for cleaner UI
        int maxApps = Math.min(targets.size(), 12);

        for (int i = 0; i < maxApps; i++) {
            ResolveInfo resolveInfo = targets.get(i);

            // Create app item container
            LinearLayout appItem = new LinearLayout(getContext());
            appItem.setOrientation(LinearLayout.VERTICAL);
            appItem.setGravity(android.view.Gravity.CENTER);

            int itemSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(itemSize, itemSize);
            int margin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            itemParams.setMargins(margin, margin, margin, margin);
            appItem.setLayoutParams(itemParams);

            // Add ripple effect
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(
                    android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            appItem.setBackgroundResource(outValue.resourceId);
            appItem.setClickable(true);
            appItem.setFocusable(true);

            // App icon
            ImageView appIcon = new ImageView(getContext());
            int iconSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            appIcon.setLayoutParams(iconParams);

            Drawable icon = resolveInfo.loadIcon(manager);
            appIcon.setImageDrawable(icon);
            appIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // App label
            TextView appLabel = new TextView(getContext());
            appLabel.setText(resolveInfo.loadLabel(manager));
            appLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            appLabel.setMaxLines(2);
            appLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
            appLabel.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            appLabel.setLayoutParams(labelParams);

            appItem.addView(appIcon);
            appItem.addView(appLabel);

            appItem.setOnClickListener(v -> {
                try {
                    Uri uri = FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".provider",
                            wedcardFile
                    );
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/octet-stream");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setPackage(resolveInfo.activityInfo.packageName);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Error sharing file", e);
                    Toast.makeText(getContext(), "Unable to share file", Toast.LENGTH_SHORT).show();
                }
            });

            shareAppsContainer.addView(appItem);
        }
    }
}