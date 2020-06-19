# JavaCV_Demo
利用JavaCV处理视频流的demo

### 关于依赖包
想要图省事，直接依赖javacv-platform，如下
`<dependency>
     <groupId>org.bytedeco</groupId>
     <artifactId>javacv-platform</artifactId>
     <version>1.5.3</version>
 </dependency>`
 
 但是这样会导致一个问题，platform会把所有平台（windows、linux、android）的依赖包全部都导进来，这就导致最后导出的jar
包很大，有将近800M，所以要针对软件运行的平台对依赖进行裁剪,而裁剪之后的jar包容量约为80M左右。

不进行裁剪情况下的依赖长这样：
  ![](http://note.youdao.com/yws/public/resource/45db2199172e23053e5190861f72ae3b/xmlnote/WEBRESOURCE95ac40d18837a18aefe8330c93b6ed7b/4853)


从图中可以看出各平台的依赖jar均在其中。

下面开始对依赖进行裁剪，要想裁剪，首先需要了解为了整体功能的正常运行，我们需要JavaCV的哪些包：

① javacv.jar；   ② javacpp.jar；   ③ ffmpeg.jar；   ④ ffmpeg-系统平台.jar；   ⑤ opencv.jar；   ⑥ opencv-系统平台.jar   ⑦ openblas.jar  ⑧ openblas-系统平台.jar

利用maven的【classifier】指定系统平台，从而达到筛选的目的。以windows-86_64位系统为例，此平台上的依赖应该是这样的：

`<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv</artifactId>
    <version>1.5.3</version>
 </dependency>
 `
 
 `<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacpp</artifactId>
    <version>1.5.3</version>
  </dependency>
  <dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacpp</artifactId>
    <version>1.5.3</version>
    <classifier>windows-x86_64</classifier>
  </dependency>
 `
 
 `<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>ffmpeg</artifactId>
    <version>4.2.2-1.5.3</version>
  </dependency>
  <dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>ffmpeg</artifactId>
    <version>4.2.2-1.5.3</version>
    <classifier>windows-x86_64</classifier>
  </dependency>
 `
 
 `<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>opencv</artifactId>
    <version>4.3.0-1.5.3</version>
  </dependency>
  <dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>opencv</artifactId>
    <version>4.3.0-1.5.3</version>
    <classifier>windows-x86_64</classifier>
  </dependency>
 `
 
 `<dependency>
      <groupId>org.bytedeco</groupId>
      <artifactId>openblas</artifactId>
      <version>0.3.9-1.5.3</version>
  </dependency>
  <dependency>
      <groupId>org.bytedeco</groupId>
      <artifactId>openblas</artifactId>
      <version>0.3.9-1.5.3</version>
      <classifier>windows-x86_64</classifier>
  </dependency>
  `
 
 裁剪后的依赖目录如下所示：
  ![](http://note.youdao.com/yws/public/resource/45db2199172e23053e5190861f72ae3b/xmlnote/WEBRESOURCE61a936c953fb0fc379d943cb2f647705/4854)
  
 
 
 ### 注意事项
 录制视频最后一定要有recorder.close()的方法过程，否则生成的视频没有时长，并且无法在一般的播放器里面播放。
 
 关于BufferedImage、IplImage、Mat、Frame之间的转换，可以使用JavaCV自带类 Java2DFrameUtils提供的接口方法
 
 这里再记录一下开发过程中遇到的一个坑：当时开发的的系统平台是Linux-arm64,最开始选择的依赖是JavaCV-Platfrom 1.5版本，但是却无法正常运行，后来才发现1.5版本是不支持arm64平台的。升级到最新1.5.3版本，才支持arm64平台。
 