package com.hanter.android.radwidget.cupertino;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StyleRes;

/**
 * 类名：CupertinoAlertDialogAction <br/>
 * 描述：CupertinoAlertDialogAction
 * 创建时间：2019/11/01 22:27
 *
 * @author hanter
 * @version 1.0
 */
public class CupertinoAlertDialogAction implements Parcelable {

    private String action;
    @StyleRes
    private int actionStyle;

    private boolean defaultAction;

    private boolean destructiveAction;

    public static CupertinoAlertDialogAction create(String action) {
        return new CupertinoAlertDialogAction(action);
    }

    public CupertinoAlertDialogAction(String action) {
        this(action, R.style.CupertinoAlertDialogAction, false, false);
    }

    public CupertinoAlertDialogAction(String action, int actionStyle) {
        this(action, actionStyle, false, false);
    }

    public CupertinoAlertDialogAction(String action, boolean defaultAction, boolean destructiveAction) {
        this(action, R.style.CupertinoAlertDialogAction, defaultAction, destructiveAction);
    }

    public CupertinoAlertDialogAction(String action, int actionStyle, boolean defaultAction, boolean destructiveAction) {
        this.action = action;
        this.actionStyle = actionStyle;
        this.defaultAction = defaultAction;
        this.destructiveAction = destructiveAction;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getActionStyle() {
        return actionStyle;
    }

    public void setActionStyle(int actionStyle) {
        this.actionStyle = actionStyle;
    }

    public boolean isDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(boolean defaultAction) {
        this.defaultAction = defaultAction;
    }

    public boolean isDestructiveAction() {
        return destructiveAction;
    }

    public void setDestructiveAction(boolean destructiveAction) {
        this.destructiveAction = destructiveAction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.action);
        dest.writeInt(this.actionStyle);
        dest.writeByte(this.defaultAction ? (byte) 1 : (byte) 0);
        dest.writeByte(this.destructiveAction ? (byte) 1 : (byte) 0);
    }

    protected CupertinoAlertDialogAction(Parcel in) {
        this.action = in.readString();
        this.actionStyle = in.readInt();
        this.defaultAction = in.readByte() != 0;
        this.destructiveAction = in.readByte() != 0;
    }

    public static final Creator<CupertinoAlertDialogAction> CREATOR = new Creator<CupertinoAlertDialogAction>() {
        @Override
        public CupertinoAlertDialogAction createFromParcel(Parcel source) {
            return new CupertinoAlertDialogAction(source);
        }

        @Override
        public CupertinoAlertDialogAction[] newArray(int size) {
            return new CupertinoAlertDialogAction[size];
        }
    };
}
