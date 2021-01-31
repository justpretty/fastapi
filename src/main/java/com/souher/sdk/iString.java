package com.souher.sdk;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public interface iString
{
    static String matchChineseDigits(String raw)
    {
        String pattern="[^零一二三四五六七八九十百千万亿兆]";
        String result= Pattern.compile(pattern).matcher(raw).replaceAll("").trim();
        return result;
    }

    static String getApiTag(Class<?> cls)
    {
        return cls.getSimpleName().toLowerCase();
    }
    static int translabeChineseDigits(String chineseNum)
    {
         char[] cnArr = new char [] {'一','二','三','四','五','六','七','八','九'};
         char[] chArr = new char [] {'十','百','千','万','亿'};
         String allChineseNum = "零一二三四五六七八九十百千万亿";
        int result = 0;
        int temp = 1;//存放一个单位的数字如：十万
        int count = 0;//判断是否有chArr
        for (int i = 0; i < chineseNum.length(); i++) {
            boolean b = true;//判断是否是chArr
            char c = chineseNum.charAt(i);
            for (int j = 0; j < cnArr.length; j++) {//非单位，即数字
                if (c == cnArr[j]) {
                    if(0 != count){//添加下一个单位之前，先把上一个单位值添加到结果中
                        result += temp;
                        temp = 1;
                        count = 0;
                    }
                    // 下标+1，就是对应的值
                    temp = j + 1;
                    b = false;
                    break;
                }
            }
            if(b){//单位{'十','百','千','万','亿'}
                for (int j = 0; j < chArr.length; j++) {
                    if (c == chArr[j]) {
                        switch (j) {
                            case 0:
                                temp *= 10;
                                break;
                            case 1:
                                temp *= 100;
                                break;
                            case 2:
                                temp *= 1000;
                                break;
                            case 3:
                                temp *= 10000;
                                break;
                            case 4:
                                temp *= 100000000;
                                break;
                            default:
                                break;
                        }
                        count++;
                    }
                }
            }
            if (i == chineseNum.length() - 1) {//遍历到最后一个字符
                result += temp;
            }
        }
        return result;
    }

    static String getUnderlineString(String str)
    {
        String a= str.replaceAll("[A-Z]", "_$0").toLowerCase();
        if(a.charAt(0)=='_')
        {
            return a.substring(1);
        }
        return a;
    }

    static String getWebPathByClass(Class cls)
    {
        String a= cls.getSimpleName().replaceAll("[A-Z]", "/$0").toLowerCase();

        return a;
    }

    static ArrayList<String> splitByByteLength(String rawString, int length) throws UnsupportedEncodingException
    {
        ArrayList<String> list = new ArrayList<String>();
        String link_text="";
        if(rawString!=null)
        {
            link_text= rawString;
        }
        while (link_text.getBytes(StandardCharsets.UTF_8).length > length)
        {
            String out= "";
            for(int i=0;i<link_text.length();i++)
            {
                String a=link_text.substring(i,i+1);
                String b=out+a;
                if(b.getBytes(StandardCharsets.UTF_8).length>length)
                {
                    link_text=link_text.substring(i);
                    break;
                }
                out=b;
            }
            list.add(out);
        }
        if(link_text.length()>0)
        {
            list.add(link_text);
        }
        return list;
    }
    static String getPinyin(String input) {

        StringBuilder pinyin = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
            char c = input.charAt(i);
            String[] pinyinArray = null;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                iApp.error(e);
            }
            if (pinyinArray != null) {
                pinyin.append(pinyinArray[0]);
            } else if (c != ' ') {
                pinyin.append(input.charAt(i));
            }
        }
        return pinyin.toString().trim().toUpperCase();
    }

    static String getSemiangle(String src) {
        char[] c = src.toCharArray();
        for (int index = 0; index < c.length; index++) {
            if (c[index] == 12288) {// 全角空格
                c[index] = (char) 32;
            } else if (c[index] > 65280 && c[index] < 65375) {// 其他全角字符
                c[index] = (char) (c[index] - 65248);
            }
        }
        return String.valueOf(c);
    }

    static  String md5(String s) {
        StringBuilder buf = new StringBuilder("");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            iApp.error(e);
        }
        md.update(s.getBytes());
        byte b[] = md.digest();
        int i;
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        return buf.toString().toUpperCase();
    }
}
