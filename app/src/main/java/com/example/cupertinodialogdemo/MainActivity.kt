package com.example.cupertinodialogdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hanter.android.radwidget.cupertino.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener, CupertinoAlertDialog.OnActionClickListener,
    CupertinoActionSheetDialog.OnActionClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnAlert -> {
                val actions = ArrayList<CupertinoAlertDialogAction>()
                actions.add(CupertinoAlertDialogAction("Delete", false, true))
                actions.add(CupertinoAlertDialogAction("Cancel", false, false))

                CupertinoAlertDialog.newInstance("Delete file?", "", actions)
                    .show(supportFragmentManager, "TEST")
            }

            R.id.btnAlertWithTitle -> {
                val actions = ArrayList<String>()
                actions.add("Don\'t Allow")
                actions.add("Allow")

                CupertinoAlertDialog.newInstance("Allow \"Maps\" to access your location while you are using the app?",
                    "Your current location will be displayed on the map and used for directions, nearby search results, and estimated travel times.",
                    actions)
                    .show(supportFragmentManager, "TEST")
            }

            R.id.btnAlertWithButton -> {
                val actions = ArrayList<String>()
                actions.add("test1")
                actions.add("test2")
                actions.add("test3")
                actions.add("test4")

                CupertinoAlertDialog.newInstance("title", "message", actions)
                    .show(supportFragmentManager, "TEST")
            }

            R.id.btnAlertButtonOnly -> {
                val actions = ArrayList<String>()
                actions.add("test1")
                actions.add("test2")
                actions.add("test3")
                actions.add("test4")

                CupertinoAlertDialog.newInstance("", "", actions)
                    .show(supportFragmentManager, "TEST")
            }

            R.id.btnActionSheet -> {
                val actions = ArrayList<CupertinoActionSheetAction>()
                actions.add(CupertinoActionSheetAction.create("test1"))
                actions.add(CupertinoActionSheetAction.create("test2"))
                actions.add(CupertinoActionSheetAction.create("test3"))

                val cancelAction = CupertinoActionSheetAction("取消")
                cancelAction.isDefaultAction = true

                CupertinoActionSheetDialog.newInstance("title", "message", actions, cancelAction)
                    .show(supportFragmentManager, "TEST")
            }

            R.id.btnWechatActionSheet -> {
                val actions = ArrayList<CupertinoActionSheetAction>()
                actions.add(CupertinoActionSheetAction.create("取消"))
                actions.add(CupertinoActionSheetAction.create("取消"))
                actions.add(CupertinoActionSheetAction.create("取消"))

                val cancelAction = CupertinoActionSheetAction("取消")
                WechatActionSheetDialog.newInstance(null, null, actions, cancelAction)
                    .show(supportFragmentManager, "TEST")
            }
        }
    }

    override fun onActionClick(dialog: CupertinoAlertDialog?, position: Int) {
        dialog?.dismiss()
    }

    override fun onActionClick(dialog: CupertinoActionSheetDialog?, position: Int) {
        dialog?.dismiss()
    }
}
