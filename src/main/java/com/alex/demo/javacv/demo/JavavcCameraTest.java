package com.alex.demo.javacv.demo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.opencv.opencv_core.IplImage;

/**
 * @Author Alex
 * @Created Dec May 8, 2020 7:16:09 PM
 * @Description
 *              <p>
 *              调用本地摄像头，读取视频并存储或者推送到视频流服务器
 */

public class JavavcCameraTest {

	private static String outputPath = "rtmp://localhost:10085/hls/cLdxgl6WR?sign=cYObR_6Wgz";
	// private static String outputPath = "C:\\Users\\alex\\Desktop\\test.mp4";

	public static void main(String[] args) throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		recordCamera(outputPath, 25);
	}

	/**
	 * 按帧录制本机摄像头视频（边预览边录制，停止预览即停止录制）
	 * 
	 * @param outputFile
	 *            -录制的文件路径，也可以是rtsp或者rtmp等流媒体服务器发布地址
	 * @param frameRate
	 *            - 视频帧率
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	private static void recordCamera(String outputFile, double frameRate)
			throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		// FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(0);//无法用这个创建
		grabber.start(); // 开始获取摄像头数据
		CanvasFrame canvas = new CanvasFrame("摄像头", CanvasFrame.getDefaultGamma() / grabber.getGamma());// 新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		Frame frame = null;

		frame = grabber.grab();
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();// 转换器
		IplImage grabbedImage = converter.convert(frame);// 抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加

		FrameRecorder recorder = FrameRecorder.createDefault(outputPath, grabbedImage.width(), grabbedImage.height());
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
		recorder.setFormat("flv");// 封装格式，如果是推送到rtmp就必须是flv封装格式
		recorder.setFrameRate(frameRate);
		recorder.start();// 开启录制器

		long startTime = 0;
		long videoTS = 0;
		while (canvas.isVisible() && (frame = grabber.grabFrame()) != null) {
			canvas.showImage(frame);// 获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab();
			byte[] convertFrame2Bytes = convertFrame2Bytes(frame);
			// System.out.println(convertFrame2Bytes.length);

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
			ImageIO.write(bufferedImage, "png", bStream);// 可以是png或者jpg，png图片格式要大于jpg
		} catch (IOException e) {
			throw new RuntimeException("bugImg读取失败:" + e.getMessage(), e);
		}
		return bStream.toByteArray();
	}
}