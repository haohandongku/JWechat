package com.dcits.app.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

import com.dcits.app.constant.Constant;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.app.util.JacksonUtils;

@SuppressWarnings("serial")
public class GraphicalServlet extends HttpServlet {
	private static final Log LOG = LogFactory.getLog(GraphicalServlet.class);
	
	/** 宽度 */
	private int width = 160;
	/** 高度 */
	private int height = 32;
	/** 个数 */
	private int codeCount = 11;
	/** 随机数 */
	Random random = new Random();
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings({ "rawtypes" })
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String serviceName = (String) request
				.getParameter(Constant.SERVICE_NAME);
		String methodName = (String) request.getParameter(Constant.METHOD_NAME);
		String requestJson = (String) request
				.getParameter(Constant.REQUEST_JSON);
		try {
			//获取需要图形化显示的字符串
			Map parameter = JacksonUtils.getMapFromJson(requestJson);
			if (parameter.get("width") != null) {
				width = (Integer) parameter.get("width");
			}
			if (parameter.get("height") != null) {
				height = (Integer) parameter.get("height");
			}
			DataObject dataObject = new DataObject(parameter);
			Object service = ApplicationContextUtils.getContext().getBean(
					serviceName);
			Method method = ReflectionUtils.findMethod(service.getClass(),
					methodName, new Class[] { DataObject.class });
			DataObject result = (DataObject) ReflectionUtils.invokeMethod(
					method, service, dataObject);
			String graphicalString = (String) result.getMap().get("graphicalString");
			if(StringUtils.isBlank(graphicalString) || "null".equals(graphicalString)) {
				graphicalString = "暂无信息";
			}
			codeCount = graphicalString.length();
			
			//字符串图形化处理
			BufferedImage bufferedImage = null;
			int fontWidth = width / codeCount; // 字体宽度
			int fontHeight = height - 5; // 字体高度
			int codeY = height - 8;

			bufferedImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = bufferedImage.getGraphics();
			// 设置背景色
			//g.setColor(getRandColor(200, 250));
			g.setColor(new Color(255,255,255));
			g.fillRect(0, 0, width, height);

			// 设置字体
			Font font = new Font("Gill Sans Ultra Bold", Font.ITALIC, fontHeight);
			g.setFont(font);

			for (int i = 0; i < codeCount; i++) {
				String code = graphicalString.substring(i, i + 1);
				//g.setColor(getRandColor(1, 255));
				g.setColor(new Color(255,0,0));
				g.drawString(code, i * (fontWidth), codeY);
			}

			// 设置响应的类型格式为图片格式
			response.setContentType("image/jpeg");
			// 禁止图像缓存。
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			ImageIO.write(bufferedImage, "png", response.getOutputStream());
			
		} catch (Throwable e) {
			LOG.error("图形化字符串时出现异常：", e);
		}
	}
	
	@SuppressWarnings("unused")
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
