package com.nhs3108.fels2015;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
	SharedPreferences sharedPreferences;
	String name;
	String email;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
		name = sharedPreferences.getString("name", null);
		if(name != null){
			startActivity(new Intent(this, MainActivity.class));
		} else {
			Button loginBtn = (Button)findViewById(R.id.submit_login);
			loginBtn.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					String email = ((EditText)findViewById(R.id.email_edit)).getText().toString();
					String password = ((EditText)findViewById(R.id.password_edit)).getText().toString();
					new SigninTasks(getBaseContext()).execute(email, password);
				}
			});
		}
	}

	public void onResume(){
		super.onResume();
//		if(name != null){
//			startActivity(new Intent(this, MainActivity.class));
//		}
	}
	class SigninTasks extends AsyncTask<String, Void, String> {
		private Context context;
		private ProgressDialog progressDialog;
		private JSONObject responseJson;
		private int statusCode;
		final String URL = "https://manh-nt.herokuapp.com/login.json";
		SigninTasks(Context context){
			this.context = context;
		}

		protected void onPreExecute(){
			super.onPreExecute();
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setMessage("Đang đăng nhập...");
			progressDialog.setIndeterminate(false);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		protected String doInBackground(String... args){
			String email = (String)args[0];
			String password = (String)args[1];
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(URL);
			String responseBody = null;
			try{
				List nameValuePairs = new ArrayList(2);
				nameValuePairs.add(new BasicNameValuePair("session[email]", email));
				nameValuePairs.add(new BasicNameValuePair("session[password]", password));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpClient.execute(httpPost);
				statusCode = response.getStatusLine().getStatusCode();
				responseBody = EntityUtils.toString(response.getEntity());
				responseJson = new JSONObject(responseBody);
			}catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
			return responseBody;
		}


		protected void onPostExecute(String result){
			super.onPostExecute(result);
			progressDialog.dismiss();
			if (statusCode == 200){
				try {
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("email", responseJson.getJSONObject("user").getString("email"));
					editor.putString("name", responseJson.getJSONObject("user").getString("name"));
					editor.commit();
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					finish();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e){
					e.printStackTrace();
				}
			}else if (statusCode == 401){
				Toast.makeText(LoginActivity.this, "Wrong email or password!", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(LoginActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
