/*******************************************************************************
 * Copyright (c) 2020, 2020 Hirain Technologies Corporation.
 ******************************************************************************/
package com.alex.demo.javacv.demo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

/**
 * @Author Alex
 * @Created Dec May 7, 2020 6:32:48 PM
 * @Description
 *              <p>
 *              读取海康威视摄像头的视频流
 */
public class HikvisionReceiveTest {

	/**
	 * 海康威视摄像头rtsp取流地址
	 */
	private static String camerarRtspAddr = "rtsp://admin:admin12345@192.168.1.63:554/Streaming/Channels/103";

	private static String EasyDarwinRtspAddr = "rtsp://localhost:554/live";

	public static void main(String[] args) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		pullHikvisionRtsp(camerarRtspAddr, 25, EasyDarwinRtspAddr);
	}

	/**
	 * 拉取海康威视摄像头的rtsp视频流，并推送到EasyDarwin RTSP视频流服务器
	 * 
	 * @param cameraAddr
	 *            摄像头取流地址
	 * @param frameRate
	 *            帧速率
	 * @param videoServerRtspAddr
	 *            rtsp推流地址
	 * @throws Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	private static void pullHikvisionRtsp(String cameraAddr, double frameRate, String videoServerRtspAddr)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(cameraAddr);
		grabber.setFrameRate(frameRate);
		// grabber.setOption("stimeout", "5000000");
		// grabber.setOption("rtsp_transport", "tcp");
		// grabber.setVideoBitrate(3000000);
		// grabber.setImageHeight(480);
		// grabber.setImageWidth(640);
		// grabber.setVideoCodec(avcodec.AV_CODEC_ID_RAWVIDEO);
		// grabber.setFormat("flv");
		grabber.start();
		CanvasFrame canvas = new CanvasFrame("摄像头", CanvasFrame.getDefaultGamma() / grabber.getGamma());// 新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		Frame frame = null;

		frame = grabber.grab();
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();// 转换器
		IplImage grabbedImage = converter.convert(frame);// 抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加

		FrameRecorder recorder = FrameRecorder.createDefault(videoServerRtspAddr, grabbedImage.width(), grabbedImage.height());
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
		// recorder.setFormat("flv");// 封装格式，如果是推送到rtmp就必须是flv封装格式
		recorder.setFrameRate(frameRate);
		recorder.start();// 开启录制器

		long startTime = 0;
		long videoTS = 0;
		while (canvas.isVisible() && (frame = grabber.grab()) != null) {
			canvas.showImage(frame);// 获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab();
			byte[] photoData = convertFrame2Bytes(frame);
			// System.out.println(photoData.length);

			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			videoTS = 1000 * (System.currentTimeMillis() - startTime);
			recorder.setTimestamp(videoTS);
			recorder.record(frame);
			Thread.sleep(100);// 100毫秒刷新一次图像
		}
		canvas.dispose();
		recorder.stop();
		recorder.close();
		recorder.release();
		grabber.stop();
		grabber.close();
		grabber.release();
	}

	/**
	 * 将收取的Frame对象转换成byte[]
	 */
	private static byte[] convertFrame2Bytes(Frame frame) {
		Java2DFrameConverter java2dFrameConverter = new Java2DFrameConverter();
		BufferedImage bufferedImage = java2dFrameConverter.convert(frame);
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "jpg", bStream);// 可以是png或者jpg，png图片格式要大于jpg
		} catch (IOException e) {
			throw new RuntimeException("bugImg读取失败:" + e.getMessage(), e);
		}
		return bStream.toByteArray();
	}
}
