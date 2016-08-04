package com.buffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Boris
 * 
 * wave �ļ�ͷ
 *
 * 2016��8��3��
 */
public class WaveHeader {
	public final static char fileID[] = { 'R', 'I', 'F', 'F' };//4�ֽ� "RIFF"��־
	public static int fileLength;                              //4�ļ�����  �ļ��ܳ��� - 8
	public static char wavTag[] = { 'W', 'A', 'V', 'E' };      //4�ֽ� "WAVE"��־ 
	public static char FmtHdrID[] = { 'f', 'm', 't', ' ' };    //4�ֽ�  "fmt"��־ 
	public static int FmtHdrLeth = 16;                         //4�ֽ� �����ֽڣ�������
	public static short FormatTag = 1;                         //2�ֽ�  ��ʽ���ͣ�10HΪPCM��ʽ����������)
	public static short Channels = 1;                          //2�ֽ� ͨ������������Ϊ1��˫����Ϊ2
	public static int SamplesPerSec;                           //2�ֽ� �����ʣ�ÿ��������������ʾÿ��ͨ���Ĳ����ٶ�
	public static int AvgBytesPerSec;                          //4�ֽ�   ������Ƶ���ݴ������ʣ���ֵΪͨ������ÿ������λ����ÿ����������λ����8������������ô�ֵ���Թ��ƻ������Ĵ�С�� 
	public static short BlockAlign = 2;                        //2�ֽ�  ���ݿ�ĵ����������ֽ���ģ�����ֵΪͨ������ÿ����������λֵ��8�����������Ҫһ�δ�������ֵ��С���ֽ����ݣ��Ա㽫��ֵ���ڻ������ĵ����� 
	public static short BitsPerSample = 16;                    //2�ֽ�  ÿ����������λ������ʾÿ�������и�������������λ��������ж����������ÿ���������ԣ�������С��һ���� 
	public static char DataHdrID[] = { 'd', 'a', 't', 'a' };   //4�ֽ� ���ݱ�Ƿ���data��
	public static int DataHdrLeth;//4�ֽ� �������ݵĳ���  �ļ��ܳ���-44

	/** 
	 * @Method: getHeader 
	 * @Description: ȡwave�ļ�ͷ
	 * @param fileLength ����������
	 * @param samplesPerSec ������
	 * @return wave�ļ�ͷ
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
