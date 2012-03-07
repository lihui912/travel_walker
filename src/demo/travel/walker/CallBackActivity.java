package demo.travel.walker;

import java.io.IOException;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.openintents.intents.WikitudeARIntentHelper;
import org.openintents.intents.WikitudePOI;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CallBackActivity extends Activity {

	private static final int POI_CLICKED_DIALOG = 1;
	private static final int NOTHING_SELECTED_DIALOG = 2;
	private int poiId;
	private List<WikitudePOI> pois;
	private ListView arListView;
	private Handler mAR_handler = new Handler();
	private Handler mThreadHandler;
	private HandlerThread mThread;

	/* 資料庫所需成員變數 */
	private MySQLiteOpenHelper dbHelper = null;
	private int version = 1;
	ResultSet rs;

	private LocationManager mLocationManager;
	private Location mLocation;
	private String mLocationPrivider = "";

	String correctData = null;
	String x = "";
	String y = "";

	String Name;
	String Address;
	String Web;
	String Cal;
	String Peen;
	String Lat;
	String Long;
	String Dis;

	GeoPoint gpNow, from, to;
	private String Url;

	// 擺在市區測試的座標點
	GeoPoint Test = new GeoPoint((int) (23.89963 * 1000000),
			(int) (121.545975 * 1000000));

	/* 資料庫資料表 */
	private String tables[] = { "t_restaurant", "t_favorite" };

	/* 資料庫欄位名稱 */
	private String fieldNames[][] = {
			{ "f_id", "f_name", "f_address", "f_cal", "f_web", "f_peen",
					"f_kind" }, { "v_id", "v_number" } };

	/* 資料庫欄位資料型態 */
	private String fieldTypes[][] = {
			{ "INTEGER PRIMARY KEY AUTOINCREMENT", "text", "text", "text",
					"text", "float", "text" },
			{ "INTEGER PRIMARY KEY AUTOINCREMENT", "text" } };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ar_list);
		arListView = (ListView) findViewById(R.id.list);
		pois = ((ARApplication) this.getApplication()).getPois();
		poiId = this.getIntent().getIntExtra(
				WikitudeARIntentHelper.EXTRA_INDEX_SELECTED_POI, -1);
		/* 資料庫連線 */
		dbHelper = new MySQLiteOpenHelper(this, "xdb", null, version, tables,
				fieldNames, fieldTypes);
		/* Provider初始化 */
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		/* 取得Provider與Location */
		getLocationPrivider();
		gpNow = getGeoByLocation(mLocation);

		/* 開啟一個thread執行transmission */
		mThread = new HandlerThread("trans");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
		mThreadHandler.post(transmission);
		// transmission();
		// strMan();
		updateListView();

		arListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int id,
					long arg3) {
				switch (id) {
				case 0:
					break;

				case 1:
					/* 地址 - 路徑規劃 */
					// 取得GeoPoint
					// gpNow = Test;
					from = gpNow;
					to = getGeoByAddress(pois.get(poiId).getDescription());
					// 路徑規畫
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					// 傳入所需的地標位址
					intent.setData(Uri
							.parse("http://maps.google.com/maps?f=d&saddr="
									+ GeoPointToString(from) + "&daddr="
									+ pois.get(poiId).getDescription()
									+ "&hl=tw"));
					startActivity(intent);
					break;

				case 2:
					/* 電話 - 撥號 */
					try {
						/* 取得使用者電話的字串 */
						String strInput = Cal;
						if (isPhoneNumberValid(strInput) == true) {
							/* 建構一個新的Intent並執行action.CALL的常數與透過Uri將字串帶入 */
							Intent myIntentDial = new Intent(
									"android.intent.action.CALL", Uri
											.parse("tel:" + strInput));
							/* 在startActivity()方法中帶入自訂的Intent物件以執行撥打電話的工作 */
							startActivity(myIntentDial);
						} else
							Toast.makeText(CallBackActivity.this, "輸入的電話格式不符",
									Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;

				case 3:
					/* 網址 - 連結網頁 */
					if (pois.get(poiId).getLink().equals("無資料"))
						break;
					else {
						Url = pois.get(poiId).getLink();
						goUrl(Url);
					}
					break;

				case 4:
					/* 新增至我的最愛 */

					String f[] = { "f_id", "f_name" };
					String[] selectionArgs = { Name };
					// 呼叫select方法搜尋資料表 /
					Cursor c = dbHelper.select(tables[0], f, "f_name=?",
							selectionArgs, null, null, null);
					String strRes = "";
					while (c.moveToNext()) {
						strRes += c.getString(0) + "\n";
					}

					if (strRes == "") {
						// 資料庫未找到餐廳名稱，新增它 /
						String f2[] = { "f_name", "f_address", "f_cal",
								"f_web", "f_peen", "f_kind" }; // +
						String v1[] = { Name.trim(), Address.trim(),
								Cal.trim(), Web.trim(), Peen.trim(), "測試" }; // +
						long rowid = dbHelper.insert(tables[0], f2, v1);
						strRes += rowid + "\n";
						Toast.makeText(CallBackActivity.this, "新增至我的最愛成功",
								Toast.LENGTH_SHORT).show();
					} else {// 餐廳名稱已存在資料庫 /
						Toast.makeText(CallBackActivity.this, "餐廳名稱已存在我的最愛",
								Toast.LENGTH_SHORT).show();
					}
					break;

				case 5:
					/*
					 * Intent intentW = new Intent();
					 * intentW.setClass(CallBackActivity.this, FoodNote.class);
					 * startActivity(intentW); //finish();
					 */
					break;
				}
			}
		});
	}

	private Runnable transmission = new Runnable() {
		public void run() {
			// get shop's information
			String uriAPI = "http://134.208.3.217/dev/getshopinfo.php?shop_name="
					+ pois.get(poiId).getName();
			correctData = null;
			HttpGet request = new HttpGet(uriAPI);
			try {
				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(request);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					correctData = EntityUtils
							.toString(httpResponse.getEntity());
				}
			} catch (ClientProtocolException e) {
				Toast.makeText(CallBackActivity.this,
						e.getMessage().toString(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(CallBackActivity.this,
						e.getMessage().toString(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (Exception e) {
				Toast.makeText(CallBackActivity.this,
						e.getMessage().toString(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			mAR_handler.post(strMan);
		}
	};

	private Runnable strMan = new Runnable() {
		public void run() {
			String temp2[] = new String[7];
			temp2 = correctData.split(",");
			Name = temp2[0];
			Address = temp2[1];
			Cal = temp2[2];
			Lat = temp2[3];
			Long = temp2[4];
			if (!temp2[5].equals(""))
				Web = temp2[5];
			else
				Web = "無資料";
			Peen = temp2[6];
		}
	};

	private void updateListView() {
		if (pois != null && poiId != -1) {
			// this.showDialog(CallBackActivity.POI_CLICKED_DIALOG);
			String[] strs = { pois.get(poiId).getName(), Address, Cal,
					pois.get(poiId).getLink(), "新增至我的最愛", "撰寫食記" };
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					CallBackActivity.this, android.R.layout.simple_list_item_1,
					strs);
			arListView.setItemsCanFocus(true);
			arListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			arListView.setAdapter(adapter);
		} else {
			this.showDialog(CallBackActivity.NOTHING_SELECTED_DIALOG);
		}
	}

	/**
	 * 查詢地址的地理座標
	 * 
	 * @param strSearchAddress
	 *            地址字串
	 * @return GeoPoint 地理座標物件
	 */
	private GeoPoint getGeoByAddress(String strSearchAddress) {
		GeoPoint gp = null;
		try {
			if (strSearchAddress != "") {
				Geocoder mGeocoder01 = new Geocoder(CallBackActivity.this,
						Locale.getDefault());
				List<Address> lstAddress = mGeocoder01.getFromLocationName(
						strSearchAddress, 1);
				if (!lstAddress.isEmpty()) {
					Address adsLocation = lstAddress.get(0);
					/* 1E6 = 1000000 */
					double geoLatitude = adsLocation.getLatitude() * 1E6;
					double geoLongitude = adsLocation.getLongitude() * 1E6;
					gp = new GeoPoint((int) geoLatitude, (int) geoLongitude);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gp;
	}

	private String GeoPointToString(GeoPoint gp) {
		String strReturn = "";
		try {
			/* 當Location存在 */
			if (gp != null) {
				double geoLatitude = (int) gp.getLatitudeE6() / 1E6;
				double geoLongitude = (int) gp.getLongitudeE6() / 1E6;
				strReturn = String.valueOf(geoLatitude) + ","
						+ String.valueOf(geoLongitude);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strReturn;
	}

	/** 撥號 */
	public static boolean isPhoneNumberValid(String phoneNumber) {
		boolean isValid = false;
		/*
		 * 可接受的電話格式有: ^\\(? : 可以使用 "(" 作為開頭 (\\d{3}): 緊接著三個數字 \\)? : 可以使用")"接續
		 * [- ]? : 在上述格式後可以使用具選擇性的 "-". (\\d{3}) : 再緊接著三個數字 [- ]? : 可以使用具選擇性的
		 * "-" 接續. (\\d{4})$: 以四個數字結束. 可以比對下列數字格式: (123)456-7890, 123-456-7890,
		 * 1234567890, (123)-456-7890
		 */
		String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";
		String expression2 = "^\\(?(\\d{2})\\)?[- ]?(\\d{4})[- ]?(\\d{4})$";
		String expression3 = "^\\(?(\\d{2})\\)?[- ]?(\\d{7})$";
		String expression4 = "^\\(?(\\d{2})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";

		CharSequence inputStr = phoneNumber;
		/* 建立Pattern */

		Pattern pattern = Pattern.compile(expression);
		/* 將Pattern 以參數傳入Matcher作Regular expression */
		Matcher matcher = pattern.matcher(inputStr);

		/* 建立Pattern2 */
		Pattern pattern2 = Pattern.compile(expression2);
		/* 將Pattern2 以參數傳入Matcher2作Regular expression */
		Matcher matcher2 = pattern2.matcher(inputStr);

		/* 建立Pattern3 */
		Pattern pattern3 = Pattern.compile(expression3);
		/* 將Pattern3 以參數傳入Matcher3作Regular expression */
		Matcher matcher3 = pattern3.matcher(inputStr);

		Pattern pattern4 = Pattern.compile(expression4);
		/* 將Pattern4 以參數傳入Matcher3作Regular expression */
		Matcher matcher4 = pattern4.matcher(inputStr);

		if (matcher.matches() || matcher2.matches() || matcher3.matches()
				|| matcher4.matches()) {
			isValid = true;
		}
		return isValid;
	}

	/** 連結網頁 */
	private void goUrl(String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

	/* 取得LocationProvider */
	public void getLocationPrivider() {
		Criteria mCriteria01 = new Criteria();
		mCriteria01.setAccuracy(Criteria.ACCURACY_FINE);
		mCriteria01.setAltitudeRequired(false);
		mCriteria01.setBearingRequired(false);
		mCriteria01.setCostAllowed(true);
		mCriteria01.setPowerRequirement(Criteria.POWER_LOW);

		mLocationPrivider = mLocationManager.getBestProvider(mCriteria01, true);
		mLocation = mLocationManager.getLastKnownLocation(mLocationPrivider);
	}

	/* 取得GeoPoint的method */
	private GeoPoint getGeoByLocation(Location location) {
		GeoPoint gp = null;
		try {
			if (location != null) {
				double geoLatitude = location.getLatitude() * 1E6;
				double geoLongitude = location.getLongitude() * 1E6;
				gp = new GeoPoint((int) geoLatitude, (int) geoLongitude);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gp;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CallBackActivity.POI_CLICKED_DIALOG:
			String title = "";
			if (poiId != -1 && pois != null) {
				title = String.valueOf(poiId);
			}
			return new AlertDialog.Builder(this)
					.setMessage("My new Intent!")
					.setTitle(
							"Coming from Wikitude, "
									+ Integer.parseInt(pois.get(poiId)
											.getName()) + " clicked")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		case CallBackActivity.NOTHING_SELECTED_DIALOG:
			return new AlertDialog.Builder(this)
					.setMessage("My new Intent!")
					.setTitle("Coming from Wikitude, nothing is selected.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/* 移除thread上的工作 */
		if (mThreadHandler != null) {
			mThreadHandler.removeCallbacks(transmission);
		}
		/* 關掉thread */
		if (mThread != null) {
			mThread.quit();
		}
	}

}
