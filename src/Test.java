
import java.io.IOException;








import com.client.Workstation;
import com.listener.ControllerListener;
import com.service.ControllerService;
import com.util.Config;


public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub  
		ControllerService.start();
		ControllerService.setControllerListener(new ControllerListener() {
			
		Workstation workstation = new Workstation(Config.WORKSTATION_UDP_PORT);
		
		@Override
			public void onReceivedReportMsg(byte[] reportBuffer) {
				// TODO Auto-generated method stub
				if ((reportBuffer[99] & 0xFF) == 0x00 && (reportBuffer[100] & 0xFF) == 0x00) {
					workstation.sendApplyMessageByReportBuffer(reportBuffer);
				}
			}
		});

	}

}
