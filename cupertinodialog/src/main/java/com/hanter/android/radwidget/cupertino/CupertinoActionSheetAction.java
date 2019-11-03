package com.hanter.android.radwidget.cupertino;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 类名：CupertinoActionSheetAction <br/>
 * 描述：CupertinoActionSheetAction
 * 创建时间：2019/11/01 22:35
 *
 * @author hanter
 * @version 1.0
 */
public class CupertinoActionSheetAction implements Parcelable {

    private String action;

    private boolean defaultAction;

    private boolean destructiveAction;

    public static CupertinoActionSheetAction create(String action) {
        return new CupertinoActionSheetAction(action);
    }

    public CupertinoActionSheetAction(String action) {
        this(action, false, false);
    }

    public CupertinoActionSheetAction(String action, boolean defaultAction, boolean destructiveAction) {
        this.action = action;
        this.defaultAction = defaultAction;
        this.destructiveAction = destructiveAction;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
        dest.writeByte(this.defaultAction ? (byte) 1 : (byte) 0);
        dest.writeByte(this.destructiveAction ? (byte) 1 : (byte) 0);
    }

    protected CupertinoActionSheetAction(Parcel in) {
        this.action = in.readString();
        this.defaultAction = in.readByte() != 0;
        this.destructiveAction = in.readByte() != 0;
    }

    public static final Creator<CupertinoActionSheetAction> CREATOR = new Creator<CupertinoActionSheetAction>() {
        @Override
        public CupertinoActionSheetAction createFromParcel(Parcel source) {
            return new CupertinoActionSheetAction(source);
        }

        @Override
        public CupertinoActionSheetAction[] newArray(int size) {
            return new CupertinoActionSheetAction[size];
        }
    };
}
