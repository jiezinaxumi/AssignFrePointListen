package com.util;

/**
 * @author Boris
 * @description 常量
 * 2016年8月9日
 */
public class Constance {
	//接收机
	public static final class Reveiver{
		//接收机状态
		public final static int FREE      = 1200;
		public final static int BUSY      = 1201;
		public final static int EXCEPTION = 1202;
	}
	
	//任务
	public static final class Task{
		public final static int WAIT   = 70;
		public final static int DOING  = 71;
		public final static int DONE   = 72;
		public final static int FAILED = 73;
	}
	
	//频率优先级
	public static final class FreqPri{
		public final static int URGENCY = 110; //加急
		public final static int NORMAL  = 111;
	}

}
