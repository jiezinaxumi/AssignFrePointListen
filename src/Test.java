
import com.manager.CWManager;


public class Test {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub  
		CWManager.getInstance().contorlAndGetData("07530000", "D://", 5, 10);
	}
}
/*
 * select g.grap_id,g.task_id,f.freq_name,r.ip,r.port,to_char(g.start_time,'yyyy-mm-dd hh24:mi:ss'),g.length,g.priorty,g.freq_id,
(select inner_url from tab_app_storage  where sto_id=2) path from tab_grap_task g
 left join tab_task t on g.task_id=t.task_id
 left join tab_mam_freq f on f.freq_id=g.freq_id
 left join tab_mam_receiver r on r.receiver_id=g.receiver_id
 where g.status=70 and g.start_time>sysdate;
 */