package com.nhs3108.fels2015;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nhs3108 on 15/12/2015.
 */
public class LoginActivity extends Activity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Button loginBtn = (Button)findViewById(R.id.submit_login);
		loginBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				String email = ((EditText)findViewById(R.id.email_edit)).getText().toString();
				String password = ((EditText)findViewById(R.id.password_edit)).getText().toString();
				new SigninTasks(getBaseContext()).execute(email, password);
				Intent intent = new Intent("com.hongson.frelearningsys.main");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				LoginActivity.this.startActivity(intent);

			}
		});
	}

	class SigninTasks extends AsyncTask<String, Void, String> {
		private Context context;
		private String content = "default";
		private ProgressDialog pDialog;
		private JSONObject user_info;
		SigninTasks(Context context){
			this.context = context;
		}
		protected String doInBackground(String... args){
			String email = (String)args[0];
			String password = (String)args[1];
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("https://manh-nt.herokuapp.com/login.json");
			try{
				List nameValuePairs = new ArrayList(2);
				nameValuePairs.add(new BasicNameValuePair("session[email]", "nhs3108@gmail.com"));
				nameValuePairs.add(new BasicNameValuePair("session[password]", "11111111"));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = httpClient.execute(httpPost);
				int statusCode = response.getStatusLine().getStatusCode();
				final String responseBody = EntityUtils.toString(response.getEntity());
				if (statusCode == 200){
					JSONObject json_response = new JSONObject(responseBody);
					user_info = json_response.getJSONObject("user");
				}else{

				}
			}catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}


		protected  void onPreExecute(){
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Attempting login...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}
		protected void onPostExecute(String result){
			super.onPostExecute(result);
			pDialog.dismiss();
			try {
				SharedPreferences sharedpreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor= sharedpreferences.edit();
				editor.putString("email", user_info.getString("email"));
				editor.putString("name", user_info.getString("name"));
				editor.commit();
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}
}
