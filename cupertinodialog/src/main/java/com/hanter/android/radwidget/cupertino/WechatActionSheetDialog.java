package com.hanter.android.radwidget.cupertino;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hanter.android.radwidget.cupertino.blur.BlurView;
import com.hanter.android.radwidget.cupertino.blur.RenderScriptBlur;

import java.util.ArrayList;
import java.util.List;

public class WechatActionSheetDialog extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_ACTION_LIST = "actionList";
    private static final String ARG_CANCEL_ACTION = "cancel";

    private String title;
    private String message;
    private List<CupertinoActionSheetAction> actionList;
    private CupertinoActionSheetAction cancelAction;
    private RecyclerView rcvActions;
    private ActionAdapter actionAdapter;
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onActionClick(WechatActionSheetDialog dialog, int position);
    }

    public static WechatActionSheetDialog newInstance(String title, String message, List<String> actions) {
        ArrayList<CupertinoActionSheetAction> list = new ArrayList<>();
        for (String action : actions) {
            list.add(CupertinoActionSheetAction.create(action));
        }
        CupertinoActionSheetAction cancelAction = new CupertinoActionSheetAction("取消", false, false);
        return newInstance(title, message, list, cancelAction);
    }

    public static WechatActionSheetDialog newInstance(String title, String message,
                                                      ArrayList<CupertinoActionSheetAction> data) {
        return newInstance(title, message, data, null);
    }

    public static WechatActionSheetDialog newInstance(String title,
                                                      String message,
                                                      ArrayList<CupertinoActionSheetAction> data,
                                                      CupertinoActionSheetAction cancelAction) {
        WechatActionSheetDialog fragment = new WechatActionSheetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putParcelableArrayList(ARG_ACTION_LIST, data);
        args.putParcelable(ARG_CANCEL_ACTION, cancelAction);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog_Cupertino_ActionSheet);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            message = getArguments().getString(ARG_MESSAGE);
            actionList = getArguments().getParcelableArrayList(ARG_ACTION_LIST);
            cancelAction = getArguments().getParcelable(ARG_CANCEL_ACTION);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnActionClickListener) {
            this.listener = (OnActionClickListener) context;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_wechat_action_sheet, container, false);
        initViews(rootView);
        return rootView;
    }

    private void initViews(View rootView) {
        if (getActivity() != null) {
            BlurView blurView = rootView.findViewById(R.id.blurView);
            ViewGroup decorView = (ViewGroup) getActivity().getWindow().getDecorView();
            blurView.setupWith(decorView)
                    .setFrameClearDrawable(decorView.getBackground())
                    .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                    .setBlurRadius(20)
                    .setHasFixedTransformationMatrix(true);
        }

        TextView tvTitle = rootView.findViewById(R.id.title);

        tvTitle.setText(title);
        if (TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.GONE);
        }

        TextView tvMessage = rootView.findViewById(R.id.message);

        tvMessage.setText(message);
        if (TextUtils.isEmpty(message)) {
            tvMessage.setVisibility(View.GONE);
        }

        int paddingHorizontal = getResources().getDimensionPixelSize(R.dimen.action_sheet_content_padding_horizontal);
        int paddingVertical = getResources().getDimensionPixelSize(R.dimen.action_sheet_content_padding_vertical);
        tvMessage.setPadding(
                paddingHorizontal,
                TextUtils.isEmpty(title) ? paddingVertical : 0,
                paddingHorizontal,
                TextUtils.isEmpty(title) ? paddingVertical : getResources().getDimensionPixelSize(R.dimen.action_sheet_message_padding_top_without_title)
        );

        rcvActions = rootView.findViewById(R.id.rcvActions);
        actionAdapter = new ActionAdapter(rcvActions, actionList);
        actionAdapter.setOnItemClickListener(new OnItemClickListener(rcvActions) {
            @Override
            public void onItemClick(View v, int position) {
                if (listener != null)
                    listener.onActionClick(WechatActionSheetDialog.this, position);
            }
        });
        rcvActions.setAdapter(actionAdapter);

        if (getContext() != null) {
            ActionDividerDecoration dividerDecoration = new ActionDividerDecoration(
                    getContext(),
                    ActionDividerDecoration.VERTICAL,
                    R.color.cupertinoActionSheetDivider,
                    R.color.actionSheetDialogOverlayPressed);
            dividerDecoration.setDividerHeight(1);
            dividerDecoration.setHeaderDividersEnabled(!TextUtils.isEmpty(title) || !TextUtils.isEmpty(message));
            rcvActions.addItemDecoration(dividerDecoration);
        }

        Button btnCancel = rootView.findViewById(R.id.btnCancel);
        if (cancelAction == null) {
            btnCancel.setVisibility(View.GONE);
        } else {
            btnCancel.setText(cancelAction.getAction());
            if (cancelAction.isDestructiveAction()) {
                btnCancel.setTextColor(CupertinoColors.destructiveRed);
            } else {
                btnCancel.setTextColor(0xFF1F1F1F);
            }
            if (cancelAction.isDefaultAction()) {
                btnCancel.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                btnCancel.setTypeface(Typeface.DEFAULT);
            }

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    public void setListener(OnActionClickListener listener) {
        this.listener = listener;
    }

    static class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder> implements
            CupertinoDialogActionButton.OnActionDownChangeListener {

        private final RecyclerView recyclerView;
        private OnItemClickListener onItemClickListener;
        @Nullable
        private final List<CupertinoActionSheetAction> actionList;

        ActionAdapter(RecyclerView recyclerView, @Nullable List<CupertinoActionSheetAction> actionList) {
            this.recyclerView = recyclerView;
            this.actionList = actionList;
        }

        @NonNull
        @Override
        public ActionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_action_sheet_wechat, parent, false));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull ActionAdapter.ViewHolder holder, int position) {

            CupertinoActionSheetAction item = getItem(position);

            if (item == null)
                return;

            holder.action.setText(item.getAction());
            if (item.isDestructiveAction()) {
                holder.action.setTextColor(CupertinoColors.destructiveRed);
            } else {
                holder.action.setTextColor(0xFF1F1F1F);
            }
            if (item.isDefaultAction()) {
                holder.action.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                holder.action.setTypeface(Typeface.DEFAULT);
            }
            holder.action.setOnActionDownChangeListener(this);
            holder.action.setOnClickListener(onItemClickListener);
        }

        CupertinoActionSheetAction getItem(int position) {
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
                action = view.findViewById(R.id.action);
            }
        }
    }

    public static abstract class OnItemClickListener implements View.OnClickListener {

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
