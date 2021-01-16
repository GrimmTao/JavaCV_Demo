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
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * @Author Alex
 * @Created Dec 2020/6/2 16:00
 * @Description
 *              <p>
 *              将电脑摄像头/外接摄像头采集到的视频流信息推送到RTMP视频流服务器
 */
public class Frame2RtmpServer {

	// 以海康威视摄像头的rtsp地址为例
	private static String rtspAddr = "rtsp://admin:admin12345@192.168.1.63:554/Streaming/Channels/103";

	private static String rtmpAddr = "rtmp://localhost:10085/hls/cLdxgl6WR?sign=cYObR_6Wgz";

	public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		avutil.av_log_set_level(avutil.AV_LOG_ERROR);// 设置JavaCV只显示error级别的log
		pullCamera2RtmpServer(rtmpAddr, 25);
		// pullRtsp2RtmpServer(rtspAddr, rtmpAddr, 25);
	}

	/**
	 * 拉取笔记本摄像头数据并推送到rtmp服务器
	 * 
	 * @param rtmpAddr
	 *            rtmp地址
	 * @param frameRate
	 *            帧率
	 * @throws FrameGrabber.Exception
	 * @throws InterruptedException
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	private static void pullCamera2RtmpServer(String rtmpAddr, int frameRate)
			throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		OpenCVFrameGrabber grabber = JavaCVUtil.initComputerCameraGrabber();
		grabber.start(); // 开始获取摄像头数据
		CanvasFrame canvas = new CanvasFrame("摄像头", CanvasFrame.getDefaultGamma() / grabber.getGamma());// 新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);

		FrameRecorder recorder = JavaCVUtil.initRecorder(rtmpAddr, 20, "flv", 640, 480, avcodec.AV_CODEC_ID_H264, grabber.getAudioChannels(),
				grabber.getAudioCodec(), avutil.AV_PIX_FMT_YUV420P);
		recorder.start();// 开启录制器

		Frame frame = null;
		long startTime = 0;
		long videoTS = 0;
		while (canvas.isVisible()) {
			if ((frame = grabber.grabFrame()) != null) {
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
		recorder.close();// close接口里面就包含了stop()和release()
		grabber.close();// close接口里面就包含了stop()和release()
		canvas.dispose();
	}

	/**
	 * 拉取海康威视摄像头数据并推送到rtmp服务器
	 * 
	 * @param rtspAddr
	 *            摄像头地址
	 * @param rtmpAddr
	 *            rtmp地址
	 * @param frameRate
	 *            帧率
	 * @throws FrameGrabber.Exception
	 * @throws InterruptedException
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	private static void pullRtsp2RtmpServer(String rtspAddr, String rtmpAddr, int frameRate)
			throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		FFmpegFrameGrabber grabber = JavaCVUtil.initGrabber(rtspAddr, 25, 640, 480, avcodec.AV_CODEC_ID_H264, true);
		grabber.start(); // 开始获取摄像头数据
		CanvasFrame canvas = new CanvasFrame("摄像头", CanvasFrame.getDefaultGamma() / grabber.getGamma());// 新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);

		FrameRecorder recorder = JavaCVUtil.initRecorder(rtmpAddr, 20, "flv", 640, 480, grabber.getVideoCodec(), grabber.getAudioChannels(),
				grabber.getAudioCodec(), avutil.AV_PIX_FMT_YUV420P);
		recorder.start();// 开启录制器

		Frame frame = null;
		long startTime = 0;
		long videoTS = 0;
		while (canvas.isVisible()) {
			if ((frame = grabber.grabFrame()) != null) {
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
		recorder.close();// close接口里面就包含了stop()和release()
		grabber.close();// close接口里面就包含了stop()和release()
		canvas.dispose();
	}
}
