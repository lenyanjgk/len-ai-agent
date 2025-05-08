package com.lenyan.lenaiagent.app;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LovePartnerDataGenerator {
    private static final String[] GENDERS = {"男", "女"};
    private static final String[] CONSTELLATIONS = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
            "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"};
    private static final String[] PROFESSIONS = {"大学生（计算机）", "大学生（英语）", "大学生（艺术设计）",
            "大学生（金融）", "大学生（汉语言）", "大学生（生物）", "大学生（体育）",
            "职场新人（教师）", "职场新人（程序员）", "职场新人（设计师）", "职场新人（运营）"};
    private static final String[] HOBBIES = {
            "读书（村上春树）、咖啡品鉴、摄影",
            "心理学实验、瑜伽、密室逃脱",
            "编程、篮球、科技论坛",
            "绘画、看展、手账制作",
            "旅行、脱口秀、宠物救助",
            "投资模拟、辩论赛、钢琴",
            "古风文学、汉服、书法练习",
            "数据分析、音乐剧、滑雪",
            "足球、健身、户外运动",
            "实验室科研、烘焙、纪录片观看"
    };
    private static final String[] EDUCATION_BACKGROUNDS = {"本科在读", "硕士在读", "本科毕业"};
    private static final String[] INCOME_RANGES = {"无", "奖学金", "5-8k", "8-12k"};
    private static final String[] CITIES = {"北京", "上海", "深圳", "广州", "杭州", "成都", "南京", "武汉", "天津", "重庆"};

    public static void main(String[] args) {
        int dataCount = 10000; // 生成 100 条数据，可根据需要调整
        List<String[]> data = generateData(dataCount);
        createExcelFile(data, "恋爱对象信息表.xlsx");
    }

    private static List<String[]> generateData(int count) {
        List<String[]> data = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String gender = GENDERS[random.nextInt(GENDERS.length)];
            int age = random.nextInt(7) + 18; // 年龄范围 18 - 24
            String constellation = CONSTELLATIONS[random.nextInt(CONSTELLATIONS.length)];
            String profession = getProfession(age);
            String hobby = HOBBIES[random.nextInt(HOBBIES.length)];
            String educationBackground = getEducationBackground(age);
            String incomeRange = getIncomeRange(educationBackground);
            String city = CITIES[random.nextInt(CITIES.length)];
            int height = gender.equals("女") ? random.nextInt(31) + 155 : random.nextInt(21) + 170;
            int weight = gender.equals("女") ? random.nextInt(26) + 40 : random.nextInt(26) + 60;

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

    private static String getProfession(int age) {
        if (age <= 22) {
            return PROFESSIONS[new Random().nextInt(7)];
        } else {
            return PROFESSIONS[new Random().nextInt(4) + 7];
        }
    }

    private static String getEducationBackground(int age) {
        if (age <= 22) {
            return EDUCATION_BACKGROUNDS[0];
        } else if (age == 23) {
            return EDUCATION_BACKGROUNDS[1];
        } else {
            return EDUCATION_BACKGROUNDS[2];
        }
    }

    private static String getIncomeRange(String educationBackground) {
        if (educationBackground.equals("本科在读")) {
            return INCOME_RANGES[0];
        } else if (educationBackground.equals("硕士在读")) {
            return INCOME_RANGES[1];
        } else {
            return INCOME_RANGES[new Random().nextInt(2) + 2];
        }
    }

    private static String generateIdCard(int age) {
        int birthYear = 2025 - age;
        StringBuilder idCard = new StringBuilder();
        idCard.append("110101"); // 虚拟地区码
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
        String[] prefixes = {"13", "15", "17", "18"};
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