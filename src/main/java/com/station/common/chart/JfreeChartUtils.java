package com.station.common.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLabelLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Component;

import com.station.common.utils.ReflectUtil;
import com.station.moudles.entity.CellInfoDetail;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.PackDataExpandLatest;
import com.station.moudles.entity.PackDataInfo;

@Component
public class JfreeChartUtils {

	public static int small_width = 308;
	public static int small_heigh = 240;

	public static int middle_width = 410;
	public static int middle_heigh = 330;

	public static int large_width = 960;
	public static int large_heigh = 330;

	private int def_width = middle_width;
	private int def_heigh = middle_heigh;
	private String yLabel;
	private String xLabel;
	private String title;
	private String annotation;

	private final static String CELL_RESIST_PREFIX = "cellResist";
	private final static String CELL_CAP_PREFIX = "cellCap";
	private final static String CELL_CAP_SORT_PREFIX = "cellCapSort";
	private final static String CELL_VOL_PREFIX = "cellVol";

	private static Font FONT = new Font("黑体", Font.PLAIN, 12);
	public static Color[] CHART_COLORS = { new Color(31, 129, 188), new Color(92, 92, 97), new Color(144, 237, 125),
			new Color(255, 188, 117), new Color(153, 158, 255), new Color(255, 117, 153), new Color(253, 236, 109),
			new Color(128, 133, 232), new Color(158, 90, 102), new Color(255, 204, 102) };// 颜色

	static {
		setChartTheme();
	}

	private JfreeChartUtils() {
	}
	
	public static JfreeChartUtils newInstance() {
		return new JfreeChartUtils();
	}
	
	/**
	 * 中文主题样式 解决乱码
	 */
	public static void setChartTheme() {
		// 设置中文主题样式 解决乱码
		StandardChartTheme chartTheme = new StandardChartTheme("CN");
		// 设置标题字体
		chartTheme.setExtraLargeFont(FONT);
		// 设置图例的字体
		chartTheme.setRegularFont(FONT);
		// 设置轴向的字体
		chartTheme.setLargeFont(FONT);
		chartTheme.setSmallFont(FONT);
		chartTheme.setTitlePaint(new Color(51, 51, 51));
		chartTheme.setSubtitlePaint(new Color(85, 85, 85));

		chartTheme.setLegendBackgroundPaint(Color.WHITE);// 设置标注
		chartTheme.setLegendItemPaint(Color.BLACK);//
		chartTheme.setChartBackgroundPaint(Color.WHITE);
		// 绘制颜色绘制颜色.轮廓供应商
		// paintSequence,outlinePaintSequence,strokeSequence,outlineStrokeSequence,shapeSequence

		Paint[] OUTLINE_PAINT_SEQUENCE = new Paint[] { Color.WHITE };
		// 绘制器颜色源
		DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(CHART_COLORS, CHART_COLORS,
				OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
		chartTheme.setDrawingSupplier(drawingSupplier);

		chartTheme.setPlotBackgroundPaint(Color.WHITE);// 绘制区域
		chartTheme.setPlotOutlinePaint(Color.BLACK);// 绘制区域外边框
		chartTheme.setLabelLinkPaint(new Color(8, 55, 114));// 链接标签颜色
		chartTheme.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);

		chartTheme.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		chartTheme.setDomainGridlinePaint(new Color(192, 208, 224));// X坐标轴垂直网格颜色
		chartTheme.setRangeGridlinePaint(new Color(192, 192, 192));// Y坐标轴水平网格颜色

		chartTheme.setBaselinePaint(Color.WHITE);
		chartTheme.setCrosshairPaint(Color.BLUE);// 不确定含义
		chartTheme.setAxisLabelPaint(new Color(51, 51, 51));// 坐标轴标题文字颜色
		chartTheme.setTickLabelPaint(new Color(67, 67, 72));// 刻度数字
		chartTheme.setBarPainter(new StandardBarPainter());// 设置柱状图渲染
		chartTheme.setXYBarPainter(new StandardXYBarPainter());// XYBar 渲染

		chartTheme.setItemLabelPaint(Color.black);
		chartTheme.setThermometerPaint(Color.white);// 温度计
		
		ChartFactory.setChartTheme(chartTheme);
	}

	public String generateTimeSeriesChart(TimeSeriesCollection dataset, String srcPath, String fileName) {
		if (StringUtils.isEmpty(srcPath)) {
			return null;
		}
		try {
			JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xLabel, yLabel, dataset, false, true, false);

			XYPlot xyPlot = (XYPlot) chart.getPlot();
			xyPlot.setRangeGridlinesVisible(false);// 不显示绘图区域网格
			xyPlot.setDomainGridlinesVisible(false);

			// 获取显示线条的对象
			XYLineAndShapeRenderer xyShape = (XYLineAndShapeRenderer) xyPlot.getRenderer();

			xyShape.setDefaultStroke(new BasicStroke(0.5f)); // 设置线宽
			xyShape.setSeriesPaint(0, new Color(162, 203, 231));// 设置线的颜色

			// 设置拐点
			xyShape.setSeriesPaint(1, new Color(0, 0, 0, 0));
			xyShape.setSeriesShapesVisible(1, true);

			xyShape.setSeriesFillPaint(1, Color.RED);// 设置拐点颜色
			xyShape.setUseFillPaint(true);// 设置false拐点颜色设置不生效
			xyShape.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-2.5D, -2.5D, 5D, 5D));// 设置拐点大小

			// 设置显示值
			xyShape.setSeriesItemLabelsVisible(1, true);
			xyShape.setSeriesItemLabelGenerator(1, new StandardXYItemLabelGenerator());
			xyShape.setSeriesItemLabelFont(1, new Font("Dialog", 1, 10));
			xyShape.setSeriesPositiveItemLabelPosition(1,
					new ItemLabelPosition(ItemLabelAnchor.INSIDE1, TextAnchor.TOP_LEFT));

			// y 轴
			NumberAxis numberaxis = (NumberAxis) xyPlot.getRangeAxis();
			// numberaxis.setRange(42, 58);
			// numberaxis.setTickUnit(new NumberTickUnit(2)); // 间隔为2
			numberaxis.setLowerMargin(0.08);
			numberaxis.setUpperMargin(0.06);
			
			// x 轴
			DateAxis dateAxis = (DateAxis) xyPlot.getDomainAxis();
			dateAxis.setLabelLocation(AxisLabelLocation.HIGH_END);
			dateAxis.setLowerMargin(0.06);
			dateAxis.setUpperMargin(0.06);
			/*
			 * SimpleDateFormat frm = new SimpleDateFormat("HH:mm"); int count =
			 * 30;//间隔为30分钟 dateAxis.setTickUnit(new
			 * DateTickUnit(DateTickUnitType.MINUTE,count,frm));
			 */

			// 添加文本区注释
			if (annotation != null) {
				XYTextAnnotation text = new XYTextAnnotation(annotation, 
						dateAxis.getRange().getLowerBound(),    // X 坐标
						numberaxis.getRange().getLowerBound()); // Y 坐标
				text.setTextAnchor(TextAnchor.BOTTOM_LEFT);
				xyPlot.addAnnotation(text);
			}
			if (StringUtils.isEmpty(fileName)) {
				fileName = new Date().getTime() + "XY";
			}
			String filePath = srcPath + File.separator + fileName + ".png";
			ChartUtils.saveChartAsPNG(new File(filePath), chart, def_width, def_heigh);
			return filePath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String generateBarChart(DefaultCategoryDataset dataset, String srcPath, String fileName) {
		if (StringUtils.isEmpty(srcPath)) {
			return null;
		}
		try {
			JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset, PlotOrientation.VERTICAL, // 图表方向
					false, // 是否显示图例
					true, // 是否生成提示工具
					false);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			plot.setRangeGridlinesVisible(false);// 不显示绘图区域网格
			plot.setDomainGridlinesVisible(false);

			// 获取显示线条的对象
			BarRenderer renderer = (BarRenderer) plot.getRenderer();
			renderer.setSeriesPaint(0, new Color(61, 38, 168));// 设置颜色

			// y 轴
			NumberAxis numberaxis = (NumberAxis) plot.getRangeAxis();
			// numberaxis.setRange(42, 58);
			// numberaxis.setTickUnit(new NumberTickUnit(2)); // 间隔为2
			numberaxis.setUpperMargin(0.06);

			// x 轴
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setLowerMargin(0.01);
			domainAxis.setUpperMargin(0.01);
			
			// 添加文本区注释
			if (annotation != null) {
				CategoryTextAnnotation text = new CategoryTextAnnotation(annotation, 
						dataset.getColumnKeys().get(0).toString(), // X 坐标
						numberaxis.getRange().getUpperBound());   // Y 坐标
				text.setTextAnchor(TextAnchor.TOP_LEFT);
				plot.addAnnotation(text);
			}
			
			if (StringUtils.isEmpty(fileName)) {
				fileName = new Date().getTime() + "Bar";
			}
			String filePath = srcPath + File.separator + fileName + ".png";
			ChartUtils.saveChartAsPNG(new File(filePath), chart, def_width, def_heigh);
			return filePath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TimeSeriesCollection createVolTimeSeriesDataset(List<PackDataInfo> datas, List<PackDataInfo> points) {
		TimeSeriesCollection dataset = new TimeSeriesCollection();

		// 曲线赋值
		TimeSeries series = new TimeSeries("line");
		for (PackDataInfo packDataInfo : datas) {
			series.add(new Second(packDataInfo.getRcvTime()), packDataInfo.getGenVol());
		}
		dataset.addSeries(series);

		// 点赋值
		if (CollectionUtils.isNotEmpty(points)) {
			TimeSeries point = new TimeSeries("point");
			for (PackDataInfo packDataInfo : points) {
				point.add(new Second(packDataInfo.getRcvTime()), packDataInfo.getGenVol());
			}
			dataset.addSeries(point);
		}
		return dataset;
	}
	
	/**
	 * 得到单体电压的时序图数据
	 * @param datas
	 * @param points
	 * @param cellIndex 
	 * @return
	 */
	public static TimeSeriesCollection createEntityVolTimeSeriesDataset(List<PackDataInfo> datas, 
			List<PackDataInfo> points, int cellIndex) {
		TimeSeriesCollection dataset = new TimeSeriesCollection();

		// 曲线赋值
		TimeSeries series = new TimeSeries("line");
		for (PackDataInfo packDataInfo : datas) {
			Object cellVol = ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + cellIndex);
			series.add(new Second(packDataInfo.getRcvTime()), Double.parseDouble(cellVol.toString()));
		}
		dataset.addSeries(series);

		// 点赋值
		if (CollectionUtils.isNotEmpty(points)) {
			TimeSeries point = new TimeSeries("point");
			for (PackDataInfo packDataInfo : points) {
				Object cellVol = ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + cellIndex);
				point.add(new Second(packDataInfo.getRcvTime()), Double.parseDouble(cellVol.toString()));
			}
			dataset.addSeries(point);
		}
		return dataset;
	}

	public static TimeSeriesCollection createCurTimeSeriesDataset(List<PackDataInfo> datas, List<PackDataInfo> points) {
		TimeSeriesCollection dataset = new TimeSeriesCollection();

		// 曲线赋值
		TimeSeries series = new TimeSeries("line");
		for (PackDataInfo packDataInfo : datas) {
			series.add(new Second(packDataInfo.getRcvTime()), packDataInfo.getGenCur());
		}
		dataset.addSeries(series);

		// 点赋值
		if (CollectionUtils.isNotEmpty(points)) {
			TimeSeries point = new TimeSeries("point");
			for (PackDataInfo packDataInfo : points) {
				point.add(new Second(packDataInfo.getRcvTime()), packDataInfo.getGenCur());
			}
			dataset.addSeries(point);
		}
		return dataset;
	}

	public static DefaultCategoryDataset createCapBarSeriesDataset(PackDataExpandLatest packDataExpandLatest, int cellCount) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		List<CellInfoDetail> capList = new ArrayList<>();
		for (int i = 1; i < cellCount + 1; i++) {
			CellInfoDetail info = new CellInfoDetail();
			info.setCellIndex(i);
			Object cellCap = ReflectUtil.getValueByKey(packDataExpandLatest, CELL_CAP_PREFIX + i);
			info.setCellCap(BigDecimal.valueOf(Double.parseDouble(cellCap.toString())));
			Object capSort = ReflectUtil.getValueByKey(packDataExpandLatest, CELL_CAP_SORT_PREFIX + i);
			info.setCellCapIndex(Integer.parseInt(capSort.toString()));
			capList.add(info);
		}
		if (CollectionUtils.isNotEmpty(capList)) {
			capList = capList.stream().sorted(Comparator.comparing(CellInfoDetail::getCellCapIndex).reversed())
					.collect(Collectors.toList());
			
			for (CellInfoDetail cellInfoDetail : capList) {
				dataset.addValue(cellInfoDetail.getCellCap().doubleValue(),  // Y值
						"bar", 
						String.valueOf(cellInfoDetail.getCellIndex())); // X 值
			}
		}
		return dataset;
	}

	public static DefaultCategoryDataset createResistBarSeriesDataset(PackDataExpandLatest packDataExpandLatest, int cellCount) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 1; i < cellCount + 1; i++) {
			Object valueByKey = ReflectUtil.getValueByKey(packDataExpandLatest, CELL_RESIST_PREFIX + i);
			dataset.addValue(Double.parseDouble(valueByKey.toString()), // Y值
					"bar", String.valueOf(i)); // X 值
		}
		return dataset;
	}

	public JfreeChartUtils setSize(int width, int heigh) {
		this.def_width = width < 0 ? middle_width : width;
		this.def_heigh = heigh < 0 ? middle_heigh : heigh;
		return this;
	}
	
	public JfreeChartUtils setLabel(String yLabel, String xLabel) {
		this.yLabel = yLabel;
		this.xLabel = xLabel;
		return this;
	}
	
	public JfreeChartUtils setTiltle(String title) {
		this.title = title;
		return this;
	}
	
	public JfreeChartUtils addAnnotation(String annotation) {
		this.annotation = annotation;
		return this;
	}
	
	public static String getParentPath(DischargeManualRecord record) {
		String path = "D:/整治报告";
		Calendar cal = Calendar.getInstance();
		cal.setTime(record.getDischargeStartTime());
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH )+1;
		//path = D:/整治报告/2018_04
		path =path + File.separator +year+"_"+month;
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return path;
	}
	//保存图片临时路径
	public static String getPicturePath(DischargeManualRecord record) {
		String path = "D:/整治报告图片";
		path =path + File.separator +record.getGprsId()+"_"+dataFormat(record.getDischargeStartTime());
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return path;
	}
	
	public static String dataFormat(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		return format.format(date);
	}
}
