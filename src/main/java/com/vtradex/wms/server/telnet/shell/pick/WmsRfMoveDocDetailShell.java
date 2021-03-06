package com.vtradex.wms.server.telnet.shell.pick;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.wimpi.telnetd.net.Connection;

import com.vtradex.kangaroo.shell.BreakException;
import com.vtradex.kangaroo.shell.ContinueException;
import com.vtradex.kangaroo.shell.Thorn4BaseShell;
import com.vtradex.wms.server.telnet.pick.WmsPickRFManager;
import com.vtradex.wms.server.telnet.shell.ShellExceptions;
import com.vtradex.wms.server.utils.MyUtils;

public class WmsRfMoveDocDetailShell extends Thorn4BaseShell{

	public static final String PAGE_ID = "wmsRfMoveDocDetailShell";
	public static final String MOVEDOC_ID = "MOVEDOC_ID";
	public static final String MOVE_DOC_DETAILS = "MOVE_DOC_DETAILS";
	
	private final WmsPickRFManager pickRFManager;
	public WmsRfMoveDocDetailShell(WmsPickRFManager pickRFManager) {
		this.pickRFManager = pickRFManager;
	}

	@SuppressWarnings("unchecked")
	protected void mainProcess(Connection connection) throws BreakException,
			ContinueException, IOException, Exception {
		String messge = "";
		Long moveDocId = (Long)this.getParentValue(MOVEDOC_ID);
		
		List<Object[]> moveDocDetails = null;
		moveDocDetails = (List<Object[]>) this.getParentContext().get(MOVE_DOC_DETAILS);
		if(moveDocDetails==null){
			moveDocDetails = pickRFManager.findUnPickMove(moveDocId);
		}
		if(moveDocDetails==null){
			messge = ShellExceptions.PICK_TASK_NULL;
			this.forward(WmsPickMoveDocShell.PAGE_ID,messge);
		}else{
			String key = null;
			Map<String,String> moveCodes = new HashMap<String,String>();
			Map<String,String> isPartPick = new HashMap<String,String>();
			
			output("NO"+" |"+"物料编码"+" |"+"分配量"+" |"+"类型");
			for(int i = 0 ; i<moveDocDetails.size() ; i++){
				Object[] obj = moveDocDetails.get(i);//[movedocdetailid,itemcode,unmoveqty,isPartPick]
				key = (i+1)+"";
				moveCodes.put(key, obj[0].toString());
				isPartPick.put(obj[0].toString(), obj[3].toString());
				output(key,obj[1]+" | "+obj[2]+" |"+obj[3]);
			}
			String moveDetailId = this.getTextField("任务序号(00拣货列表菜单)");
			if(MyUtils.OVER.equals(moveDetailId)){
				messge = "已返回拣货列表菜单";
				this.forward(WmsPickMoveDocShell.PAGE_ID,messge);
			}
			moveDetailId = moveCodes.get(moveDetailId);
			if(moveDetailId==null){
				messge = "任务序号有误,请重新录入";
				this.context.put(MOVE_DOC_DETAILS, moveDocDetails);
				this.context.put(MOVEDOC_ID, moveDocId);
				this.forward(WmsRfMoveDocDetailShell.PAGE_ID,messge);
			}else{
				messge = "拣货类型:"+isPartPick.get(moveDetailId);
				this.context.put(WmsPickPartShell.MOVE_DETAIL_ID, moveDetailId);
				this.context.put(WmsPickPartShell.PICK_TYPE, isPartPick.get(moveDetailId));
				this.context.put(WmsPickPartShell.IS_CONTAINER, true);
				this.context.put(MOVEDOC_ID, moveDocId);
				this.forward(WmsPickPartShell.PAGE_ID,messge);
			}
		}
	}
}
