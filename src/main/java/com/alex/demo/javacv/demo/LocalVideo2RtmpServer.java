/*******************************************************************************
 * Copyright (c) 2021, 2021 Hirain Technologies Corporation.
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
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.IplImage;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author Alex
 * @Created Dec 2020/6/4 11:59
 * @Description
 *              <p>
 *              读取本地视频文件推送到RTMP视频流服务器(RTMPServer选用EasyDSS)
 */
@Slf4j
public class LocalVideo2RtmpServer {

	private static String localVideoPath = "C:\\Users\\Grimm\\Desktop\\videoTest.mp4";
	// C:/Users/alex/Desktop/testVideo.mp4

	private static String rtmpServer = "rtmp://localhost:10035/hls/aBYhrSBGR?sign=MfY29IBMgz";

	public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		avutil.av_log_set_level(avutil.AV_LOG_ERROR);// 设置JavaCV只显示error级别的log
		pullLocalVideo2RtmpServer(localVideoPath, rtmpServer, 30);
		// parseLocalVideoAndPlay(localVideoPath, 25);
	}

	/**
	 * 解析本地视频文件数据并推送到rtmp服务器
	 * 
	 * @param localVideoPath
	 *            本地视频文件地址
	 * @param rtmpAddr
	 *            rtmp地址
	 * @param frameRate
	 *            帧率
	 */
	private static void pullLocalVideo2RtmpServer(String localVideoPath, String rtmpAddr, int frameRate)
			throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		FFmpegFrameGrabber grabber = JavaCVUtil.initGrabber(localVideoPath, frameRate, 852, 480, avcodec.AV_CODEC_ID_H264, false);
		grabber.start();// 开始解析视频文件，获取帧数据

		FrameRecorder recorder = JavaCVUtil.initRecorder(rtmpAddr, frameRate, "flv", 852, 480, grabber.getVideoCodec(), grabber.getAudioCodec(),
				grabber.getAudioChannels(), avutil.AV_PIX_FMT_YUV420P);
		recorder.start();// 开启录制器

		Frame frame = null;
		long startTime = 0;
		long videoTS = 0;
		while (true) {
			if ((frame = grabber.grabFrame()) != null) {
				if (startTime == 0)
					startTime = System.currentTimeMillis();
				videoTS = 1000 * (System.currentTimeMillis() - startTime);
				if (videoTS > recorder.getTimestamp()) {
					recorder.setTimestamp(videoTS);
				}
				// 推送视频帧信息到rtmp服务器
				recorder.record(frame);
			} else {
				log.info("video over");
				grabber.close();
				grabber.start();
				log.info("grabber restart");
			}
		}
	}

	/**
	 * 解析本地文件并在打开窗口中播放出来(循环播放)
	 * 
	 * @param localVideoPath
	 * @param frameRate
	 */
	private static void parseLocalVideoAndPlay(String localVideoPath, int frameRate)
			throws FrameGrabber.Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		FFmpegFrameGrabber grabber = JavaCVUtil.initGrabber(localVideoPath, frameRate, 852, 480, avcodec.AV_CODEC_ID_H264, false);
		grabber.start(); // 开始获取摄像头数据

		// 一个opencv视频帧转换器
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		Frame grabframe = grabber.grab();
		IplImage grabbedImage = null;
		if (grabframe != null) {
			log.info("取到第一帧");
			grabbedImage = converter.convert(grabframe);
		} else {
			log.warn("没有取到第一帧");
		}
		// 如果想要保存图片,可以使用 opencv_imgcodecs.cvSaveImage("hello.jpg", grabbedImage);来保存图片

		CanvasFrame canvas = new CanvasFrame("Video", CanvasFrame.getDefaultGamma() / grabber.getGamma());// 新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);

		Frame frame = null;
		while (canvas.isVisible()) {
			if ((frame = grabber.grabFrame()) != null) {
				canvas.showImage(frame);// 获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab();
			} else {
				log.info("video over");
				grabber.close();
				grabber.start();
				log.info("grabber restart");
			}
		}
		canvas.dispose();
	}

}
