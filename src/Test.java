import com.db.operation.CRUD;
import com.util.Constance;
import com.util.Log;


/**
 * @author Boris
 * @description 
 * 2016Äê8ÔÂ12ÈÕ
 */
public class Test {

	public static void updateGrapTaskStatus(){
		CRUD crud = new CRUD();
		String sql = "update tab_grap_task set status = " + Constance.Task.WAIT; 
		crud.update(sql);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		updateGrapTaskStatus();
//		try {
//			System.out.println(1/0);
//			System.out.println(22);
//		} catch (Exception e) {
//			// TODO: handle exception
//			Log.out.error(e);
//		}
	}

}
