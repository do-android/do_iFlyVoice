package doext.app;
import android.content.Context;
import core.interfaces.DoIAppDelegate;

/**
 * APP启动的时候会执行onCreate方法；
 *
 */
public class do_iFlyVoice_App implements DoIAppDelegate {

	private static do_iFlyVoice_App instance;
	
	private do_iFlyVoice_App(){
		
	}
	
	public static do_iFlyVoice_App getInstance() {
		if(instance == null){
			instance = new do_iFlyVoice_App();
		}
		return instance;
	}
	
	@Override
	public void onCreate(Context context) {
		// ...do something
	}
	
	public String getModuleTypeID() {
		return "do_iFlyVoice";
	}

	@Override
	public String getTypeID() {
		// TODO Auto-generated method stub
		return getModuleTypeID();
	}
}
