/*******************************************************************************
 * Copyright (c) 2020, 2020 Alex.
 ******************************************************************************/
package com.alex.demo.javacv.demo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * @Author Alex
 * @Created Dec 2020/6/4 11:59
 * @Description
 *              <p>
 *              关于BufferedImage、IplImage、Mat、Frame之间的转换，可以使用JavaCV自带类 Java2DFrameUtils提供的接口方法
 */
public class JavaCVUtil {

	/**
	 * 初始化外接摄像头视频信息的抓取器
	 *
	 * @param rtspAddr
	 *            外接摄像头视频流地址
	 * @param frameRate
	 *            抓取帧率
	 * @param iamgeWidth
	 *            抓取图片宽度
	 * @param imageHeight
	 *            抓取图片高度
	 * @return
	 * @throws FrameGrabber.Exception
	 */
	public static FFmpegFrameGrabber initGrabber(String rtspAddr, int frameRate, int iamgeWidth, int imageHeight) throws FrameGrabber.Exception {
		FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(rtspAddr);
		grabber.setFrameRate(frameRate);
		grabber.setImageWidth(iamgeWidth);
		grabber.setImageHeight(imageHeight);
		// 以下两个属性的设置，只有在视频源是外接摄像头时才开放，若是直接获取笔记本自带摄像头的视频信息，不能要以下两个属性
		grabber.setOption("rtsp_transport", "tcp"); // 使用tcp的方式，不然会丢包很严重
		grabber.setOption("stimoout", "5000000");// 增加超时参数
		grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		return grabber;
	}

	/**
	 * 初始化抓取笔记本自带摄像头视频信息的抓取器
	 *
	 * @return
	 */
	public static OpenCVFrameGrabber initComputerCameraGrabber() {
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);// 抓取计算机摄像头视频
		grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);// 一般都采用h264编码方式
		return grabber;
	}

	/**
	 * 初始化一个视频推送器
	 * 
	 * @param ouputPath
	 *            视频推送地址，可以是本地视频文件地址，也可以是rtmp视频流服务器地址
	 * @param frameRate
	 *            推送帧率
	 * @param width
	 *            推送的帧宽度
	 * @param heigth
	 *            推送的帧高度
	 * @param format
	 *            推送的视频格式，可以是mp4,可以是flv,如果要推送到rtmp地址，则必须是flv格式
	 * @return
	 * @throws FrameRecorder.Exception
	 */
	public static FrameRecorder initRecorder(String ouputPath, int frameRate, int width, int heigth, String format) throws FrameRecorder.Exception {
		FrameRecorder recorder = FrameRecorder.createDefault(ouputPath, width, heigth);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
		recorder.setFormat(format);
		recorder.setFrameRate(frameRate);
		recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);// 设置像素
		return recorder;
	}

	/**
	 * 将收取的Frame对象转换成byte[]
	 *
	 * @param frame
	 *            帧
	 * @param format
	 *            图片格式:jpg或者png ,jpg的大小要比png小
	 * @return
	 */
	public static byte[] convertFrame2Bytes(Frame frame, String format) {
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

	/**
	 * 将bufferedImage转换成本地图片
	 * 
	 * @param formatName
	 *            图片格式 ，jpg或者png
	 * @throws IOException
	 */
	public static void writeImageFile(BufferedImage img, String imgPath, String formatName) throws IOException {
		File outputfile = new File(imgPath);
		ImageIO.write(img, formatName, outputfile);
	}

	/**
	 * BufferImage转byte[]
	 * 
	 * @param formatName
	 *            图片格式，jpg或者png
	 */
	public static byte[] bufImg2Bytes(BufferedImage original, String formatName) {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(original, formatName, bStream);
		} catch (IOException e) {
			throw new RuntimeException("bugImg读取失败:" + e.getMessage(), e);
		}
		return bStream.toByteArray();
	}

	/**
	 * byte[]转BufferImage
	 * 
	 * @param imgBytes
	 * @return
	 */
	public static BufferedImage bytes2bufImg(byte[] imgBytes) {
		BufferedImage tagImg = null;
		try {
			tagImg = ImageIO.read(new ByteArrayInputStream(imgBytes));
			return tagImg;
		} catch (IOException e) {
			throw new RuntimeException("bugImg写入失败:" + e.getMessage(), e);
		}
	}

	/**
	 * BufferedImage 转 mat
	 * 
	 * @param original
	 * @return
	 */
	public static Mat bufImg2Mat(BufferedImage original) {
		OpenCVFrameConverter.ToMat openCVConverter = new OpenCVFrameConverter.ToMat();
		Java2DFrameConverter java2DConverter = new Java2DFrameConverter();
		Mat mat = openCVConverter.convert(java2DConverter.convert(original));
		return mat;
	}
}
