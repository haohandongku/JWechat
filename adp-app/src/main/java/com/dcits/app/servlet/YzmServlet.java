package com.dcits.app.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.app.constant.Constant;

/**
 * 验证码
 * 
 * @author zhongym
 * 
 */
@SuppressWarnings("serial")
public class YzmServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(YzmServlet.class);
	/** 宽度 */
	private int width = 100;
	/** 高度 */
	private int height = 38;
	/** 个数 */
	private int codeCount = 4;
	/** 干扰线 */
	private int lineCount = 20;
	/** 随机数 */
	Random random = new Random();

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		BufferedImage bufferedImage = null;
		try {
			int fontWidth = width / codeCount; // 字体宽度
			int fontHeight = height - 5; // 字体高度
			int codeY = height - 8;

			bufferedImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = bufferedImage.getGraphics();
			// 设置背景色
			g.setColor(getRandColor(200, 250));
			g.fillRect(0, 0, width, height);

			// 设置字体
			Font font = new Font("Gill Sans Ultra Bold", Font.PLAIN, fontHeight);
			g.setFont(font);

			// 设置干扰线
			for (int i = 0; i < lineCount; i++) {
				int xs = random.nextInt(width);
				int ys = random.nextInt(height);
				int xe = xs + random.nextInt(width);
				int ye = ys + random.nextInt(height);
				g.setColor(getRandColor(1, 255));
				g.drawLine(xs, ys, xe, ye);
			}

			// 添加噪点
			float yawpRate = 0.01f;
			int area = (int) (yawpRate * width * height);
			for (int i = 0; i < area; i++) {
				int x = random.nextInt(width);
				int y = random.nextInt(height);
				bufferedImage.setRGB(x, y, random.nextInt(255));
			}

			String randCode = getRandCode(codeCount);
			for (int i = 0; i < codeCount; i++) {
				String code = randCode.substring(i, i + 1);
				g.setColor(getRandColor(1, 255));
				g.drawString(code, i * fontWidth + 3, codeY);
			}

			String key = (String) request
					.getParameter(Constant.SESSION_VALIDCODE_KEY);
			request.getSession().setAttribute(key, randCode);
			// 设置响应的类型格式为图片格式
			response.setContentType("image/jpeg");
			// 禁止图像缓存。
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			ImageIO.write(bufferedImage, "png", response.getOutputStream());
		} catch (Exception e) {
			LOG.error("生成验证码时出现异常：", e);
		}
	}

	private String getRandCode(int n) {
		String base = "ABDEFHJKMNPRSTUVWXYZ23456789";
		String randCode = "";
		int len = base.length() - 1;
		double r;
		for (int i = 0; i < n; i++) {
			r = (Math.random()) * len;
			randCode = randCode + base.charAt((int) r);
		}
		return randCode;
	}

	private Color getRandColor(int fc, int bc) {
		if (fc > 255) {
			fc = 255;
		}
		if (bc > 255) {
			bc = 255;
		}
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

}