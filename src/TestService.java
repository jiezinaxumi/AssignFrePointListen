
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.service.ControllerService;


public class TestService {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//服务端在20006端口监听客户端请求的TCP连接  
        ServerSocket server = new ServerSocket(5770);  
        Socket client = null;  
        boolean f = true;  
        while(f){  
            //等待客户端的连接，如果没有获取连接  
            client = server.accept();  
            System.out.println("与客户端连接成功！");  
            //为每个客户端连接开启一个线程  
            new Thread(new ControllerService(client)).start(); 
            
            f = false;
        }  
        server.close();  
	}

}
