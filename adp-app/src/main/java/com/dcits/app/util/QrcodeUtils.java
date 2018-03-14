package com.dcits.app.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import com.dcits.app.data.DataObject;
import com.swetake.util.Qrcode;

public class QrcodeUtils {

	public static byte[] createQrcode(DataObject dataObject) throws Throwable {
		BufferedImage image = commonCreateQrcode(dataObject);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "jpeg", out);
		byte[] b = out.toByteArray();
		return b;
	}

	@SuppressWarnings("rawtypes")
	public static byte[] createQrcodeWithLogo(DataObject dataObject)
			throws Throwable {
		Map parameter = dataObject.getMap();
		BufferedImage image = commonCreateQrcode(dataObject);
		Graphics2D g = image.createGraphics();
		String logoPath = (String) parameter.get("logoPath");
		int logoWidth = Integer.parseInt((String) parameter.get("logoWidth"));
		int logoHeight = Integer.parseInt((String) parameter.get("logoHeight"));
		int centerX = image.getMinX() + image.getWidth() / 2 - logoWidth / 2;
		int centerY = image.getMinY() + image.getHeight() / 2 - logoHeight / 2;
		InputStream inputStream = QrcodeUtils.class
				.getResourceAsStream(logoPath);
		Image img = ImageIO.read(inputStream);
		g.drawImage(img, centerX, centerY, logoWidth, logoHeight, null);
		g.dispose();
		image.flush();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "jpeg", out);
		byte[] b = out.toByteArray();
		return b;
	}

	@SuppressWarnings("rawtypes")
	private static BufferedImage commonCreateQrcode(DataObject dataObject)
			throws Throwable {
		Map parameter = dataObject.getMap();
		String content = (String) parameter.get("CONTENT");
		int size = mm2px(Double.parseDouble((String) parameter.get("SIZE")));
		char errorC = 'L';
		char encodeM = 'B';
		int qrcodeV = 8;
		if (parameter.get("errorC") != null) {
			errorC = ((String) parameter.get("errorC")).charAt(0);
		}
		if (parameter.get("encodeM") != null) {
			encodeM = ((String) parameter.get("encodeM")).charAt(0);
		}
		if (parameter.get("qrcodeV") != null) {
			qrcodeV = Integer.parseInt((String) parameter.get("qrcodeV"));
		}
		Qrcode qrcode = new Qrcode();
		// 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小
		qrcode.setQrcodeErrorCorrect(errorC);
		qrcode.setQrcodeEncodeMode(encodeM);
		// 设置二维码版本号，取值范围1-40，值越大尺寸越大，可存储的信息越大
		qrcode.setQrcodeVersion(qrcodeV);
		// 图片尺寸
		int imgSize = size + 12 * (qrcodeV - 1);
		BufferedImage image = new BufferedImage(imgSize, imgSize,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, imgSize, imgSize);
		g.setColor(Color.BLACK);
		// 获得内容的字节数组，设置编码格式
		byte[] contentArr = content.getBytes("GBK");
		if (contentArr.length > 0 && contentArr.length < 800) {
			boolean[][] b = qrcode.calQrcode(contentArr);
			for (int i = 0; i < b.length; i++) {
				for (int j = 0; j < b.length; j++) {
					if (b[j][i]) {
						g.fillRect(j * 2 + 1, i * 2 + 1, 2, 2);
					}
				}
			}
		}
		g.dispose();
		image.flush();
		return image;
	}

	private static int mm2px(double mm) {
		return (int) (mm * 3.78);
	}

}
