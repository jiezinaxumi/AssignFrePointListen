import com.db.operation.CRUD;
import com.util.Constance;


/**
 * @author Boris
 * @description 
 * 2016��8��12��
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
	}

}
