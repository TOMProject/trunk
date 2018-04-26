package com.station.common.chart;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImageUtils {
	private ImageUtils() {}

	/**
	 * 
	 * @param imgs  要合并的图片的绝对路径
	 * @param dst_pic 
	 * @return
	 */
	public static boolean mergeX(String[] imgs, String dst_pic) {
		// 获取需要拼接的图片长度
		int len = imgs.length;
		// 判断长度是否大于0
		if (len < 1) {
			return false;
		}
		File[] src = new File[len];
		BufferedImage[] images = new BufferedImage[len];
		int[][] ImageArrays = new int[len][];
		for (int i = 0; i < len; i++) {
			try {
				src[i] = new File(imgs[i]);
				images[i] = ImageIO.read(src[i]);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			int width = images[i].getWidth();
			int height = images[i].getHeight();
			// 从图片中读取RGB 像素
			ImageArrays[i] = new int[width * height];
			ImageArrays[i] = images[i].getRGB(0, 0, width, height, ImageArrays[i], 0, width);
		}

		int dst_height = 0;
		int dst_width = 0;
		// 合成图片像素
		for (int i = 0; i < images.length; i++) {
			dst_width += images[i].getWidth();
			dst_height = dst_height > images[i].getHeight() ? dst_height : images[i].getHeight();
		}
		// 合成后的图片
		System.out.println("宽度:" + dst_width + "高度:" + dst_height);
		if (dst_height < 1) {
			System.out.println("dst_height < 1");
			return false;
		}
		// 生成新图片
		try {
			BufferedImage imageNew = new BufferedImage(dst_width, dst_height, BufferedImage.TYPE_INT_RGB);
			int width_i = 0;
			for (int i = 0; i < images.length; i++) {
				imageNew.setRGB(width_i, 0, images[i].getWidth(), images[i].getHeight(), ImageArrays[i], 0,
						images[i].getWidth());
				width_i += images[i].getWidth();
			}

			File outFile = new File(dst_pic);
			ImageIO.write(imageNew, "png", outFile);// 写图片 ，输出到硬盘
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean mergeY(String[] imgs, String dst_pic) {
		// 获取需要拼接的图片长度
		int len = imgs.length;
		// 判断长度是否大于0
		if (len < 1) {
			return false;
		}
		File[] src = new File[len];
		BufferedImage[] images = new BufferedImage[len];
		int[][] ImageArrays = new int[len][];
		for (int i = 0; i < len; i++) {
			try {
				src[i] = new File(imgs[i]);
				images[i] = ImageIO.read(src[i]);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			int width = images[i].getWidth();
			int height = images[i].getHeight();
			// 从图片中读取RGB 像素
			ImageArrays[i] = new int[width * height];
			ImageArrays[i] = images[i].getRGB(0, 0, width, height, ImageArrays[i], 0, width);
		}

		int dst_height = 0;
		int dst_width = 0;
		// 合成图片像素
		for (int i = 0; i < images.length; i++) {
			dst_width = dst_width > images[i].getWidth() ? dst_width : images[i].getWidth();
			dst_height += images[i].getHeight();
		}
		// 合成后的图片
		System.out.println("宽度:" + dst_width + "高度:" + dst_height);
		if (dst_height < 1) {
			System.out.println("dst_height < 1");
			return false;
		}
		// 生成新图片
		try {
			BufferedImage imageNew = new BufferedImage(dst_width, dst_height, BufferedImage.TYPE_INT_RGB);
			int height_i = 0;
			for (int i = 0; i < images.length; i++) {
				imageNew.setRGB(0, height_i, images[i].getWidth(), images[i].getHeight(), ImageArrays[i], 0,
						images[i].getWidth());
				height_i += images[i].getHeight();
			}

			File outFile = new File(dst_pic);
			ImageIO.write(imageNew, "png", outFile);// 写图片 ，输出到硬盘
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
