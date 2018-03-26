# RBReader-Mupdf_library
将 RBReader 与Mupdf代码进行整合
1.界面更加美观 
      类似主流阅读器界面风格
2.引用更加方便 
      实现0代码加入
！！！！！！！！！！！！！！！！

曾经帅过的 发布者联系方式  2212709787（QQ）

FBReader全世界最大最好的交流群  321171877 （QQ->"FBReader"）

！！！！！！！！！！！！！！！！！！！！！

******             先将项目引入修改包名*** ******************后在按照下方步骤

修改包名：包路径为: org.geometerplus.android.fbreader.api; 

class类为：FBReaderIntents

修改类下变量  DEFAULT_PACKAGE 的值 改为主程序的包名


添加步骤

1.将 library下aar  放到项目libs下

2. compile(name: 'fBReader-release', ext: 'aar')

3.应用 AppApplication中 
  //初始化阅读器组件
        ZLAndroidApplication.init(this);
4.提供便捷方法
    public static void bookInit(Context context) {
        if (bs == null) {
            bs = new BookCollectionShadow();
            bs.bindToService(context, null);
        }
    }

    public void initBook(Context context, String filePath, String type) {
        if (bs == null) {
            bs = new BookCollectionShadow();
            bs.bindToService(context, null);
        }
        File file = new File(filePath);
        if (file.exists()) {
            Book book = bs.getBookByFile(filePath);
            if (type.equalsIgnoreCase("TXT") || type.equalsIgnoreCase("EPUB")) {
                //跳转阅读器
                FBReader.openBookActivity(context, book, null);
            } else if (type.equalsIgnoreCase("PDF")) {
                //跳转PDF阅读器
                Uri uri = Uri.parse(filePath);
                Intent intent = new Intent(context, MuPDFActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
    
