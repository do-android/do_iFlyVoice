package doext.implement;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_iFlyVoice_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_iFlyVoice_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_iFlyVoice_Model extends DoSingletonModule implements do_iFlyVoice_IMethod {

	private String savaPath;

	public do_iFlyVoice_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("speak".equals(_methodName)) {
			this.speak(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("pause".equals(_methodName)) { // 暂停播放
			this.pause(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("resume".equals(_methodName)) { // 继续播放
			this.resume(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stop".equals(_methodName)) {
			this.stop(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	// 语音合成对象
	private SpeechSynthesizer mTts;

	@Override
	public void speak(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		final Activity _activity = DoServiceContainer.getPageViewFactory().getAppContext();
		savaPath = _scriptEngine.getCurrentApp().getDataFS().getRootPath() + "/temp/do_iFlyVoic/";
		SpeechUtility.createUtility(_activity, "appid=55750a2b");
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(_activity, mInitListener);

		String _text = DoJsonHelper.getString(_dictParas, "text", "");
		if (TextUtils.isEmpty(_text))
			return;
		String _role = DoJsonHelper.getString(_dictParas, "role", "xiaoyan");
		if (TextUtils.isEmpty(_role))
			_role = "xiaoyan";
		setTTSParam(_role);
		mTts.startSpeaking(_text, mTtsListener);

	}

	private void setTTSParam(String _role) {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置在线合成发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, _role);
		// 设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, "50");
		// 设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, "50");
		// 设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		// 设置合成音频保存路径，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		mTts.setParameter(SpeechConstant.PARAMS, "tts_audio_path=" + savaPath + "deviceone_speck.pcm");
	}

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			getEventCenter().fireEvent("bengin", new DoInvokeResult(getUniqueKey()));
		}

		@Override
		public void onSpeakPaused() {
			getEventCenter().fireEvent("paused", new DoInvokeResult(getUniqueKey()));
		}

		@Override
		public void onSpeakResumed() {
			getEventCenter().fireEvent("resumed", new DoInvokeResult(getUniqueKey()));
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
			// 合成进度
//			mPercentForBuffering = percent;
//			showTip(String.format(getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
//			mPercentForPlaying = percent;
//			showTip(String.format(getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
			if (error != null) {
				_result.setError(error.getPlainDescription(true));
			}
			getEventCenter().fireEvent("finished", _result);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

		}
	};

	@Override
	public void pause(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (mTts != null) {
			mTts.pauseSpeaking();
		}
	}

	@Override
	public void resume(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (mTts != null) {
			mTts.resumeSpeaking();
		}
	}

	@Override
	public void stop(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (mTts != null) {
			mTts.stopSpeaking();
		}
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("open".equals(_methodName)) {
			this.open(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog mIatDialog;

	/**
	 * 打开语音识别功能；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void open(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) {
		final Activity _activity = DoServiceContainer.getPageViewFactory().getAppContext();
		savaPath = _scriptEngine.getCurrentApp().getDataFS().getRootPath() + "/temp/do_iFlyVoic/";
		SpeechUtility.createUtility(_activity, "appid=55750a2b");
		DoServiceContainer.getPageViewFactory().getAppContext().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 初始化识别无UI识别对象
				// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
				mIat = SpeechRecognizer.createRecognizer(_activity, mInitListener);
				// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
				// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
				mIatDialog = new RecognizerDialog(_activity, mInitListener);
				// 设置参数
				setIATParam();

				mIatDialog.setListener(new MyListener(_scriptEngine, _callbackFuncName));

				mIatDialog.show();
			}
		});

	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				Activity _activity = DoServiceContainer.getPageViewFactory().getAppContext();
				Toast.makeText(_activity, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 听写UI监听器
	 */
	private class MyListener implements RecognizerDialogListener {

		private DoIScriptEngine scriptEngine;
		private String callbackFuncName;
		private DoInvokeResult invokeResult;

		public MyListener(DoIScriptEngine _scriptEngine, String _callbackFuncName) {
			this.scriptEngine = _scriptEngine;
			this.callbackFuncName = _callbackFuncName;
			invokeResult = new DoInvokeResult(do_iFlyVoice_Model.this.getUniqueKey());

		}

		public void onResult(RecognizerResult results, boolean isLast) {
			if (isLast)
				return;
			try {
				String _text = parseIatResult(results.getResultString());
				JSONObject _result = new JSONObject();
				_result.put("result", _text);
				_result.put("spell", getPingYin(_text));
				invokeResult.setResultNode(_result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			scriptEngine.callback(callbackFuncName, invokeResult);

		}

		private String parseIatResult(String json) throws JSONException {
			StringBuffer ret = new StringBuffer();
			JSONTokener tokener = new JSONTokener(json);
			JSONObject joResult = new JSONObject(tokener);

			JSONArray words = joResult.getJSONArray("ws");
			for (int i = 0; i < words.length(); i++) {
				// 转写结果词，默认使用第一个结果
				JSONArray items = words.getJSONObject(i).getJSONArray("cw");
				JSONObject obj = items.getJSONObject(0);
				ret.append(obj.getString("w"));
			}
			return ret.toString();
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			invokeResult.setError(error.getPlainDescription(true));
			scriptEngine.callback(callbackFuncName, invokeResult);
		}

	};

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	public void setIATParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理 iat_vadbos_preference
		mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音 iat_vadeos_preference
		mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点 iat_punc_preference
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");

		// 设置音频保存路径，保存音频格式仅为pcm，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, savaPath + "wavaudio.pcm");
		// 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
		// iat_dwa_preference
		// 注：该参数暂时只对在线听写有效
		mIat.setParameter(SpeechConstant.ASR_DWA, "0");
	}

	@Override
	public void dispose() {
		super.dispose();
		if (mIat != null) {
			// 退出时释放连接
			mIat.cancel();
			mIat.destroy();
		}
		if (mTts != null) {
			mTts.stopSpeaking();
			// 退出时释放连接
			mTts.destroy();
		}
	}

	private String getPingYin(String src) {
		char[] t1 = null;
		t1 = src.toCharArray();
		String[] t2 = new String[t1.length];
		// 设置汉字拼音输出的格式
		HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
		t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		t3.setVCharType(HanyuPinyinVCharType.WITH_V);
		String t4 = "";
		int t0 = t1.length;
		try {
			for (int i = 0; i < t0; i++) {
				// 判断是否为汉字字符
				if (java.lang.Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);// 将汉字的几种全拼都存到t2数组中
					t4 += t2[0];// 取出该汉字全拼的第一种读音并连接到字符串t4后
				} else {
					t4 += java.lang.Character.toString(t1[i]);// 如果不是汉字字符，直接取出字符并连接到字符串t4后
				}
			}
			return t4;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return t4;
	}
}