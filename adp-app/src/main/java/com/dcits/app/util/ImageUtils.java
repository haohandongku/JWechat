package com.dcits.app.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import sun.misc.BASE64Decoder;

import com.dcits.app.exception.BizRuntimeException;

public class ImageUtils {

	public static byte[] getImage(String data) {
		try {
			data = data.substring("data:image/jpeg;base64,".length());
			return new BASE64Decoder().decodeBuffer(data);
		} catch (IOException e) {

		}
		return null;
	}

	/**
	 * 缩放图像（按高度和宽度缩放）
	 * 
	 * @param srcImageFile
	 *            源图像文件地址
	 * @param result
	 *            缩放后的图像地址
	 * @param height
	 *            缩放后的高度
	 * @param width
	 *            缩放后的宽度
	 * @param bb
	 *            比例不对时是否需要补白：true为补白; false为不补白;
	 * @throws IOException
	 */
	public final static ByteArrayOutputStream scale(InputStream inputstream,
			int width, int height, boolean bb) throws IOException {
		ByteArrayOutputStream outimage = new ByteArrayOutputStream();
		ImageInputStream iis = null;
		ImageReader reader = null;
		Graphics2D g = null;
		try {
			// 读取源图像
			iis = ImageIO.createImageInputStream(inputstream);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (!readers.hasNext()) {
				throw new BizRuntimeException("No readers found!");
			}
			reader = readers.next();
			reader.setInput(iis, true);

			int srcWidth = reader.getWidth(0); // 源图宽度
			int srcHeight = reader.getHeight(0); // 源图高度
			// 缩放
			// 计算缩放比例
			double ratio = 1.0; // 缩放比例
			if (srcHeight > height || srcWidth > width) {
				double sh = (new Integer(srcHeight)).doubleValue() / srcWidth, sw = (new Integer(
						height)).doubleValue() / width;
				if (sh < sw) {
					ratio = (new Integer(width)).doubleValue() / srcWidth;
				} else {
					ratio = (new Integer(height)).doubleValue() / srcHeight;
				}
			}
			int realWidth = ((Long) Math.round(ratio * srcWidth)).intValue();// 压缩后宽度
			int realHeight = ((Long) Math.round(ratio * srcHeight)).intValue();// 压缩后高度
			BufferedImage tagImage = new BufferedImage(width, height,
					BufferedImage.SCALE_SMOOTH);
			g = tagImage.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);

			g.drawImage(
					reader.read(0).getScaledInstance(realWidth, realHeight,
							Image.SCALE_SMOOTH),
					Math.round((width - realWidth) / 2),
					Math.round((height - realHeight) / 2), realWidth,
					realHeight, Color.white, null); // 绘制缩小后的图
			ImageIO.write(tagImage, "JPEG", outimage);
		} finally {
			if (reader != null) {
				reader.dispose();
			}
			if (g != null) {
				g.dispose();
			}
			if (iis != null) {
				iis.close();
			}
		}
		return outimage;
	}

	/**
	 * @param bytes
	 * @param x
	 *            截取坐标
	 * @param y
	 *            截取坐标
	 * @param w
	 *            截取宽度
	 * @param h
	 *            截取高度
	 * @param width
	 *            压缩后宽度
	 * @param height
	 *            压缩后高度
	 * @return
	 * @throws Exception
	 */
	public static byte[] cutAndScale(byte[] bytes, int x, int y, int w, int h,
			int width, int height) throws Exception {
		ByteArrayOutputStream outimage = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ImageInputStream iis = null;
		byte[] imagebytes;
		try {
			// 读取源图像
			Iterator<ImageReader> readers = ImageIO
					.getImageReadersByFormatName("jpg");
			ImageReader reader = readers.next();
			iis = ImageIO.createImageInputStream(bis);
			reader.setInput(iis, true);

			int srcWidth = reader.getWidth(0); // 源图宽度
			int srcHeight = reader.getHeight(0); // 源图高度
			// 截取
			ImageReadParam param = reader.getDefaultReadParam();
			Rectangle rect = new Rectangle(x, y, w, h);
			param.setSourceRegion(rect);
			BufferedImage tag = reader.read(0, param);
			srcWidth = w; // 截取图宽度
			srcHeight = h; // 截取图高度
			// 缩放
			// 计算缩放比例
			double ratio = 0.0; // 缩放比例
			double sh = (new Integer(srcHeight)).doubleValue() / srcWidth, sw = (new Integer(
					height)).doubleValue() / width;
			if (sh < sw) {
				ratio = (new Integer(width)).doubleValue() / srcWidth;
			} else {
				ratio = (new Integer(height)).doubleValue() / srcHeight;
			}
			int realWidth = ((Long) Math.round(ratio * srcWidth)).intValue();// 压缩后宽度
			int realHeight = ((Long) Math.round(ratio * srcHeight)).intValue();// 压缩后高度
			BufferedImage tagImage = new BufferedImage(width, height,
					BufferedImage.SCALE_SMOOTH);
			Graphics2D g = tagImage.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);
			g.drawImage(tag.getScaledInstance(realWidth, realHeight,
					Image.SCALE_SMOOTH), Math.round((width - realWidth) / 2),
					Math.round((height - realHeight) / 2), realWidth,
					realHeight, Color.white, null); // 绘制缩小后的图
			g.dispose();
			ImageIO.write(tagImage, "JPEG", outimage);
			imagebytes = outimage.toByteArray();
		} finally {
			if (iis != null) {
				iis.close();
			}
			if (bis != null) {
				bis.close();
			}
			if (outimage != null) {
				outimage.flush();
				outimage.close();
			}
		}
		return imagebytes;
	}

	/**
	 * @param bytes
	 * @param x
	 *            截取坐标
	 * @param y
	 *            截取坐标
	 * @param w
	 *            截取宽度
	 * @param h
	 *            截取高度
	 * @return
	 * @throws Exception
	 */
	public static byte[] cut(byte[] bytes, int x, int y, int w, int h)
			throws Exception {
		ByteArrayOutputStream outimage = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ImageInputStream iis = null;
		byte[] imagebytes = null;
		try {
			// 读取源图像
			Iterator<ImageReader> readers = ImageIO
					.getImageReadersByFormatName("jpg");
			ImageReader reader = readers.next();
			iis = ImageIO.createImageInputStream(bis);
			reader.setInput(iis, true);
			// 截取
			ImageReadParam param = reader.getDefaultReadParam();
			Rectangle rect = new Rectangle(x, y, w, h);
			param.setSourceRegion(rect);
			BufferedImage tag = reader.read(0, param);
			ImageIO.write(tag, "JPEG", outimage);
			imagebytes = outimage.toByteArray();
		} finally {
			if (iis != null) {
				iis.close();
			}
			if (bis != null) {
				bis.close();
			}
			if (outimage != null) {
				outimage.flush();
				outimage.close();
			}
		}
		return imagebytes;
	}

}