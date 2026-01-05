package com.example.springai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * SpringAI 应用启动类
 * 基于 SpringBoot + Vue + AI大模型 的简历生成+职业推荐系统
 * 
 * @author 半夜撕代码
 * @version 1.0.0
 * @since 2024
 */
@MapperScan("com.example.springai.mapper") // 指定 Mapper 所在包
@SpringBootApplication
public class SpringAiApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpringAiApplication.class, args);
		printCopyright();
	}

	/**
	 * 打印版权信息
	 */
	private static void printCopyright() {
		System.out.println();
		System.out.println("  ============================================================");
		System.out.println("  |                                                          |");
		System.out.println("  |    应用启动成功！                                       |");
		System.out.println("  |                                                          |");
		System.out.println("  |    周辉 版权所有 © 2025                           |");
		System.out.println("  |    联系方式: 81370071@qq.com                   |");
		System.out.println("  |                       |");
		System.out.println("  |                                                          |");
		System.out.println("  ============================================================");
		System.out.println();
	}

}
