package com.dx.helpCode;

import java.io.IOException;

public class GtcgSocketTemplate {
	
	/**
	 * 单向报文发送
	 *
	 * @return
	 */
	private String shortTwoWay(GtcgSocketRequest gtcgSocketRequest) throws IOException {
		try {
			gtcgSocketRequest.send();
			return gtcgSocketRequest.receive();
		} finally {
			try {
				gtcgSocketRequest.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}