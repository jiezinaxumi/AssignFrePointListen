
import java.io.IOException;









import com.client.Workstation;
import com.listener.ControllerListener;
import com.manager.CWManager;
import com.service.ControllerService;
import com.util.Config;


public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub  
		CWManager.getInstance().contorlAndGetData("06210000", "D://", 5, 15);
	}

}
