package com.vtradex.wms.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.vtradex.thorn.server.config.globalparam.GlobalParam;
import com.vtradex.thorn.server.dao.hibernate.HibernateCommonDao;
import com.vtradex.thorn.server.exception.BusinessException;

/**
 * 
 * @Description :   常用工具类 WMS
 * @Author      :    <a href='yongcheng.min@vtradex.net'>闵永成</a>
 * @CreateDate  :    2011-4-19
 */
public class JavaTools extends HibernateCommonDao{
	//g001
	public static SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	//g002
	public static final String numbers = "0123456789";
	//g003
	public static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	//g004
	public static final String mixings = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	//g005
	private static final String ENCODING="GBK";
	//g006
	public static final String ymdPattern2 = "^\\d{4}\\d{2}\\d{2}[-]\\d{4}\\d{2}\\d{2}$";//yyyyMMdd-yyyyMMdd
	//g007
	public static final String ymdPattern3 = "^\\d{4}\\d{2}\\d{2}$";//yyyyMMdd
	//g008
	 /** 
     * 定义分割常量 （#在集合中的含义是每个元素的分割，|主要用于map类型的集合用于key与value中的分割） 
     */  
    private static final String SEP1 = "#";  
  //g009
    private static final String SEP2 = "|";  
  //g010
    /**\r\n*/
	public static String enter = "\r\n";
	//g011
	/**"yyyy-MM-dd HH:mm*/
	public static String dmy_hm = "yyyy-MM-dd HH:mm";
	//g012
	/**yyyyMMdd*/
	public static String dmy = "yyyyMMdd";
	//g013
	/**yyyy-MM-dd*/
	public static String y_m_d = "yyyy-MM-dd";
	//g014
	/**"yyyy-MM-dd HH:mm:ss*/
	public static String dmy_hms = "yyyy-MM-dd HH:mm:ss";
	//g015
	/**"HH:mm:ss*/
	public static String hms = "HH:mm:ss";
	//g016
	/**' '空格*/
	public static String tab = " ";
	//g017
	/**yyyyMM*/
	public static String ym = "yyyyMM";
	//g018
	/**yyMMdd*/
	public static String yymd = "yyMMdd";
	//g019
	public static String patterString = "^\\d{4}[-]\\d{2}[-]\\d{2}$";
	//g020
	/**HHmmss*/
	public static String HMS = "HHmmss";
	//g021
	public static String reg="^[a-zA-Z]{2}$";
	//g022
	/**yyMMdd_HHmmss*/
	public static String ymd_Hms="yyMMdd_HHmmss";
    //000
    public static int getSize(int size,int PAGE_NUMBER){
		int j = size / PAGE_NUMBER;
		if((size % PAGE_NUMBER) > 0){
			j += 1;
		}
		return j;
    }
  //001
    public static int getIndex(int k,int size,int PAGE_NUMBER){
    	int toIndex = ((k + 1) * PAGE_NUMBER);
		if(toIndex > size){
			toIndex = size;
		}
		return toIndex;
    }
  //002
    public static List<Object> getList(List<Object> list,int k,int toIdnex,int PAGE_NUMBER){
    	List<Object> ret = list.subList((k * PAGE_NUMBER), toIdnex);
		return ret;
    }
  //003
    /**return List<Object[]>*/
    public static List<Object[]> getListObj(List<Object[]> list,int k,int toIndex,int PAGE_NUMBER){  
        List<Object[]> ret = list.subList((k * PAGE_NUMBER), toIndex);  
        return ret;  
    }
  //004
    public static List<String> getListStr(List<String> list,int k,int toIdnex,int PAGE_NUMBER){
    	List<String> ret = list.subList((k * PAGE_NUMBER), toIdnex);
		return ret;
    }
  //005
    public static List<Long> getListLong(List<Long> list,int k,int toIdnex,int PAGE_NUMBER){
    	List<Long> ret = list.subList((k * PAGE_NUMBER), toIdnex);
		return ret;
    }
    public static List getLists(List list,int k,int toIdnex,int PAGE_NUMBER){
    	List ret = list.subList((k * PAGE_NUMBER), toIdnex);
		return ret;
    }
  //006
    public static String isNull(Object obj){
		return (obj == null) ? "" : obj.toString();
	}
  //007
    /**@nullback:为空值时返回值*/
    public static String valueOf(Object obj,String nullback){
    	return (obj == null) ? nullback : obj.toString();
    }
  //008
	public static String isNull(String Num){
		 if(Num == null||"".equals(Num)){
			Num = "0";
		 }
		 return Num;
	}
	//009
	public static Double isNull(Double Num){
		if(Num == null){
			Num = 0D;
		}
		return Num;
	}
	//010
	/**  
	 * stringToInteger
	 * @return  Integer
	 */
	public static Integer stringToInteger(String str) {
		if (null==str || str.trim().equals("")){
			return 0;
		}else{
			return Integer.parseInt(str.trim());
		}
	}
	//011
	 public static boolean isValidDate(String s){
		     DateFormat df = new SimpleDateFormat("yyyyMMdd");
	        try
	        {
	             df.parse(s);
	             return true;
	         }
	        catch (Exception e)
	        {
	            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
	            return false;
	        }
	 }
	//012
	/**
	 * stringToDate-minyongcheng
	 * @param string is xxxx-xx-xx
	 * @return yyyy-MM-dd
	 */
	 public static Date stringToDate(String str) {
		if (str == null || str.trim().equals("") || str.trim().equals("0"))
			return null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return df.parse(str.trim());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	 }
	 public static Date stringFDate(String str,String pattern) {
		if (str == null || str.trim().equals("") || str.trim().equals("0"))
			return null;
		DateFormat df = new SimpleDateFormat(pattern);
		try {
			return df.parse(str.trim());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	 }
	//013
	 /**
		 * stringToDate-huangshaokang
		 * @param string is xxxxmmdd
		 * @return yyyyMMdd
		 */
		 public static Date stringToDate2(String str) {
			if (str == null || str.trim().equals("") || str.trim().equals("0"))
				return null;
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			try {
				return df.parse(str.trim());
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		 }
		//014
	 public static Long stringToLong(String str) {
			if (str == null || str.trim().equals(""))
				return null;
			else
				return Long.parseLong(str.trim());
	}
	//015
	 public static Double stringToDouble(String str) {
			if (null==str || str.trim().equals(""))
				return 0D;
			else
				return Double.parseDouble(str.trim());
	 }
	//016
	/**
	 * 长度单位转换
	 * cm->m
	 * @return double
	 */
	public static double unitConversion(double num){
		double result = 0;
		result = num * (1 / 100.0);
		return result;
	}
	//017
	/**
	 * 体积转换
	 * 立方厘米->立方米
	 */
	public static double unitConv(double num){
		double result = 0;
		result = num * (1 / 1000.0);
		return result;
	}
	//018
	/** yyyyMMdd*/
	public static String dateToString(Date date) {
		if(date == null){
			return null;
		}
		DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(date);
	}
	//019
	/** yyyy-MM-dd*/
	public static String dateYMDToStr(Date date){
		if(date==null){
			return null;
		}
		return ymd.format(date);
	}
	//020
	public static String getRandomUtils(int length){
		 StringBuffer sb = new StringBuffer(); 
	     Random random = new Random(); 
	     for (int i = 0; i < length; i++){
	    	 sb.append(numbers.charAt(random.nextInt(numbers.length())));
	     }
	     return sb.toString();

	}
	//021
	/**将字符串转换成符合规范的date  myc*/
	public static Date format(String time){
		Date date = null;
		if(time.contains("-")){
			date = DateUtil.getDate(
					time, "yyyy-MM-dd");
		}else if (time.contains("/")){
			date = DateUtil.getDate(
					time, "yyyy/MM/dd");
		}else{
			date = DateUtil.getDate(
					time, "yyyyMMdd");
		}
		return date;
	}
	//022
	/**
	 * 把一个Date 按照指定格式转换为String
	 * @param date
	 * @return
	 */
	public static String format(Date date,String format) {
		DateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}
	//023
	/** 
	  * 返回两个日期相差的天数,有一个时间为null返回-1 
	  * @param d1  长的时间 
	  * @param d2  短的时间 
	  * @return int 
	  */ 
	 public static int diff_in_date(Date d1, Date d2){
		 if(null == d1 || null == d2){ 
		   return -1; 
		 } 
		 return (int)(d1.getTime() - d2.getTime())/86400000; 
	 }
	//024
	 /**  
	     * 计算两个日期之间相差的天数  
	     * @param smdate 较小的时间 
	     * @param bdate  较大的时间 
	     * @return 相差天数 
	     * @throws ParseException  
	     */    
	    public static int daysBetween(Date smdate,Date bdate) throws ParseException    
	    {    
	    	if(null == smdate || null == bdate){ 
			   return -1; 
			 } 
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
	        smdate=sdf.parse(sdf.format(smdate));  
	        bdate=sdf.parse(sdf.format(bdate));  
	        Calendar cal = Calendar.getInstance();    
	        cal.setTime(smdate);    
	        long time1 = cal.getTimeInMillis();                 
	        cal.setTime(bdate);    
	        long time2 = cal.getTimeInMillis();         
	        long between_days=(time2-time1)/(1000*3600*24);  
	            
	       return Integer.parseInt(String.valueOf(between_days));           
	    }  
	  //025
	    /** 
	    *字符串的日期格式的计算 
	    */  
	        public static int daysBetween(String smdate,String bdate) throws ParseException{  
	        	if(null == smdate || null == bdate){ 
	 			   return -1; 
	 			 }
	            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
	            Calendar cal = Calendar.getInstance();    
	            cal.setTime(sdf.parse(smdate));    
	            long time1 = cal.getTimeInMillis();                 
	            cal.setTime(sdf.parse(bdate));    
	            long time2 = cal.getTimeInMillis();         
	            long between_days=(time2-time1)/(1000*3600*24);  
	                
	           return Integer.parseInt(String.valueOf(between_days));     
	        }  
	      //026
	/**某个日期加一天
	 * @today yyyyMMdd
	 * @return String yyyyMMdd*/
	public static String addOneday(String today){   
        SimpleDateFormat f =  new SimpleDateFormat("yyyyMMdd");   
        try   {   
            Date  d  =  new Date(f.parse(today).getTime()+24*3600*1000);     
              return  f.format(d);   
        }   
        catch(Exception ex) {   
            return   "输入格式错误";     
        }   
    }
	//027
	/**日期+天数得到相应日期
	 * @today yyyyMMdd
	 * @return String yyyyMMdd*/
	public static Date addNumday(Date d,long day){
		if(d == null){
			return null;
		}
		long time = d.getTime(); 
		day = day*24*60*60*1000; 
		time+=day; 
		return new Date(time); 
    }
	//028
	/**某个日期减一天
	 *@return String yyyyMMdd */
	public static String deleteOneday(String today){   
        SimpleDateFormat f =  new SimpleDateFormat("yyyyMMdd");   
        try   {   
            Date  d  =  new Date(f.parse(today).getTime()-24*3600*1000);     
              return  f.format(d);   
        }   
        catch(Exception ex) {   
            return   "输入格式错误";     
        }   
    }
	//029
	/**获得系统上一天的时间*/
	public static Date getAscendDay(){
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(Calendar.DAY_OF_MONTH, -1);
		return currentDate.getTime();
	}
	//030
	/**
     * 
     * @param dateTime 格式为"yyyy-mm-dd"的日期格式
     * @return string类型的前一天的日期yyyy-MM-dd
     * @throws ParseException
     */
	//031
    public static  String getFormerDate(String dateTime,Date date) throws ParseException{
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
        long dif = df.parse(dateTime).getTime()-86400*1000;
//        Date date=new Date(); 
        date.setTime(dif);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            
        return sdf.format(date);
     }
  //032
    /**
     * 
     * @param dateTime 格式为"yyyy-mm-dd"的日期格式
     * @return string类型的前一天的日期yyyyMMdd
     * @throws ParseException
     */
    public static  String getFormerDate2(String dateTime,Date date) throws ParseException{
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
        long dif = df.parse(dateTime).getTime()-86400*1000;
//        Date date=new Date(); 
        date.setTime(dif);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            
        return sdf.format(date);
     }
  //033
	/**给日期增加几天 
	 * @return date*/
	public static Date addDays(int num){
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.set(Calendar.DAY_OF_YEAR, day + num);
		return calendar.getTime();
	}
	//034
	/**比较日期大小*/
	 public static int compare_date(String DATE1, String DATE2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
//                System.out.println("dt1 在dt2前");
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
//                System.out.println("dt1在dt2后");
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }
	//035
	 public static int compare_date2(String DATE1, String DATE2) {
	        DateFormat df = new SimpleDateFormat("yyyyMMdd");
	        try {
	            Date dt1 = df.parse(DATE1);
	            Date dt2 = df.parse(DATE2);
	            if (dt1.getTime() > dt2.getTime()) {
//	                System.out.println("dt1 在dt2前");
	                return 1;
	            } else if (dt1.getTime() < dt2.getTime()) {
//	                System.out.println("dt1在dt2后");
	                return -1;
	            } else {
	                return 0;
	            }
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }
	        return 0;
	    }
	//036
	 public static int compareToYMD( Date dt1, Date dt2) {
	        try {
	            if (dt1.getTime() > dt2.getTime()) {
//	                System.out.println("dt1 在dt2前");
	                return 1;
	            } else if (dt1.getTime() < dt2.getTime()) {
//	                System.out.println("dt1在dt2后");
	                return -1;
	            } else {
	                return 0;
	            }
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }
	        return 0;
	    }
	//037
	 /**获取这批导入单据的标志 */
		public static String getMark(){
			Date today = new Date();
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
			String mark = sf.format(today);
			return mark;
		}
		//038
	 /**
	  * 压缩文件zip-Apache中ant所带包
	  * @功能信息 :
	  * @参数信息 :
	  * @返回值信息 :
	  * @异常信息 :
	  * */
	 public static void getZip(List list,String path,String fileName) throws Exception{
	   byte[] buffer = new byte[1024];
	    
	   String strZipName = fileName + ".zip";
	   ZipOutputStream out = new ZipOutputStream(
			   new FileOutputStream(path + strZipName));
	   for (int j = 0; j < list.size(); j++) {
		   String name = list.get(j).toString();
		   FileInputStream fis = new FileInputStream(path + name);//D:\export2011-08-21ERR.txt
		   out.putNextEntry(new ZipEntry(name));
		   int len;
		   while ((len = fis.read(buffer)) > 0) {
		     out.write(buffer, 0, len);
		   }
		   out.closeEntry();
		   fis.close();
	   }
	   out.close();
	   System.out.println("生成Demo.zip成功");
	  }
	//039
	 /**yyyy/MM/dd -> yyyy-MM-dd hh:mm:ss */
	 public static String parseTimes(String date) {
	        String d = date;
	        if (date.indexOf("/") > 0) {
	            d = date.replace("/", "-");
	        }
	        String temp = d + " 00:00:00";
	        try {
	            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            Date time = df.parse(temp);
	            return String.valueOf(time);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	}
	//040
		public Date getGMTTime(Date date){
			Date result = null;
			try {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				String dataStr = dateFormat.format(date);
				dateFormat.setTimeZone(TimeZone.getDefault());
				result = dateFormat.parse(dataStr);
			} catch (ParseException e) {
				result = new Date();
				throw new BusinessException("import.data.ParseException.error");
			}
			return result;
		} 
		//041
	@SuppressWarnings("unchecked")
	public static String copyField(Object obj,String value,String propertyName) throws ClassNotFoundException, SecurityException, NoSuchMethodException,
	             IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Class classType=obj.getClass();
//			Field[] fields=classType.getDeclaredFields();
        String fieldName=propertyName;   
        String stringLetter=fieldName.substring(0, 1).toUpperCase();   
//	      String getName="get"+stringLetter+fieldName.substring(1);   
        String setName="set"+stringLetter+fieldName.substring(1);   
           
        Method m = classType.getMethod(setName,new Class[]{Class.forName("java.lang.String")});
		m.invoke(obj,new Object[]{value}); 
		
		return propertyName;
	}
	//042
	@SuppressWarnings("unchecked")
	public static String copyField(Object obj,String value1,String propertyName1,
			Double value2,String propertyName2,int num) throws ClassNotFoundException, SecurityException, NoSuchMethodException,
	             IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Class classType=obj.getClass();
//		Field[] fields=classType.getDeclaredFields();
        String stringLetter1=propertyName1.substring(0, 1).toUpperCase(); 
        String stringLetter2=propertyName2.substring(0, 1).toUpperCase(); 
//      String getName="get"+stringLetter+fieldName.substring(1);   
        String setName1="set"+stringLetter1+propertyName1.substring(1)+num;   //set通道1
        String setName2="set"+stringLetter2+propertyName2.substring(1)+num;
           
        Method m1 = classType.getMethod(setName1,new Class[]{Class.forName("java.lang.String")});
		m1.invoke(obj,new Object[]{value1}); 
		Method m2 = classType.getMethod(setName2,new Class[]{Class.forName("java.lang.Double")});
		m2.invoke(obj,new Object[]{value2});
		
		return null;
	}
	//043
	public int getPort() throws SQLException {
		int port = 0;
		String hql = "from GlobalParam gp where gp.paramId=:IP_ADDRESS";
		GlobalParam  gp = (GlobalParam) this.findByQueryUniqueResult(hql, new String[]{"IP_ADDRESS"}, new Object[]{"IP_ADDRESS"});
		String value = gp.getValue();
		String[] str2 = value.split(":");
		port = Integer.parseInt(str2[1].substring(0,4));
		return port;
	}
	//044
	public static String formatTest(double a,int num){
		String s = "0";
		DecimalFormat df = new DecimalFormat("###.00");
		if(a != 0){
			 if(num == 1){
	        	 df = new DecimalFormat("###.0");
			 }else if(num == 2){
			 	 df = new DecimalFormat("###.00");
			 }else if(num == 3){
				 df = new DecimalFormat("###.000");
			 }
	         s = df.format(a);
		}
		return s;
	}
	//045
	public static String formatTest(double a,double b,int num){
		String s = "0";
		DecimalFormat df = new DecimalFormat("###.00");
		if(a != 0){
			 if(num == 1){
	        	 df = new DecimalFormat("###.0");
			 }else if(num == 2){
			 	 df = new DecimalFormat("###.00");
			 }else if(num == 3){
				 df = new DecimalFormat("###.000");
			 }
			 s = df.format(a/b);
		}
		return s;
	}
	//046
	public static String formatMultiply(double a,double b,int num){
		String s = "0";
		DecimalFormat df = new DecimalFormat("###.00");
		if(a != 0){
			 if(num == 1){
	        	 df = new DecimalFormat("###.0");
			 }else if(num == 2){
			 	 df = new DecimalFormat("###.00");
			 }else if(num == 3){
				 df = new DecimalFormat("###.000");
			 }
			 s = df.format(a*b);
		}
		return s;
	}
	//047
	/** 
     * String转换Map 
     *  
     * @param mapText 
     *            :需要转换的字符串 
     * @param KeySeparator 
     *            :字符串中的分隔符每一个key与value中的分割 
     * @param ElementSeparator 
     *            :字符串中每个元素的分割 
     * @return Map<?,?> 
     */  
	//048
    public static Map<String, Object> StringToMap(String mapText) {  
  
        if (mapText == null || mapText.equals("")) {  
            return null;  
        }  
        mapText = mapText.substring(1);  
  
//        mapText = EspUtils.DecodeBase64(mapText);  
  
        Map<String, Object> map = new HashMap<String, Object>();  
        String[] text = mapText.split("\\" + SEP2); // 转换为数组  
        for (String str : text) {  
            String[] keyText = str.split(SEP1); // 转换key与value的数组  
            if (keyText.length < 1) {  
                continue;  
            }  
            String key = keyText[0]; // key  
            String value = keyText[1]; // value  
            if (value.charAt(0) == 'M') {  
                Map<?, ?> map1 = StringToMap(value);  
                map.put(key, map1);  
            } else if (value.charAt(0) == 'L') {  
                List<?> list = StringToList(value);  
                map.put(key, list);  
            } else {  
                map.put(key, value);  
            }  
        }  
        return map;  
    }  
  //049
	/** 
     * String转换List 
     *  
     * @param listText 
     *            :需要转换的文本 
     * @return List<?> 
     */  
    public static List<Object> StringToList(String listText) {  
        if (listText == null || listText.equals("")) {  
            return null;  
        }  
        listText = listText.substring(1);  
  
//        listText = EspUtils.DecodeBase64(listText);  
  
        List<Object> list = new ArrayList<Object>();  
        String[] text = listText.split(SEP1);  
        for (String str : text) {  
            if (str.charAt(0) == 'M') {  
                Map<?, ?> map = StringToMap(str);  
                list.add(map);  
            } else if (str.charAt(0) == 'L') {  
                List<?> lists = StringToList(str);  
                list.add(lists);  
            } else {  
                list.add(str);  
            }  
        }  
        return list;  
    }  
  //050
	public static Map<Long,Double> getAddMap(Map<Long, Double> map1,Map<Long, Double> map2) {
		Map<Long,Double> cc = new HashMap<Long,Double>();
			Iterator<Entry<Long, Double>> iter1 = map1.entrySet().iterator();
			while (iter1.hasNext()) {
				Map.Entry<Long,Double> entry = (Map.Entry<Long,Double>) iter1.next();
				if(cc.containsKey(entry.getKey())){
					Double temp=(Double) entry.getValue();
					cc.put(entry.getKey(),cc.get(entry.getKey())+temp);
				}else{
					cc.put(entry.getKey(),entry.getValue());
				}
			}
			Iterator<Entry<Long, Double>> iter2 = map2.entrySet().iterator();
			while (iter2.hasNext()) {
				Map.Entry<Long,Double> entry = (Map.Entry<Long,Double>) iter2.next();
				if(cc.containsKey(entry.getKey())){
					Double temp=(Double) entry.getValue();
					cc.put(entry.getKey(),cc.get(entry.getKey())+temp);
				}else{
					cc.put(entry.getKey(),entry.getValue());
				}
			}
		
		return cc;
	}
	//051
	@SuppressWarnings("unchecked")
	public static Map<Long,List> getQMap(Map<Long, List> map1,Map<Long, Double> map2) {
		Iterator<Entry<Long, Double>> iter2 = map2.entrySet().iterator();
		while (iter2.hasNext()) {
			Map.Entry<Long,Double> entry = (Map.Entry<Long,Double>) iter2.next();
			if(map1.containsKey(entry.getKey())){
				//合并
			   Double temp=(Double) entry.getValue();
			   List newlIST = map1.get(entry.getKey());
			   newlIST.remove(1);
			   newlIST.add(1,temp);
			   map1.put(entry.getKey(),newlIST);
			}else{
				//新增,同时设置list第一列为0
				List listrece = new ArrayList();
				listrece.add(0D);
				listrece.add(entry.getValue());
				map1.put(entry.getKey(),listrece);
			}
		}
		return map1;
	}
	//052
	public static Map<Long,String> getQMapByLong(Map<Long, Long> map1,
			Map<Long, String> map2) {
		Map<Long,String> newMap = new HashMap<Long,String>();
		Iterator<Entry<Long, Long>> iter1 = map1.entrySet().iterator();
		while (iter1.hasNext()) {
			Map.Entry<Long,Long> entry = (Map.Entry<Long,Long>) iter1.next();
			if(map2.containsKey(entry.getKey())){
				//合并
			   String map2Value = map2.get(entry.getKey());
			   Long temp = entry.getValue();
			   newMap.put(temp,map2Value);
			}
		}
		return newMap;
	}
	//053
	/**合并list*/
	public static List<Object> mergeList(List<Object> l1,List<Object> l2){
		List<Object> temp = new ArrayList<Object>(l1);
		temp.retainAll(l2);
		l1.removeAll(temp);
		l2.removeAll(temp);
		List<Object> l3 = new ArrayList<Object>();
		l3.addAll(l1);
		l3.addAll(l2);
		l3.addAll(temp);
		return l3;
		
	}
	//054
	@SuppressWarnings("unchecked")
	public  String getGlobalParamValue(String param) throws SQLException {
		String value = null;
		List<Long> gpIds = this.findByQuery("SELECT gp.id FROM GlobalParam gp WHERE gp.paramId=:param",
				new String[]{"param"}, new Object[]{param});
		if(gpIds.size() > 0){
			GlobalParam  gp = this.load(GlobalParam.class, gpIds.get(0));
			value = gp.getValue();
		}
		return value;
	}
	//055
	public static void deleteFile(File file){ 
	   if(file.exists()){                         
		    if(file.isFile()){                     //判断是否是文件
		        file.delete();                     
		    }else if(file.isDirectory()){          //否则如果它是一个目录
		        File files[] = file.listFiles();          
		        for(int i=0;i<files.length;i++){           
		            deleteFile(files[i]);             
		        } 
		    } 
		    file.delete(); 
	   }
	} 
	//056
	public static String encoder(String str) {
		try {
			return URLEncoder.encode(str , ENCODING);
//			return StringUtils.replace(s , "%" , "_VT_");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	//057
	public static String decoder(String str) {
		try {
//			str = StringUtils.replace(str, "_VT_" , "%");
			return URLDecoder.decode(str , ENCODING);
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}
	//058
	public String numberCode(int length){
		Date date = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String ss = sdf.format(date);
		
    	String base = "0123456789";
        Random random = new Random();  
        StringBuffer sb = new StringBuffer();  
        for (int i = 0; i < length; i++) {  
            int number = random.nextInt(base.length());  
            sb.append(base.charAt(number));  
        }  
        return sb.toString();  
	}
	//059
	private static String formateHql(String hql){
		String hqlStatement = hql;
		int leftToenailIndex = hqlStatement.indexOf("(");
		if(leftToenailIndex != -1){
			hqlStatement = StringUtils.replace(hqlStatement,"("," ( ");
		}
		int rightToenailIndex = hqlStatement.indexOf(")");
		if(rightToenailIndex != -1){
			hqlStatement = StringUtils.replace(hqlStatement,")"," ) ");
		}
		int enterIndex = hqlStatement.indexOf("\n");
		if(enterIndex != -1){
			hqlStatement = StringUtils.replace(hqlStatement,"\n"," \n");
		}
		return hqlStatement;
	}
	//060
	public static Boolean isNumber(String value){
		if(StringUtils.isEmpty(value)){
			return false;
		}
		return value.matches("[0-9]+");
	}
	//065
	public static boolean isNumeric1(String str){
		  Pattern pattern = Pattern.compile("[0-9]*");
		  return pattern.matcher(str).matches();
	}
	//061
	public static Boolean isContainLetter(String value){
		Pattern p = Pattern.compile(value);  
        Matcher m = p.matcher("[a-zA-Z]");  
		return m.find();
	}
	//062
	public static Boolean isLetter(String value){
		return value.matches("[a-zA-Z]");
	}
	//063
	public static String replaceAllNumber(String value){
		return value.replaceAll("\\d+","");
	}
	//064
	public static String removeStartsWithStr(String value,String remValue){
		while(value.startsWith(remValue)){
			value = StringUtils.substringAfter(value,remValue);
		}
		return value ;
	}
		//066
		public static List<Boolean> booleans(String djNO,int size){
			String[] ss = StringUtils.split(djNO, "//.");
			Map<String,Boolean> boof = new HashMap<String,Boolean>();
			for(int i = 0 ; i<ss.length ;i++){//{3=false, 2=false, 1=false, 5=false, 4=false}
				boof.put(ss[i], false);
			}
			List<Boolean> bool = new ArrayList<Boolean>();
			for(int i = 1 ; i<=size; i++){
				if(boof.containsKey(i+"")){
					bool.add(false);
				}else{
					bool.add(true);
				}
			}
			return bool;
			
		}
		//067
		 //================================解析txt档并写入新的txt档=================================================
	    public static Map<Integer,List<Object>>  readTxt(File file){
	    	//jt.readTxt(new File("C:/Users/Administrator/Desktop/aaaaa.txt"));
	    	BufferedReader br = null;
	    	Map<Integer,List<Object>> map = new HashMap<Integer, List<Object>>();
	    	List<Object> values = null;
	    	int key = 1;
	    	try {
	    		InputStreamReader read = new InputStreamReader(new FileInputStream(file), "GBK");
	    		br = new BufferedReader(read);
	    		String temp = null;
	    		while ((temp = br.readLine()) != null) {
	    			if("".equals(temp)){
	    				continue;
	    			}
	    			values = new ArrayList<Object>();
	    			StringTokenizer st = new StringTokenizer(temp);
	    			while(st.hasMoreElements()){
	    				String num = st.nextToken("\t").trim();
	    				values.add(num);
	    			}
	    			map.put(key, values);
	    			key++;
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					if(null != br){
						br.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return map;
	    }
	    //068
	    public static List<Object[]> excuteReadTxt(String filePath){
			Map<Integer,List<Object>> map = readTxt(new File(filePath));
			List<Object[]> objs = new ArrayList<Object[]>();
			Iterator<Entry<Integer, List<Object>>> iterator = map.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Integer, List<Object>> entery = iterator.next();
				int key = entery.getKey();
				List<Object> values = entery.getValue();
				Object[] ss = new Object[values.size()+10];
				for(int i = 0 ; i<values.size() ; i++){
					ss[i] = values.get(i);
				}
				objs.add(ss);
			}
			return objs;
	    }
	    //069
	    public static void createTxt(List<Object[]> values,int objSize,String file
	    		,String endLine,String spilt,String character){
	    	StringBuffer row=null;
	    	OutputStreamWriter osw = null;
	    	try {
	    		osw = new OutputStreamWriter(new FileOutputStream(file,true),character);
	    		for(int i=0 ; i<values.size() ; i++){
	    			row = new StringBuffer();
	    			Object[] obj = values.get(i);
	    			for(int j = 0 ; j<objSize ; j++){
	    				if(objSize-j == 1){
	    					row.append(obj[j]+endLine);//换行
	    				}else{
	    					row.append(obj[j]+spilt);//"\t":tab键
	    				}
	    			}
	    			osw.write(row.toString());
	    		}
	    		values.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
	       	 if(osw!=null)//if(osw!=null)
	             try {
	             	osw.close();//osw.close();
	             } catch (IOException e) {
	                 e.printStackTrace();
	             }
	      }
	    }
	  //070
	  //================================解析txt档并写入新的txt档=================================================
	  //================================类型转换=================================================
	    /**string转list<Object[]>
	     * @param requestContent='xx,{a=1,b=2,c=3......}'
	     * @param requestContent:用一个逗号分隔的字符串,第二个元素是map,用{}包含了若干个a=1组合形式的数据*/
	    public static List<Object[]> string2Obj(String requestContent){
	    	List<Object[]> values = new ArrayList<Object[]>();
	    	String xx = StringUtils.substringBefore(requestContent, ",");
			String map = StringUtils.substringAfter(requestContent, ",");//{A=1.0, B=2.0, C=3.0}
			String m = StringUtils.substringBetween(map, "{", "}");//A=1.0,B=2.0,C=3.0
			String[] ss = m.split(",");//[A=1.0, B=2.0, C=3.0]
			for(int i = 0;i<ss.length;i++){
				String a = StringUtils.substringBefore(ss[i], "=");
				String quitity = StringUtils.substringAfter(ss[i], "=");
				String[] obj = new String[]{
//						xx+","+a+","+quitity+","+"-"
						xx.trim()+"\t"+a.trim()+"\t"+quitity.trim()+"\t"+"-"
				};
				values.add(obj);
			}
			return values;
	    }
	    //071
	    public static void markTxt(String message,String txtPath){
			List<Object[]> values = new ArrayList<Object[]>();
			values.add(new Object[]{
					message+JavaTools.format(new Date(), "yyyy-MM-dd HH:mm:ss")
			});
			JavaTools.createTxt(values, 
					values.size(), txtPath, JavaTools.enter, "\t", "utf-8");
		}
	    //072
	    public static long getDistDates(Date startDate,Date endDate)    
	    {  
	        long totalDate = 0;  
	        Calendar calendar = Calendar.getInstance();  
	        calendar.setTime(startDate);  
	        long timestart = calendar.getTimeInMillis();  
	        calendar.setTime(endDate);  
	        long timeend = calendar.getTimeInMillis();  
	        totalDate = Math.abs((timeend - timestart))/(1000*60*60*24);  
	        return totalDate;  
	    }
	    //073
	    public static Integer moveFileToDel(String fromDir,int deleteDays){
			File srcDir = new File(fromDir);
			if (!srcDir.exists()) {
				return 0;
			}
			File[] files = srcDir.listFiles();
			if (files == null || files.length <= 0) {
				return 0;
			}
			int l = 0;
			Date today = new Date();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					try {
						File ff = files[i];
				        long time=ff.lastModified();
					    Calendar cal=Calendar.getInstance();   
				        cal.setTimeInMillis(time);   
				        Date lastModified = cal.getTime();
				      //(int)(today.getTime() - lastModified.getTime())/86400000;
				        long days = getDistDates(today, lastModified);
				        if(days>=deleteDays){
				        	files[i].delete();
				        	l++;
				        }
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return l;
		}
	    //074
	    public static String markDir(String filePath){
	    	File shipmentFile = new File(filePath);
	    	if(!shipmentFile.exists()){
	    		shipmentFile.mkdir();
	    	}
			return filePath;
	    }
	    //075
	    public static double floor(double a){
	    	double b = Math.floor(a);
	    	if(a-b>=0.5){
	    		b += 1;
	    	}
			return b;
	    }
	  //076
	    public static Date beforeOneDate(Date beginDate){
	    	Calendar calendar = Calendar.getInstance();
			calendar.setTime(beginDate);
			calendar = DateUtils.truncate(calendar, Calendar.DATE);
//			Date currentDate = calendar.getTime();
			calendar.add(Calendar.DATE, -1);
			Date beforeOneDate = calendar.getTime();
			return beforeOneDate;
	    }
	  //077
	    public static List<String> charList(List<String> fromLis){
	    	List<String> toLis = new ArrayList<String>();
	    	if(fromLis!=null&&fromLis.size()>0){
				for(String s : fromLis){
					toLis.add("'"+s+"'");
				}
			}
			return toLis;
	    }
	    //078
	    public static String mosaicDate(int day,Date someDay,String format){
	    	String[] value = JavaTools.format(someDay, format).split("-");//JavaTools.y_m_d
	    	String date = null;
	    	if(day>9){
	    		date = value[0]+"-"+value[1]+"-"+day;
	    	}else if(day>0 && day<10){
	    		date = value[0]+"-"+value[1]+"-0"+day;
	    	}
			return date;
	    }
	    //079
	    public static void createTxt(String file,StringBuffer row,String character){
	    	OutputStreamWriter osw = null;
	    	try {
	    		osw = new OutputStreamWriter(new FileOutputStream(file,true),character);
	    		osw.write(row.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
		       	 if(osw!=null)//if(osw!=null)
		             try {
		             	osw.close();//osw.close();
		             } catch (IOException e) {
		                 e.printStackTrace();
		             }
		    }
	    }
	    //080
  		public static void smbPut(String remoteUrl, String localFilePath) {
  		}  
  		//081
  		public static void smbPutLocal(String remoteUrl, String localPath,String fileName){
  		}
  		//082
  		public static void smbDeleteFile(String remoteUrl,String fileName){
  		}
  		//083
  		public static Boolean patternYMD(String value){
  	    	Pattern pattern = Pattern.compile(patterString);
  	    	Matcher m = pattern.matcher(value);
  	    	return m.find();
  		}
  		public static String spiltLast(String exception,String spilt){
  			String[] errors =exception==null?null:exception.split(spilt);
			if(errors!=null && errors.length>0){
				exception = errors[errors.length-1];
			}
			return exception;
  		}
	    public static void main(String[] args) throws ParseException {
	    	System.out.println(JavaTools.isNumber("222eee"));
	    	System.out.println(JavaTools.isNumber("www"));
	    	System.out.println(JavaTools.isNumber("2w3e"));
	    	System.out.println(JavaTools.isNumber("null"));
	    	System.out.println(JavaTools.isNumber(""));
	    	System.out.println(JavaTools.isNumber(" "));
	    	System.out.println(JavaTools.isNumber("234"));
	    	System.out.println("------------------------------------");
	    	System.out.println(JavaTools.isNumeric1("222eee"));
	    	System.out.println(JavaTools.isNumeric1("www"));
	    	System.out.println(JavaTools.isNumeric1("2w3e"));
	    	System.out.println(JavaTools.isNumeric1("null"));
	    	System.out.println(JavaTools.isNumeric1(""));
	    	System.out.println(JavaTools.isNumeric1(" "));
	    	System.out.println(JavaTools.isNumeric1("234"));
	    	System.out.println("------------------------------------");
//	    	try {
//				System.out.println(new String("A-ÍË»õ".getBytes("ISO8859-1"), "UTF-8"));
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	    	/*StringBuffer row=new StringBuffer();
	    	row.append("organization="+WmsBillMode.organization+JavaTools.enter);
	    	row.append("template="+WmsBillMode.template001+JavaTools.enter);
	    	row.append("L20002#信息服务费#201509,增值服务费,2015-09-01,L20002,锦州光和密封实业有限公司,-,信息服务费,MONTH,2,0"+JavaTools.enter);
			OutputStreamWriter osw = null;
	    	try {
	    		osw = new OutputStreamWriter(new FileOutputStream( WmsBillMode.txtPath+"/fdj_"+"L20002"+"_"+JavaTools.format(new Date(), JavaTools.ym)+"_303.txt",true),"utf-8");
	    		osw.write(row.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
	       	 if(osw!=null)//if(osw!=null)
	             try {
	             	osw.close();//osw.close();
	             } catch (IOException e) {
	                 e.printStackTrace();
	             }
	      }*/
	    	
	    	
	    	
	    	//org.apache.commons.lang.StringUtils
	    	//(6ooo#~~~)第一个#开始截取(从左起)
	    	System.out.println(
	    			StringUtils.substringAfter("234556#6ooo#~~~", "#"));
	    	//(~~~)最后一个#开始截取
	    	System.out.println(
	    			StringUtils.substringAfterLast("234556#6bbb#~~~", "#"));
	    	//(234556)第一个#往左截取(从左起)
	    	System.out.println(
	    			StringUtils.substringBefore("234556#6bbb#~~~", "#"));
	    	//(234556#6bbb)最后一个#往左截取
	    	System.out.println(
	    			StringUtils.substringBeforeLast("234556#6bbb#~~~", "#"));
	    	//(6bbb)截取两个#之间的
	    	System.out.println(
	    			StringUtils.substringBetween("234556#6bbb#~~~", "#"));
	    	//(6bbb#~)截取第一个#与第一个$之间的(从左起)
	    	System.out.println(
	    			StringUtils.substringBetween("$234556#6bbb#~$~~$","#","$"));
	    	
	        // TODO Auto-generated method stub  
	       /* SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	        Date d1=sdf.parse("2012-09-08 10:10:10");  
	        Date d2=sdf.parse("2012-09-15 00:00:00");  
	        System.out.println(daysBetween(d1,d2));  
	  
	        System.out.println(daysBetween("2012-09-08 10:10:10","2012-09-15 00:00:00"));  */
	    } 
}
