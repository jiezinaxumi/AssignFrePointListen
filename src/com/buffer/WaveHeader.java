package com.buffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Boris
 * 
 * wave 文件头
 *
 * 2016年8月3日
 */
public class WaveHeader {
	public final static char fileID[] = { 'R', 'I', 'F', 'F' };//4字节 "RIFF"标志
	public static int fileLength;                              //4文件长度  文件总长度 - 8
	public static char wavTag[] = { 'W', 'A', 'V', 'E' };      //4字节 "WAVE"标志 
	public static char FmtHdrID[] = { 'f', 'm', 't', ' ' };    //4字节  "fmt"标志 
	public static int FmtHdrLeth = 16;                         //4字节 过渡字节（不定）
	public static short FormatTag = 1;                         //2字节  格式类型（10H为PCM形式的声音数据)
	public static short Channels = 1;                          //2字节 通道数，单声道为1，双声道为2
	public static int SamplesPerSec;                           //2字节 采样率（每秒样本数），表示每个通道的播放速度
	public static int AvgBytesPerSec;                          //4字节   波形音频数据传送速率，其值为通道数×每秒数据位数×每样本的数据位数／8。播放软件利用此值可以估计缓冲区的大小。 
	public static short BlockAlign = 2;                        //2字节  数据块的调整数（按字节算的），其值为通道数×每样本的数据位值／8。播放软件需要一次处理多个该值大小的字节数据，以便将其值用于缓冲区的调整。 
	public static short BitsPerSample = 16;                    //2字节  每样本的数据位数，表示每个声道中各个样本的数据位数。如果有多个声道，对每个声道而言，样本大小都一样。 
	public static char DataHdrID[] = { 'd', 'a', 't', 'a' };   //4字节 数据标记符＂data＂
	public static int DataHdrLeth;//4字节 语音数据的长度  文件总长度-44

	/** 
	 * @Method: getHeader 
	 * @Description: 取wave文件头
	 * @param fileLength 数据区长度
	 * @param samplesPerSec 采样率
	 * @return wave文件头
	 * byte[]
	 */ 
	public static byte[] getHeader(int fileLength, int samplesPerSec) {
		WaveHeader.fileLength = fileLength + 44 - 8;
		WaveHeader.SamplesPerSec = samplesPerSec;
		WaveHeader.AvgBytesPerSec = samplesPerSec * 2;
		WaveHeader.DataHdrLeth = fileLength;
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			writeChar(bos, fileID);
			writeInt(bos, fileLength);
			writeChar(bos, wavTag);
			writeChar(bos, FmtHdrID);
			writeInt(bos, FmtHdrLeth);
			writeShort(bos, FormatTag);
			writeShort(bos, Channels);
			writeInt(bos, SamplesPerSec);
			writeInt(bos, AvgBytesPerSec);
			writeShort(bos, BlockAlign);
			writeShort(bos, BitsPerSample);
			writeChar(bos, DataHdrID);
			writeInt(bos, DataHdrLeth);
			bos.flush();
			byte[] r = bos.toByteArray();
			bos.close();
			return r;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private static void writeShort(ByteArrayOutputStream bos, int s)throws IOException {
		byte[] mybyte = new byte[2];
		mybyte[1] = (byte) ((s << 16) >> 24);
		mybyte[0] = (byte) ((s << 24) >> 24);
		bos.write(mybyte);
	}

	private static void writeInt(ByteArrayOutputStream bos, int n)throws IOException {
		byte[] buf = new byte[4];
		buf[3] = (byte) (n >> 24);
		buf[2] = (byte) ((n << 8) >> 24);
		buf[1] = (byte) ((n << 16) >> 24);
		buf[0] = (byte) ((n << 24) >> 24);
		bos.write(buf);
	}

	private static void writeChar(ByteArrayOutputStream bos, char[] id) {
		for (int i = 0; i < id.length; i++) {
			char c = id[i];
			bos.write(c);
		}
	}
}
