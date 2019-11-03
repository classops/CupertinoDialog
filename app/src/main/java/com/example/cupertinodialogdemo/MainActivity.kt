package com.example.cupertinodialogdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hanter.android.radwidget.cupertino.CupertinoActionSheetAction
import com.hanter.android.radwidget.cupertino.CupertinoActionSheetDialog
import com.hanter.android.radwidget.cupertino.CupertinoAlertDialog
import com.hanter.android.radwidget.cupertino.CupertinoAlertDialogAction
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnAlert -> {
                val actions = ArrayList<CupertinoAlertDialogAction>()
                actions.add(CupertinoAlertDialogAction("test1", false, true))
                actions.add(CupertinoAlertDialogAction("test2", R.style.CupertinoAlertDialogAction_Text_Green, false, false))

                CupertinoAlertDialog.newInstance("title", "", actions)
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
        }
    }

}
