package com.hanter.android.radwidget.cupertino;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;

public class CupertinoAlertDialog extends DialogFragment {

    private static final String TAG = "CupertinoAlertDialog";

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_ACTION_LIST = "actionList";

    private String title;
    private String message;
    private RecyclerView rcvButtons;
    private List<CupertinoAlertDialogAction> actionList;
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onActionClick(CupertinoAlertDialog dialog, int position);
    }

    public static CupertinoAlertDialog newInstance(String title, String message, List<String> actions) {
        ArrayList<CupertinoAlertDialogAction> list = new ArrayList<>();
        for (String action : actions) {
            list.add(CupertinoAlertDialogAction.create(action));
        }
        return newInstance(title, message, list);
    }

    public static CupertinoAlertDialog newInstance(String title, String message, ArrayList<CupertinoAlertDialogAction> actions) {
        CupertinoAlertDialog fragment = new CupertinoAlertDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putParcelableArrayList(ARG_ACTION_LIST, actions);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog_Cupertino_Alert);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            message = getArguments().getString(ARG_MESSAGE);
            actionList = getArguments().getParcelableArrayList(ARG_ACTION_LIST);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnActionClickListener) {
            this.listener = (OnActionClickListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_cupertino_alert, container, false);
        initViews(rootView);
        return rootView;
    }

    private void initViews(View rootView) {
        Log.d(TAG, "activity: " + getActivity());

        if (getActivity() != null) {
            BlurView blurView = rootView.findViewById(R.id.blurView);
            blurView.setupWith((ViewGroup) getActivity().getWindow().getDecorView())
                    .setFrameClearDrawable(null)
                    .setBlurAlgorithm(new SupportRenderScriptBlur(getContext()))
                    .setBlurRadius(20)
                    .setHasFixedTransformationMatrix(true);
        }

        TextView tvTitle = rootView.findViewById(R.id.title);

        tvTitle.setText(title);
        if (TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.GONE);
        }

        int edgePadding = getResources().getDimensionPixelSize(R.dimen.cupertino_alert_edge_padding);
        int extraPadding = getResources().getDimensionPixelSize(R.dimen.cupertino_alert_extra_padding);

        tvTitle.setPadding(edgePadding, edgePadding, edgePadding,
                TextUtils.isEmpty(message) ? edgePadding : extraPadding);

        TextView tvMessage = rootView.findViewById(R.id.message);

        tvMessage.setText(message);
        if (TextUtils.isEmpty(message)) {
            tvMessage.setVisibility(View.GONE);
        }

        tvMessage.setPadding(edgePadding, TextUtils.isEmpty(title) ? edgePadding : extraPadding,
                edgePadding, edgePadding);

        rcvButtons = rootView.findViewById(R.id.rcvButtons);

        RecyclerView.LayoutManager layoutManager;
        if (actionList.size() == 1) {
            layoutManager = new GridLayoutManager(getContext(), 1);
        } else if (actionList.size() == 2) {
            layoutManager = new GridLayoutManager(getContext(), 2);

            ActionDividerDecoration dividerDecoration = new ActionDividerDecoration(
                    getContext(),
                    ActionDividerDecoration.HORIZONTAL,
                    R.color.cupertinoAlertButtonDivider,
                    R.color.cupertinoAlertOverlayPressed);
            dividerDecoration.setDividerWidth(1);
            dividerDecoration.setHeaderDividersEnabled(false);
            rcvButtons.addItemDecoration(dividerDecoration);

        } else {
            layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            ActionDividerDecoration dividerDecoration = new ActionDividerDecoration(
                    getContext(),
                    ActionDividerDecoration.VERTICAL,
                    R.color.cupertinoAlertButtonDivider,
                    R.color.cupertinoAlertOverlayPressed);
            dividerDecoration.setDividerHeight(1);
            dividerDecoration.setHeaderDividersEnabled(false);
            rcvButtons.addItemDecoration(dividerDecoration);
        }
        rcvButtons.setLayoutManager(layoutManager);

        ActionAdapter actionAdapter = new ActionAdapter(rcvButtons, actionList);
        actionAdapter.setOnItemClickListener(new OnItemClickListener(rcvButtons) {
            @Override
            public void onItemClick(View v, int position) {
                dismiss();
                if (listener != null)
                    listener.onActionClick(CupertinoAlertDialog.this, position);
            }
        });
        rcvButtons.setAdapter(actionAdapter);
    }

    public void setListener(OnActionClickListener listener) {
        this.listener = listener;
    }

    static class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder> implements
            CupertinoDialogActionButton.OnActionDownChangeListener {

        private final RecyclerView recyclerView;
        private OnItemClickListener onItemClickListener;
        @Nullable
        private final List<CupertinoAlertDialogAction> actionList;

        ActionAdapter(RecyclerView recyclerView, @Nullable List<CupertinoAlertDialogAction> actionList) {
            this.recyclerView = recyclerView;
            this.actionList = actionList;
        }

        @NonNull
        @Override
        public ActionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cupertino_alert_button, parent, false));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull ActionAdapter.ViewHolder holder, int position) {
            CupertinoAlertDialogAction item = getItem(position);

            if (item == null)
                return;

            holder.action.setText(item.getAction());
            holder.action.setText(item.getAction());
            if (item.isDestructiveAction()) {
                holder.action.setTextColor(CupertinoColors.destructiveRed);
            } else {
                holder.action.setTextColor(CupertinoColors.activeBlue);
            }

            TextViewCompat.setTextAppearance(holder.action, item.getActionStyle());

            if (item.isDefaultAction()) {
                holder.action.setTypeface(Typeface.DEFAULT_BOLD);
            }
            holder.action.getPaint().setFakeBoldText(true);
            holder.action.setOnActionDownChangeListener(this);
            holder.action.setOnClickListener(onItemClickListener);
        }

        CupertinoAlertDialogAction getItem(int position) {
            if (actionList == null || position >= actionList.size() || position < 0) {
                return null;
            } else {
                return actionList.get(position);
            }
        }

        @Override
        public int getItemCount() {
            return actionList == null ? 0 : actionList.size();
        }

        @Override
        public void onActionDownChange(View view, boolean actionDown) {
            recyclerView.invalidateItemDecorations();
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            CupertinoDialogActionButton action;

            ViewHolder(View view) {
                super(view);
                action = (CupertinoDialogActionButton) view;
            }
        }
    }

    static abstract class OnItemClickListener implements View.OnClickListener {

        private RecyclerView recyclerView;

        public OnItemClickListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void onClick(View v) {
            RecyclerView.ViewHolder holder = recyclerView.findContainingViewHolder(v);

            int position = AdapterView.INVALID_POSITION;

            if (holder != null) {
                position = holder.getAdapterPosition();
            }

            onItemClick(v, position);
        }


        public abstract void onItemClick(View v, int position);
    }

}
