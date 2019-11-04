## Cupertino Dialog

IOS Style Dialog: AlertDialog, ActionSheetDialog

## 效果

![CupertinoDialog](/screenshots/screenshots.jpg)

## 使用方法

### 添加依赖

添加仓库到根build.gradle文件及库依赖
``` gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	implementation 'com.github.wangmingshuo:cupertinodialog:1.0.0'
}
```
### 使用

- 使用CupertinoAlertDialog(kotlin)

``` kotlin
val actions = ArrayList<String>()
actions.add("test1")
actions.add("test2")
actions.add("test3")

CupertinoAlertDialog.newInstance("title", "message", actions)
	.show(supportFragmentManager, "alert")
```

- 使用CupertinoActionSheetDialog(kotlin)

``` kotlin
val actions = ArrayList<CupertinoActionSheetAction>()
actions.add(CupertinoActionSheetAction.create("test1"))
actions.add(CupertinoActionSheetAction.create("test2"))
actions.add(CupertinoActionSheetAction.create("test3"))

val cancelAction = CupertinoActionSheetAction("取消")
cancelAction.isDefaultAction = true

CupertinoActionSheetDialog.newInstance("title", "message", actions, cancelAction)
	.show(supportFragmentManager, "sheet")
});
```
## Thanks

[BlurView](https://github.com/Dimezis/BlurView) 
