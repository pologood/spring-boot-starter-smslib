/*
 * Service.java
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.smslib.spring.boot.sms;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.smslib.Message.MessageEncodings;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

public class SmsMain {

	private static Service srv = null;

	public static boolean creatService() {
		srv = Service.getInstance();
		
//		SerialModemGateway gateway = new SerialModemGateway("SMS", "COM3",
//				9600, "", ""); 
		//Win32Driver
		
		Map<String,String> map = getProperties("sms.properties");
		
		System.out.println(" map.get(\"comPort\")="+ map.get("comPort"));
		SerialModemGateway gateway = new SerialModemGateway("SMS", map.get("comPort"),
				Integer.parseInt(map.get("baudRate")), map.get("manufacturer"), map.get("model"));

		gateway.setInbound(true);
		gateway.setOutbound(true);
		try {
			srv.S.SERIAL_POLLING = true;
			srv.addGateway(gateway);

			srv.startService();
			System.out.println("Modem connected.");

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static Service getService() {
		if (srv == null) {
			creatService();
		}
		return srv;
	}

	public static void main(String args[]) {
		// creatService();
		sendSms("133*****", "发送测试啊测试");
		// close();
//		System.out.println( System.getProperty("java.library.path"));
	}

	public static boolean sendSms(String mobile, String content) {
		if (srv == null) {
			if(!creatService()){
				close();
				srv=null;
				return false;
			}
		}
		OutboundMessage msg = new OutboundMessage(mobile, content);
		msg.setEncoding(MessageEncodings.ENCUCS2);
		try {
			srv.sendMessage(msg);
			System.out.println(msg);
		} catch (Exception ex) {
			try {
				System.out.println("发送失败   重新发送   ...");
				srv.sendMessage(msg);
				System.out.println(msg);
			} catch (Exception ex2) {
				return false;
			}
		}
		return true;
	}

	public static void close() {
		try {
			System.out.println("Modem disconnected.");
			srv.stopService();
		} catch (Exception ex) {

		}
	}

	public static Map<String,String> getProperties(String path) {
		Properties p = new Properties();
		InputStream inputStream = SmsMain.class.getResourceAsStream(path);
		
		try {
			p.load(inputStream);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Map<String,String> map = new HashMap<String,String>();
		Iterator<Object> it =p.keySet().iterator();
		String key="";
		while(it.hasNext()){
			key = it.next().toString();
			map.put(key, p.getProperty(key).trim());
		}
		return map;

	}
}
