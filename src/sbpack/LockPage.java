package sbpack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sb.R;

public class LockPage extends Activity{
	public static String classname = "LockPage";
	private String password = "yo";

	EditText edtxt_password;
	Button btn_login;
	TextView txtvw_status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(new Intent(getBaseContext(),HomePage.class));
		setContentView(R.layout.layout_lock);

		edtxt_password = (EditText) findViewById(R.id.edtxt_password);
		btn_login = (Button) findViewById(R.id.btn_login);
		txtvw_status = (TextView) findViewById(R.id.txtvw_status);

		btn_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(edtxt_password.getText().toString().equals(password)){
					startActivity(new Intent(getBaseContext(),HomePage.class));
				}else{
					txtvw_status.setText("Ooops! try again..");
				}
			}
		});
	}
}
