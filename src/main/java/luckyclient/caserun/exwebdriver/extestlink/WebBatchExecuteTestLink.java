package luckyclient.caserun.exwebdriver.extestlink;

import java.io.IOException;
import java.net.MalformedURLException;

import org.openqa.selenium.WebDriver;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import luckyclient.caserun.exinterface.TestControl;
import luckyclient.caserun.exwebdriver.WebDriverInitialization;
import luckyclient.dblog.DbLink;
import luckyclient.dblog.LogOperation;
import luckyclient.testlinkapi.TestBuildApi;
import luckyclient.testlinkapi.TestCaseApi;

/**
 * =================================================================
 * 这是一个受限制的自由软件！您不能在任何未经允许的前提下对程序代码进行修改和用于商业用途；也不允许对程序代码修改后以任何形式任何目的的再发布。
 * 为了尊重作者的劳动成果，LuckyFrame关键版权信息严禁篡改
 * 有任何疑问欢迎联系作者讨论。 QQ:1573584944  seagull1985
 * =================================================================
 * 
 * @author： seagull
 * @date 2017年12月1日 上午9:29:40
 * 
 */
public class WebBatchExecuteTestLink{
	
	@SuppressWarnings("static-access")
	public static void batchCaseExecuteForTast(String projectname,String taskid,String batchcase) throws IOException{
		//记录日志到数据库
		DbLink.exetype = 0;   
		TestControl.TASKID = taskid;
		int drivertype = LogOperation.querydrivertype(taskid);
		WebDriver wd = null;
		try {
			wd = WebDriverInitialization.setWebDriverForTask(drivertype);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		LogOperation caselog = new LogOperation(); 
		TestBuildApi.getBuild(projectname);
		//执行全部非成功状态用例
		if(batchcase.indexOf("ALLFAIL")>-1){    
			String casemore = caselog.unSucCaseUpdate(taskid);
			String[] temp=casemore.split("\\#",-1);
			for(int i=0;i<temp.length;i++){
  			   String testCaseExternalId = temp[i].substring(0, temp[i].indexOf("%"));
			   int version = Integer.parseInt(temp[i].substring(temp[i].indexOf("%")+1,temp[i].length()-1));
			   TestCase testcase = TestCaseApi.getTestCaseByExternalId(testCaseExternalId, version);
			   //删除旧的用例
			   LogOperation.deleteCaseDetail(testCaseExternalId, taskid); 
			   //删除旧的日志
			   LogOperation.deleteCaseLogDetail(testCaseExternalId, taskid);    
			   try {
				WebCaseExecutionTestLink.caseExcution(projectname,testcase, taskid,wd,caselog);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				luckyclient.publicclass.LogUtil.APP.error("用户执行过程中抛出异常！", e);
				e.printStackTrace();
			 }
			}			
		}else{                                           //批量执行用例
			String[] temp=batchcase.split("\\#",-1);
			for(int i=0;i<temp.length;i++){
				String testCaseExternalId = temp[i].substring(0, temp[i].indexOf("%"));
				int version = Integer.parseInt(temp[i].substring(temp[i].indexOf("%")+1,temp[i].length()));
				TestCase testcase = TestCaseApi.getTestCaseByExternalId(testCaseExternalId, version);
				try {
					WebCaseExecutionTestLink.caseExcution(projectname,testcase, taskid,wd,caselog);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					luckyclient.publicclass.LogUtil.APP.error("用户执行过程中抛出异常！", e);
					e.printStackTrace();
				}
			}
		}
        //关闭浏览器
        wd.quit();
	}
	
}
