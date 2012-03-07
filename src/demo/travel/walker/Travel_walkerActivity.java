package demo.travel.walker;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.openintents.intents.AbstractWikitudeARIntent;
import org.openintents.intents.WikitudeARIntent;
import org.openintents.intents.WikitudePOI;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class Travel_walkerActivity extends Activity {
	/** Called when the activity is first created. */
	private Button btnLeft01; // 左上按鈕
	private Button btnRight02; // 右上按鈕
	private Button btnLeft03; // 左下按鈕
	private Button btnRight04; // 右下按鈕

	String correctData = null;
	String x = "";
	String y = "";

	String Name[];
	String Address[];
	String Web[];
	String Cal[];
	String Peen[];
	String Lat[];
	String Long[];
	String Dis[];

	GeoPoint gpNow;
	/** the callback-intent after pressing any buttons */
	private static final String CALLBACK_INTENT = "wikitudeapi.mycallbackactivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btnLeft01 = (Button) findViewById(R.id.Button01);
		btnRight02 = (Button) findViewById(R.id.Button02);
		btnLeft03 = (Button) findViewById(R.id.Button03);
		btnRight04 = (Button) findViewById(R.id.Button04);

		btnLeft01.setOnClickListener(new View.OnClickListener() {
			// @Override
			public void onClick(View v) {
				// transmission();
				// strMan();
				Log.v("test", "btn1 clicked");
				Travel_walkerActivity.this.startARViewWithIcons();
				// TODO Auto-generated method stub
				// Toast.makeText(getBaseContext(), "新功能即將上市，敬請期待",
				// Toast.LENGTH_SHORT).show();
			}
		});

		btnRight02.setOnClickListener(new View.OnClickListener() {
			// @Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 語音辨識搜尋按鈕監聽
				Intent intent2 = new Intent();
				intent2.setClass(Travel_walkerActivity.this, speak_search.class);
				startActivity(intent2);
				// finish();
			}
		});

		btnLeft03.setOnClickListener(new View.OnClickListener() {
			// @Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(), "您使用的是免費版本，部分功能受到限制！",
						Toast.LENGTH_SHORT).show();

			}
		});

		btnRight04.setOnClickListener(new View.OnClickListener() {
			// @Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Toast.makeText(getBaseContext(), "毫無反應，只是一個按鈕",
						Toast.LENGTH_SHORT).show();

			}
		});
	}

	private void transmission() {
		// get shop's information
		DecimalFormat nf = new DecimalFormat("0.000000");
		x = "23.89963";
		// x = nf.format(gpNow.getLatitudeE6()/1E6);
		y = "121.545975";
		// y = nf.format(gpNow.getLongitudeE6()/1E6);

		String uriAPI = "http://134.208.3.217/dev/index.php?pointx=" + x
				+ "&pointy=" + y + "&range=" + 10000 + "&peen=" + 1 + "&kind="
				+ "不限";
		correctData = null;
		HttpGet request = new HttpGet(uriAPI);

		try {
			// HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				correctData = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (ClientProtocolException e) {
			Toast.makeText(Travel_walkerActivity.this,
					e.getMessage().toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(Travel_walkerActivity.this,
					e.getMessage().toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (Exception e) {
			// Toast.makeText(Travel_walkerActivity.this,
			// e.getMessage().toString(), Toast.LENGTH_SHORT).show();
			Log.e("transmission", e.toString());
			e.printStackTrace();
		}
	}

	public void strMan() {
		String temp[] = new String[correctData.split(";").length];
		temp = correctData.split(";");
		String temp2[] = new String[8];

		Name = new String[temp.length];
		Address = new String[temp.length];
		Cal = new String[temp.length];
		Lat = new String[temp.length];
		Long = new String[temp.length];
		Web = new String[temp.length];
		Peen = new String[temp.length];
		Dis = new String[temp.length];

		for (int i = 0; i < temp.length; i++) {
			temp2 = temp[i].split(",");
			Name[i] = temp2[0];
			Address[i] = temp2[1];
			Cal[i] = temp2[2];
			Lat[i] = temp2[3];
			Long[i] = temp2[4];
			if (!temp2[5].equals(""))
				Web[i] = temp2[5];
			else
				Web[i] = "無資料";
			Peen[i] = temp2[6];
			Dis[i] = temp2[7];
		}
	}

	void startARViewWithIcons() { // Create the basic intent
		WikitudeARIntent intent = prepareIntent();

		// Optionally add a title
		intent.addTitleText("AR app with custom icons");
		intent.setPrintMarkerSubText(false);
		// Optionally: Add icons
		addIcons(intent);
		// And launch the intent
		try {
			intent.startIntent(this);
		} catch (ActivityNotFoundException e) {
			AbstractWikitudeARIntent.handleWikitudeNotFound(this);
		}
	}

	private WikitudeARIntent prepareIntent() {
		// create the intent
		WikitudeARIntent intent = new WikitudeARIntent(this.getApplication(),
				"3eae2e49-94f3-48da-9834-078ce11dc0dd", "James Chou");
		// add the POIs
		this.addPois(intent);
		// add one menu item
		intent.setMenuItem1("My menu item",
				Travel_walkerActivity.CALLBACK_INTENT);
		intent.setPrintMarkerSubText(true);
		return intent;
	}

	private void addPois(WikitudeARIntent intent) {
		// 開發測試中 - many
		WikitudePOI poi_array[] = new WikitudePOI[Name.length];
		for (int i = 0; i < Name.length; i++) {
			poi_array[i] = new WikitudePOI(Double.parseDouble(Lat[i]),
					Double.parseDouble(Long[i]), 0, Name[i], Address[i]);
			poi_array[i].setLink(Web[i]);
			poi_array[i].setDetailAction(Travel_walkerActivity.CALLBACK_INTENT);
		}
		List<WikitudePOI> pois = new ArrayList<WikitudePOI>();
		for (int i = 0; i < Name.length; i++) {
			pois.add(poi_array[i]);
		}
		intent.addPOIs(pois);
		((ARApplication) this.getApplication()).setPois(pois);
	}

	private void addIcons(WikitudeARIntent intent) {
		ArrayList<WikitudePOI> pois = intent.getPOIs();
		Resources res = getResources();
		for (int i = 0; i < Name.length; i++) {
			pois.get(i).setIconresource(
					res.getResourceName(android.R.drawable.btn_star_big_on));
		}
		// to use this, make sure you have the file present on the sdcard
		// pois.get(3).setIconuri("content://com.IconCP/sdcard/flag_austria.png");
	}
}