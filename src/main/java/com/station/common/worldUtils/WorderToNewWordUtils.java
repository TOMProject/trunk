package com.station.common.worldUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;


/**
 * 通过word模板生成新的word工具类
 * 
 * @author 
 * 
 */
public class WorderToNewWordUtils  extends XWPFDocument{

    /**
     * 根据模板生成新word文档
     * 判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
     * @param inputUrl 模板存放地址
     * @param outPutUrl 新文档存放地址
     * @param textMap 需要替换的信息集合
     * @param tableList 需要插入的表格信息集合
     * @return 成功返回true,失败返回false
     */
    public static boolean changWord(String inputUrl, String outputUrl,
            Map<String, Object> textMap, List<String[]> tableList) {

        //模板转换默认成功
        boolean changeFlag = true;
        try {
            //获取docx解析对象
           // XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(inputUrl));
			CustomXWPFDocument document = null;
            OPCPackage pack = POIXMLDocument.openPackage(inputUrl);  
            document = new CustomXWPFDocument(pack); 
            //WorderToNewWordUtils.changePicture(document, textMap);
            //解析替换文本段落对象
            WorderToNewWordUtils.changeText(document, textMap);
            //解析替换表格对象
            WorderToNewWordUtils.changeTable(document, textMap, tableList);
            //生成新的word
            File file = new File(outputUrl);
            FileOutputStream stream = new FileOutputStream(file);
            document.write(stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
            changeFlag = false;
        }

        return changeFlag;

    }

    /**
     * 替换段落文本
     * @param document docx解析对象
     * @param textMap 需要替换的信息集合
     */
    public static void changeText(XWPFDocument document, Map<String, Object> textMap){
        //获取段落集合
        List<XWPFParagraph> paragraphs = document.getParagraphs();
      
        for (XWPFParagraph paragraph : paragraphs) {
            //判断此段落时候需要进行替换
            String text = paragraph.getText();
            if(checkText(text)){
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                	System.out.println("段落的改变---》"+run.toString());
              	  	//替换模板原来位置
                	run.setText(changeValue(run.toString(), textMap),0);
                	run.setFontFamily("宋体");
                	run.setFontSize(8);
                	if(run.toString().equals("${vol_bight}")) {
                		run.setFontSize(5);
                	}
                }
            }
        }

    }
    
    
    /**
     * 添加图片
     * @param document docx解析对象
     * @param textMap 需要替换的信息集合
     */
    public static List<FileInputStream> changePicture(CustomXWPFDocument doc, Map<String, Object> map,FileInputStream in1,FileInputStream in2,FileInputStream in3){
    	List<FileInputStream> inputStreams = new ArrayList<>();
    	//获取段落集合
        List<XWPFParagraph> paragraphs = doc.getParagraphs();
        String type = (String) map.get("type");//图片的格式png,jpg
        int width = Integer.parseInt((String) map.get("width"));//宽度
        int high = Integer.parseInt((String) map.get("high"));//宽度
      
        try {
      
        for (XWPFParagraph paragraph : paragraphs) {
        	String text = paragraph.getText(); 
        	 if(checkText(text)){
        	if(text!=null) {
        		int indexOf = text.indexOf("img");
                if(indexOf != -1){
                    //特殊处理图片
                    int length = paragraph.getRuns().size();
                    //将原有的Run去掉
                    if (length > 0) {               
                         for (int i = (length - 1); i >= 0; i--) {
                             paragraph.removeRun(i);
                         }                                                       
                     }
                }
              //解析A图片
                if(text.indexOf("${imgA_1}") != -1) {
           		String pictureUrl_A_1 = (String) map.get("imgA_1");
	           		if(pictureUrl_A_1 != null) {
	           			in1 = new FileInputStream(new File(pictureUrl_A_1));
	            		String blipId = doc.addPictureData(in1, getPictureType(type));
	                    doc.createPicture(blipId,doc.getNextPicNameNumber(getPictureType(type)), width, high,paragraph);                           
	                    inputStreams.add(in1);
	           		}
                }
                if(text.indexOf("${imgA_2}") != -1) {
           		String pictureUrl_A_2 = (String) map.get("imgA_2");
	           		if(pictureUrl_A_2 != null) {
	           			 in2 = new FileInputStream(new File(pictureUrl_A_2));
	            		String blipId = doc.addPictureData(in2, getPictureType(type));
	                    doc.createPicture(blipId,doc.getNextPicNameNumber(getPictureType(type)), width, high,paragraph);                           
	                    inputStreams.add(in2);
	           		}
                }
               //解析B图片
                if(text.indexOf("${imgB_1}") != -1) {
           		String pictureUrl_B_1 = (String) map.get("imgB_1");
	           		if(pictureUrl_B_1 != null) {
	           			high = 400;
	           			in3 = new FileInputStream(new File(pictureUrl_B_1));
	            		String blipIdb = doc.addPictureData(in3, getPictureType(type));
	                    doc.createPicture(blipIdb,doc.getNextPicNameNumber(getPictureType(type)), width, high,paragraph);                           
	                    inputStreams.add(in3);
	           		}
                }

        	}
        }	

            //判断此段落时候需要进行替换
//            String text = paragraph.getText();
//            if(checkText(text)){
//                List<XWPFRun> runs = paragraph.getRuns();
//                for (XWPFRun run : runs) {
//                	System.out.println("加载图片---》"+run.toString());
//                	String pName =run.toString().substring(run.toString().indexOf("{")+1, run.toString().length()-1);                 
//                	try {
//                    	//if(run.toString().equals("${"+pName+"}")) {
//                		String text2 = run.getText(0);
//
//                		if (text2 != null) {
//
//                		if (text2.indexOf("${"+pName+"}") >= 0) {
//                			//String pictureUrl="E:\\ABC\\"+ pName + ""+type;
//                    		String pictureUrl = Constant.TEMPLETE_PATH + pName + ".png";
////                    		run.addBreak();
////							run.addPicture(new FileInputStream(pictureUrl),getPictureType(type),pictureUrl, Units.toEMU(width),Units.toEMU(high)); // 宽200x高200 pixels							
////                    		run.addBreak(BreakType.PAGE);
//	                		 int length = paragraph.getRuns().size();
//	                         //将原有的Run去掉
//	                         if (length > 0) {               
//	                              for (int i = (length - 1); i >= 0; i--) {
//	                                  paragraph.removeRun(i);
//	                              }                                                       
//	                          }
//                    		String blipId = doc.addPictureData(new FileInputStream(new File(pictureUrl)), getPictureType(type));
//                            doc.createPicture(blipId,doc.getNextPicNameNumber(getPictureType(type)), width, high,paragraph);                        
//                            break;
//                            //} 
//                		}
//                		}
//					} catch (Exception e) {						
//						e.printStackTrace();
//					}
//                }
//            }
        }
        
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStreams;

    }

    /**
     * 替换表格对象方法
     * @param document docx解析对象
     * @param textMap 需要替换的信息集合
     * @param tableList 需要插入的表格信息集合
     */
    public static void changeTable(XWPFDocument document, Map<String, Object> textMap,
            List<String[]> tableList){
        //获取表格对象集合
        List<XWPFTable> tables = document.getTables();
        for (int i = 0; i < tables.size(); i++) {
            //只处理行数大于等于2的表格，且不循环表头
            XWPFTable table = tables.get(i);
            if(table.getRows().size()>1){
                //判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
                if(checkText(table.getText())){
                    List<XWPFTableRow> rows = table.getRows();
                    System.out.println("替换的信息--"+textMap.get("gprsId"));
                    //遍历表格,并替换模板
                    eachTable(rows, textMap);
                }else{
                 // System.out.println("插入"+table.getText());
                    //insertTable(table, tableList);
                }
            }
        }
    }

    /**
     * 遍历表格
     * @param rows 表格行对象
     * @param textMap 需要替换的信息集合
     */
    public static void eachTable(List<XWPFTableRow> rows ,Map<String, Object> textMap){
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            for (XWPFTableCell cell : cells) {
                //判断单元格是否需要替换
                if(checkText(cell.getText())){
                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                    for (XWPFParagraph paragraph : paragraphs) {
                        List<XWPFRun> runs = paragraph.getRuns(); 
                        for (XWPFRun run : runs) {
                            if(run.toString().indexOf("${gprsId}")>=0) {
                            	//run.setBold(true);加粗
                            	//run.setColor("FF0000");//红色
                            	//run.setFontSize(20);//字体大小
                            	run.setFontFamily("宋体");
                            }
                            run.setText(changeValue(run.toString(), textMap),0);
                        }
                    }
                }
            }
        }
    }
    
    public void createPicture(String blipId,int id, int width, int height, XWPFParagraph paragraph)
    {
        final int EMU = 9525;
        width *= EMU;
        height *= EMU;
       // String  blipId = getAllPictures().get(id).getPackageRelationship().getId();

        //CTInline inline = createParagraph().createRun().getCTR().addNewDrawing().addNewInline(); 
        //给段落插入图片
       CTInline inline = paragraph.createRun().getCTR().addNewDrawing().addNewInline();

        String picXml = "" +
                "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
                "   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
                "      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
                "         <pic:nvPicPr>" +
                "            <pic:cNvPr id=\"" + id + "\" name=\"Generated\"/>" +
                "            <pic:cNvPicPr/>" +
                "         </pic:nvPicPr>" +
                "         <pic:blipFill>" +
                "            <a:blip r:embed=\"" + blipId + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>" +
                "            <a:stretch>" +
                "               <a:fillRect/>" +
                "            </a:stretch>" +
                "         </pic:blipFill>" +
                "         <pic:spPr>" +
                "            <a:xfrm>" +
                "               <a:off x=\"0\" y=\"0\"/>" +
                "               <a:ext cx=\"" + width + "\" cy=\"" + height + "\"/>" +
                "            </a:xfrm>" +
                "            <a:prstGeom prst=\"rect\">" +
                "               <a:avLst/>" +
                "            </a:prstGeom>" +
                "         </pic:spPr>" +
                "      </pic:pic>" +
                "   </a:graphicData>" +
                "</a:graphic>";

        //CTGraphicalObjectData graphicData = inline.addNewGraphic().addNewGraphicData();
        XmlToken xmlToken = null;
        try
        {
            xmlToken = XmlToken.Factory.parse(picXml);
        }
        catch(XmlException xe)
        {
            xe.printStackTrace();
        }
        inline.set(xmlToken);
        //graphicData.set(xmlToken);

        inline.setDistT(0);
        inline.setDistB(0);
        inline.setDistL(0);
        inline.setDistR(0);

        CTPositiveSize2D extent = inline.addNewExtent();
        extent.setCx(width);
        extent.setCy(height);

        CTNonVisualDrawingProps docPr = inline.addNewDocPr();
        docPr.setId(id);
        docPr.setName("Picture " + id);
        docPr.setDescr("Generated");
    }

    
    /**
     * 为表格插入数据，行数不够添加新行
     * @param table 需要插入数据的表格
     * @param tableList 插入数据集合
     */
    public static void insertTable(XWPFTable table, List<String[]> tableList){
        //创建行,根据需要插入的数据添加新行，不处理表头
        for(int i = 1; i < tableList.size(); i++){
            XWPFTableRow row =table.createRow();
        }
        //遍历表格插入数据
        List<XWPFTableRow> rows = table.getRows();
        for(int i = 1; i < rows.size(); i++){
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            for(int j = 0; j < cells.size(); j++){
                XWPFTableCell cell = cells.get(j);
                cell.setText(tableList.get(i-1)[j]);
            }
        }

    }

    /**
     * 判断文本中时候包含$
     * @param text 文本
     * @return 包含返回true,不包含返回false
     */
    public static boolean checkText(String text){
        boolean check  =  false;
        if(text.indexOf("$")!= -1){
            check = true;
        }
        return check;
    }

    /**
     * 匹配传入信息集合与模板
     * @param value 模板需要替换的区域
     * @param textMap 传入信息集合
     * @return 模板需要替换区域信息集合对应值
     */
    public static String changeValue(String value, Map<String, Object> textMap){
        Set<Entry<String, Object>> textSets = textMap.entrySet();
        for (Entry<String, Object> textSet : textSets) {
            //匹配模板与替换值 格式${key}
            String key = "${"+textSet.getKey()+"}";
            if(value.indexOf(key)!= -1){
                value = (String) textSet.getValue();
            }
        }
        //模板未匹配到区域替换为空
        if(checkText(value)){
            value = "";
        }
        return value;
    }
    
    /**  
     * 根据图片类型，取得对应的图片类型代码  
     * @param picType  
     * @return int  
     */    
    public static int getPictureType(String picType){    
        int res = XWPFDocument.PICTURE_TYPE_PICT;    
        if(picType != null){    
            if(picType.equalsIgnoreCase("png")){    
                res = XWPFDocument.PICTURE_TYPE_PNG;    
            }else if(picType.equalsIgnoreCase("dib")){    
                res = XWPFDocument.PICTURE_TYPE_DIB;    
            }else if(picType.equalsIgnoreCase("emf")){    
                res = XWPFDocument.PICTURE_TYPE_EMF;    
            }else if(picType.equalsIgnoreCase("jpg") || picType.equalsIgnoreCase("jpeg")){    
                res = XWPFDocument.PICTURE_TYPE_JPEG;    
            }else if(picType.equalsIgnoreCase("wmf")){    
                res = XWPFDocument.PICTURE_TYPE_WMF;    
            }    
        }    
        return res;    
    }  

    public static void addHeader(CustomXWPFDocument doc,String imgFile) {
    	  try {
    	       // XWPFDocument doc= new XWPFDocument();

    	        // the body content
    	        XWPFParagraph paragraph = doc.createParagraph();
    	        XWPFRun run=paragraph.createRun();  
//    	        run.setText("The Body:");

//    	        paragraph = doc.createParagraph();
//    	        run=paragraph.createRun();  
//    	        run.setText("Lorem ipsum....");

    	        // create header start
    	        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
    	        XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(doc, sectPr);

    	        XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);

    	        paragraph = header.getParagraphArray(0);
    	        paragraph.setAlignment(ParagraphAlignment.LEFT);

    	        CTTabStop tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
    	        tabStop.setVal(STTabJc.RIGHT);
    	        int twipsPerInch =  1440;
    	        tabStop.setPos(BigInteger.valueOf(6 * twipsPerInch));

//    	        run = paragraph.createRun();  
//    	        run.setText("The Header:");
//    	        run.addTab();

    	        run = paragraph.createRun();  
    	       // String imgFile="E:\\ABC\\logo.png";
    	        XWPFPicture picture = run.addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_PNG, imgFile, Units.toEMU(250), Units.toEMU(50));
    	        System.out.println(picture); //XWPFPicture is added
    	        System.out.println(picture.getPictureData()); //but without access to XWPFPictureData (no blipID)

    	        String blipID = "";
    	        for(XWPFPictureData picturedata : header.getAllPackagePictures()) {
    	         blipID = header.getRelationId(picturedata);
    	         System.out.println(blipID); //the XWPFPictureData are already there
    	        }
    	        picture.getCTPicture().getBlipFill().getBlip().setEmbed(blipID); //now they have a blipID also
    	        System.out.println(picture.getPictureData());
    	        
    	        
    	        // create footer start
//    	        XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);

//    	        paragraph = footer.getParagraphArray(0);
    	        paragraph.setAlignment(ParagraphAlignment.RIGHT);

//    	        run = paragraph.createRun();  
//    	        run.setText("The Footer:");


    	        //doc.write(new FileOutputStream("E:\\ABC\\test.docx"));

    	        }catch (Exception e) {
    				e.printStackTrace();
    			}
    }
    
    
    
    /**
     * 创建目录
     * @param basePath "E:/ABC/word/"
     */
    public static void createDir(String basePath)
    {
      File file = new File(basePath);
      if (!file.exists())
        file.mkdirs();
    }
    
    public static void main(String[] args) {
        //模板文件地址
        String inputUrl = "E:\\ABC\\001.docx";
        //新生产的模板文件
        String outputUrl = "E:\\ABC\\test.docx";

        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put("name", "小明");
        testMap.put("sex", "男");
        testMap.put("address", "软件园");
        testMap.put("phone", "88888888");
        testMap.put("high", "200");
        testMap.put("width", "200");
        testMap.put("imgA", "E:\\ABC\\imgA.png");
        testMap.put("imgB", "E:\\ABC\\imgB.png");
        List<String[]> testList = new ArrayList<String[]>();
        testList.add(new String[]{"1","1AA","1BB","1CC"});
        testList.add(new String[]{"2","2AA","2BB","2CC"});
        testList.add(new String[]{"3","3AA","3BB","3CC"});
        testList.add(new String[]{"4","4AA","4BB","4CC"});
       // WorderToNewWordUtils.changWord(inputUrl, outputUrl, testMap, testList);
        
        
        
        
        try {
       // XWPFDocument doc= new XWPFDocument();
		CustomXWPFDocument doc = null;
            OPCPackage pack = POIXMLDocument.openPackage("E:\\ABC\\001.docx");  
            doc = new CustomXWPFDocument(pack); 

        // the body content
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run=paragraph.createRun();  
//        run.setText("The Body:");

//        paragraph = doc.createParagraph();
//        run=paragraph.createRun();  
//        run.setText("Lorem ipsum....");

        // create header start
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(doc, sectPr);

        XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);

        paragraph = header.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.LEFT);

        CTTabStop tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
        tabStop.setVal(STTabJc.RIGHT);
        int twipsPerInch =  1440;
        tabStop.setPos(BigInteger.valueOf(6 * twipsPerInch));

//        run = paragraph.createRun();  
//        run.setText("The Header:");
//        run.addTab();

        run = paragraph.createRun();  
        String imgFile="E:\\ABC\\logo.png";
        XWPFPicture picture = run.addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_PNG, imgFile, Units.toEMU(250), Units.toEMU(50));
        System.out.println(picture); //XWPFPicture is added
        System.out.println(picture.getPictureData()); //but without access to XWPFPictureData (no blipID)

        String blipID = "";
        for(XWPFPictureData picturedata : header.getAllPackagePictures()) {
         blipID = header.getRelationId(picturedata);
         System.out.println(blipID); //the XWPFPictureData are already there
        }
        picture.getCTPicture().getBlipFill().getBlip().setEmbed(blipID); //now they have a blipID also
        System.out.println(picture.getPictureData());
        
        
        // create footer start
//        XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
//
//        paragraph = footer.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
//
//        run = paragraph.createRun();  
//        run.setText("The Footer:");


        doc.write(new FileOutputStream("E:\\ABC\\test333.docx"));

        }catch (Exception e) {
			e.printStackTrace();
		}
    }
}