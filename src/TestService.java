
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.service.ControllerService;


public class TestService {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//�������20006�˿ڼ����ͻ��������TCP����  
        ServerSocket server = new ServerSocket(5770);  
        Socket client = null;  
        boolean f = true;  
        while(f){  
            //�ȴ��ͻ��˵����ӣ����û�л�ȡ����  
            client = server.accept();  
            System.out.println("��ͻ������ӳɹ���");  
            //Ϊÿ���ͻ������ӿ���һ���߳�  
            new Thread(new ControllerService(client)).start(); 
            
            f = false;
        }  
        server.close();  
	}

}
