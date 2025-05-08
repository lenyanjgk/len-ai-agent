package com.lenyan.lenaiagent.app;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComprehensiveLovePartnerGenerator {
    private static final String[] GENDERS = {"男", "女", "其他"};
    private static final String[] CONSTELLATIONS = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
            "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"};
    private static final String[] PROFESSIONS = {
            "大学生", "研究生", "医生", "教师", "程序员", "设计师", "会计", "律师", "厨师", "司机",
            "警察", "消防员", "服务员", "快递员", "销售员", "演员", "歌手", "运动员", "农民", "工人"
    };
    private static final String[] HOBBIES = {
            "读书", "绘画", "音乐", "舞蹈", "运动", "旅游", "美食", "摄影", "游戏", "手工",
            "宠物", "电影", "戏剧", "书法", "瑜伽", "钓鱼", "骑行", "登山", "滑雪", "游泳"
    };
    private static final String[] EDUCATION_BACKGROUNDS = {
            "小学", "初中", "高中", "中专", "大专", "本科", "硕士", "博士"
    };
    private static final String[] INCOME_RANGES = {
            "无收入", "1 - 3k", "3 - 5k", "5 - 8k", "8 - 12k", "12 - 20k", "20k 以上"
    };
    private static final String[] CITIES = {
            "北京", "上海", "广州", "深圳", "成都", "杭州", "重庆", "武汉", "西安", "苏州",
            "天津", "南京", "长沙", "郑州", "东莞", "青岛", "沈阳", "宁波", "昆明", "合肥",
            "佛山", "福州", "哈尔滨", "济南", "温州", "南宁", "长春", "泉州", "石家庄", "贵阳",
            "常州", "南通", "嘉兴", "太原", "徐州", "南昌", "潍坊", "烟台", "绍兴", "台州"
    };

    public static void main(String[] args) {
        int dataCount = 10000; // 生成 500 条数据，可按需调整
        List<String[]> data = generateData(dataCount);
        createExcelFile(data, "comprehensive_love_partners.xlsx");
    }

    private static List<String[]> generateData(int count) {
        List<String[]> data = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String gender = GENDERS[random.nextInt(GENDERS.length)];
            int age = random.nextInt(81) + 18; // 年龄范围 18 - 98 岁
            String constellation = CONSTELLATIONS[random.nextInt(CONSTELLATIONS.length)];
            String profession = PROFESSIONS[random.nextInt(PROFESSIONS.length)];
            String hobby = getRandomHobbies(random);
            String educationBackground = EDUCATION_BACKGROUNDS[random.nextInt(EDUCATION_BACKGROUNDS.length)];
            String incomeRange = getIncomeRange(age, profession, educationBackground);
            String city = CITIES[random.nextInt(CITIES.length)];
            int height = getRandomHeight(gender, random);
            int weight = getRandomWeight(gender, height, random);

            String idCard = generateIdCard(age);
            String phoneNumber = generatePhoneNumber();

            data.add(new String[]{
                    idCard, phoneNumber, gender, String.valueOf(age),
                    String.valueOf(height), String.valueOf(weight),
                    constellation, profession, hobby,
                    educationBackground, incomeRange, city
            });
        }
        return data;
    }

    private static String getRandomHobbies(Random random) {
        int hobbyCount = random.nextInt(3) + 1;
        StringBuilder hobbies = new StringBuilder();
        for (int i = 0; i < hobbyCount; i++) {
            if (i > 0) {
                hobbies.append("、");
            }
            int index = random.nextInt(HOBBIES.length);
            hobbies.append(HOBBIES[index]);
        }
        return hobbies.toString();
    }

    private static String getIncomeRange(int age, String profession, String educationBackground) {
        Random random = new Random();
        if (age < 22 && (profession.equals("大学生") || profession.equals("研究生"))) {
            return "无收入";
        }
        int educationIndex = getEducationIndex(educationBackground);
        if (educationIndex <= 2) {
            return INCOME_RANGES[random.nextInt(3)];
        } else if (educationIndex <= 4) {
            return INCOME_RANGES[random.nextInt(3) + 1];
        } else if (educationIndex <= 6) {
            return INCOME_RANGES[random.nextInt(3) + 2];
        } else {
            return INCOME_RANGES[random.nextInt(2) + 4];
        }
    }

    private static int getEducationIndex(String educationBackground) {
        for (int i = 0; i < EDUCATION_BACKGROUNDS.length; i++) {
            if (EDUCATION_BACKGROUNDS[i].equals(educationBackground)) {
                return i;
            }
        }
        return 0;
    }

    private static int getRandomHeight(String gender, Random random) {
        if (gender.equals("男")) {
            return random.nextInt(31) + 160; // 男性身高范围 160 - 190cm
        } else if (gender.equals("女")) {
            return random.nextInt(26) + 150; // 女性身高范围 150 - 175cm
        } else {
            return random.nextInt(36) + 150; // 其他性别身高范围 150 - 185cm
        }
    }

    private static int getRandomWeight(String gender, int height, Random random) {
        if (gender.equals("男")) {
            return (int) (height - 105 + random.nextInt(20) - 10); // 男性体重范围参考身高
        } else if (gender.equals("女")) {
            return (int) (height - 110 + random.nextInt(20) - 10); // 女性体重范围参考身高
        } else {
            return (int) (height - 107 + random.nextInt(20) - 10); // 其他性别体重范围参考身高
        }
    }

    private static String generateIdCard(int age) {
        int birthYear = 2025 - age;
        StringBuilder idCard = new StringBuilder();
        String[] areaCodes = {"110000", "310000", "440000", "510000", "330000", "120000", "420000",
                "610000", "320000", "430000", "410000", "441900", "370000", "210000", "330200", "530000",
                "340000", "440600", "350000", "230000", "370100", "450000", "220000", "350500", "130000",
                "520000", "320400", "320600", "330400", "140000", "320300", "360000", "370700", "370600",
                "330600", "331000"};
        idCard.append(areaCodes[new Random().nextInt(areaCodes.length)]);
        idCard.append(String.format("%04d", birthYear));
        idCard.append(String.format("%02d", new Random().nextInt(12) + 1));
        idCard.append(String.format("%02d", new Random().nextInt(28) + 1));
        for (int i = 0; i < 4; i++) {
            idCard.append(new Random().nextInt(10));
        }
        return idCard.toString();
    }

    private static String generatePhoneNumber() {
        StringBuilder phoneNumber = new StringBuilder();
        String[] prefixes = {"13", "15", "17", "18", "19"};
        phoneNumber.append(prefixes[new Random().nextInt(prefixes.length)]);
        for (int i = 0; i < 9; i++) {
            phoneNumber.append(new Random().nextInt(10));
        }
        return phoneNumber.toString();
    }

    private static void createExcelFile(List<String[]> data, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("恋爱对象信息");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"身份证号", "手机号", "性别", "年龄", "身高（cm）", "体重（kg）", "星座", "职业", "兴趣爱好", "教育背景", "收入范围", "所在城市"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData[j]);
                }
            }

            // 调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 保存文件
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}