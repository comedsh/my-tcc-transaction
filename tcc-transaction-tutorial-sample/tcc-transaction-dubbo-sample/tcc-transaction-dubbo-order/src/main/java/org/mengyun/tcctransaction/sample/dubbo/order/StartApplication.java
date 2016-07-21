package org.mengyun.tcctransaction.sample.dubbo.order;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

/**
 * 
 * @author 商洋
 *
 */
public class StartApplication {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception{
		
		Log4jConfigurer.initLogging( StartApplication.class.getResource("/log/log4j.xml").getFile() ); // manually set the log4j path.
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "classpath*:config/spring/local/appcontext-*.xml", "classpath:tcc-transaction.xml" );
		
		context.start();
		
		System.in.read(); // 按任意键退出
	    
	}
	
}
