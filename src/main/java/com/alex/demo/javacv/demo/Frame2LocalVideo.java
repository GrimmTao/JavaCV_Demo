/*******************************************************************************
 * Copyright (c) 2020, 2020 Alex.
 ******************************************************************************/
package com.alex.demo.javacv.demo;

import javax.swing.JFrame;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

/**
 * @Author Alex
 * @Created Dec 2020/6/2 16:26
 * @Description
 *              <p>
 *              将电脑摄像头/外接摄像头采集到的视频流信息录制成本地视频文件
 */
public class Frame2LocalVideo {

	// 以海康威视摄像头的rtsp地址为例
	private static String rtspAddr = "rtsp://admin:admin12345@192.168.1.63:554/Streaming/Channels/103";

	private static String videoPath = "C:/Users/alex/Desktop/testVideo.mp4";

	private static int i = 0;

	public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		avutil.av_log_set_level(avutil.AV_LOG_ERROR);// 设置JavaCV只显示error级别的log
		convertFrame2LocalVideo(rtspAddr, videoPath, 25);
	}

	private static void convertFrame2LocalVideo(String rtspAddr, String videoPath, int frameRate)
			throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		// OpenCVFrameGrabber grabber =JavaCVUtil.initComputerCameraGrabber();
		FFmpegFrameGrabber grabber = JavaCVUtil.initGrabber(rtspAddr, 25, 640, 480, avcodec.AV_CODEC_ID_H264, true);
		grabber.start(); // 开始获取摄像头数据
		CanvasFrame canvas = new CanvasFrame("摄像头", CanvasFrame.getDefaultGamma() / grabber.getGamma());// 新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);

		FrameRecorder recorder = JavaCVUtil.initRecorder(videoPath, frameRate, "mp4", 640, 480, grabber.getVideoCodec(), grabber.getAudioChannels(),
				grabber.getAudioCodec(), avutil.AV_PIX_FMT_YUV420P);
		recorder.start();// 开启录制器

		Frame frame = null;
		long startTime = 0;
		long videoTS = 0;
		while (canvas.isVisible() && i < 300) {
			if ((frame = grabber.grabFrame()) != null) {
				System.out.println(i++);// 这里是为退出while循环而做
				canvas.showImage(frame);// 获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab();
				if (startTime == 0)
					startTime = System.currentTimeMillis();
				videoTS = 1000 * (System.currentTimeMillis() - startTime);
				if (videoTS > recorder.getTimestamp()) {
					recorder.setTimestamp(videoTS);
				}
				// 推送视频帧信息到rtmp服务器
				recorder.record(frame);
			} else {
				grabber.close();
				grabber.start();
			}
		}
		// 若这里的recorder不执行一下close,则无法正常关闭录制的视频流，导致录制的视频不完整（即没有时长），一般的播放器没法播放。
		recorder.close();// close接口里面就包含了stop()和release()
		grabber.close();// close接口里面就包含了stop()和release()
		canvas.dispose();
	}

}
