package demo.travel.walker;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class speak_search extends Activity implements OnClickListener {
	// private Button close;
	private TextView TextView01;
	private TextView tvResult1;
	private Button Button01;
	public static final int VOICE_RECOGNITION_REQUEST_CODE = 0x1008;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.speak_search);

		TextView01 = (TextView) this.findViewById(R.id.Button01);
		Button01 = (Button) this.findViewById(R.id.Button01);
		// tvResult1 = (TextView) findViewById(R.id.tvResult1);

		// tvResult1.setText("aa");
		Button01.setOnClickListener(new Button.OnClickListener() {

			// @Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PackageManager pm = getPackageManager();
				/* 查詢有無裝Google Voice Search Engine */
				List<ResolveInfo> activities = pm
						.queryIntentActivities(new Intent(
								RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
				/* 若有安裝Google Voice Search Engine */
				if (activities.size() != 0) {
					try {
						/* 語音辨識Intent */
						Intent intent = new Intent(
								RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

						intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
								RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
						/* 在辨識畫面出現的說明 */
						intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "語音辨識");
						/* 開啟語音辨識Intent */
						startActivityForResult(intent,
								VOICE_RECOGNITION_REQUEST_CODE);
						Log.v("VoiceActivity", "startActivityForResult");
					} catch (Exception e) {
						TextView01.setText("" + e.getMessage());
						Toast.makeText(speak_search.this, e.getMessage(),
								Toast.LENGTH_LONG).show();
					}

				} else {
					TextView01.setText("RecognizerIntent NOT Found!");
					Toast.makeText(speak_search.this,
							"RecognizerIntent NOT Found!", Toast.LENGTH_LONG)
							.show();
				}
			}

		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		// tvResult1 = (TextView) this.findViewById(R.id.tvResult1);
		// tvResult1.setText(Integer.toString(resultCode));

		switch (requestCode) {
		case VOICE_RECOGNITION_REQUEST_CODE:
			if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
					&& resultCode == RESULT_OK) {
				final List<String> strRet;

				/* 取得辨識結果 */
				ArrayList<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				strRet = results.subList(0, 3);
				/* 設置選項數目 */

				if (strRet.get(0).length() > 0) {
					tvResult1.setText(strRet.get(0));
					/* 設置alertdialog作為選單 */
					AlertDialog aa = null;
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("MEMU");
					builder.setSingleChoiceItems(
							strRet.toArray(new CharSequence[strRet.size()]),
							-1, new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									/* 點選時會出現該選項 */
									Toast.makeText(getApplicationContext(),
											(CharSequence) strRet,
											Toast.LENGTH_SHORT).show();

								}
							});

					aa = builder.create();
					aa.show();
				}
			}

			else

			{
				TextView01.setText("Can not recognize...");
				Toast.makeText(speak_search.this, "Can not recognize...",
						Toast.LENGTH_LONG).show();
			}

			break;

		/* super.onActivityResult(requestCode, resultCode, data); */
		}

	}

	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub

	};

}