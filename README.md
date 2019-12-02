# RBReader-Mupdf_library
先上图-----------------------------------------------------------------
<div>
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/1.png" width="200" alt="PDF阅读" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/2.png" width="200" alt="功能界面1" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/3.png" width="200" alt="功能界面2" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/4.png" width="200" alt="护眼模式" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/5.png" width="200" alt="阅读进度" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/6.png" width="200" alt="默认阅读" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/7.png" width="200" alt="字体放大" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/8.png" width="200" alt="目录" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/9.png" width="200" alt="书签" />
<img src="https://github.com/lurongshuang/FBReader-Mupdf_library/blob/master/image/10.png" width="200" alt="添加书签" />
</div>
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

进行重新编译  gradle  markeJar

添加步骤

1.将 library下aar  放到项目libs下

2. compile(name: 'fBReader-release', ext: 'aar')

3.应用 AppApplication中 
  //初始化阅读器组件
        ZLAndroidApplication.init(this);
        
        
        
4.提供便捷方法

     //判断 服务是否可以使用了
    public static void bookInit(Context context) {
        if (bs == null) {
            bs = new BookCollectionShadow();
            bs.bindToService(context, null);
        }
    }
     
     //跳转到 阅读界面
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
    
